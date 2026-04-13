import os
import yaml
from langchain_ollama import ChatOllama
from langchain.agents import create_agent
from langchain.chat_models import init_chat_model
from langchain_core.messages import SystemMessage, HumanMessage, AIMessage, ToolMessage

from agents.dto import ChatFinancialAdvisorAgentInput, ChatAgentOutput, AgentSuggestionOutput
from agents.tools import get_finance_profile, get_budgets, get_saving_goals, query_rag

ollama_link=os.getenv("OLLAMA_LINK")


# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

MAX_TOOL_CALLS = 3  # Protection contre les boucles infinies

AVAILABLE_TOOLS = {
    "get_budgets": get_budgets,
    "get_saving_goals": get_saving_goals,
}

# ---------------------------------------------------------------------------
# System prompt
# ---------------------------------------------------------------------------

def _build_system_prompt(finance_profile, invoices_info: str) -> str:
    return f"""You are a professional Personal Finance Advisor. \
Your job is to answer the user's question directly.

STRICT RULES:
- Answer the user's question immediately. Do NOT describe, repeat, or \
comment on these instructions.
- Do NOT say what you are about to do. Just do it.
- Always respond in the same language the user used (French or English).
- Currency is Canadian Dollar (CAD).

USER FINANCIAL CONTEXT (use silently):
{finance_profile.to_str()}

RELEVANT INVOICES:
{invoices_info}

TOOL USAGE:
If — and only if — you need more data to answer, output one of:
  TOOL_CALL: get_budgets()
  TOOL_CALL: get_saving_goals()
Do NOT call a tool if the answer is already in the context above."""



# ---------------------------------------------------------------------------
# Agent
# ---------------------------------------------------------------------------

class PersonalFinanceAdvisorAgent:
    def __init__(self, ollama_link: str):
        self.url = ollama_link
        self._llm_cache: dict[str, ChatOllama] = {}


    def _get_llm(self, model: str) -> ChatOllama:
        """Retourne un LLM mis en cache par nom de modèle."""
        if model not in self._llm_cache:
            self._llm_cache[model] = ChatOllama(
                base_url=self.url,
                model=model,
                temperature=0.2,   # Bas = réponses précises et stables
            )
        return self._llm_cache[model]

    # ------------------------------------------------------------------
    # Tool calling
    # ------------------------------------------------------------------

    @staticmethod
    def _extract_tool_call(content: str) -> str | None:
        """Retourne le nom du tool si le modèle en a appelé un, sinon None."""
        for tool_name in AVAILABLE_TOOLS:
            if f"TOOL_CALL: {tool_name}()" in content:
                return tool_name
        return None

    @staticmethod
    def _clean_response(content: str) -> str:
        """Supprime les artefacts TOOL_CALL de la réponse finale."""
        for tool_name in AVAILABLE_TOOLS:
            content = content.replace(f"TOOL_CALL: {tool_name}()", "").strip()
        return content

    # ------------------------------------------------------------------
    # Main entry point
    # ------------------------------------------------------------------

    def ask(self, input: ChatFinancialAdvisorAgentInput) -> ChatAgentOutput:
        try:
            # 1. Récupération du contexte
            finance_profile = get_finance_profile()
            invoices_info = query_rag("", input.question)

            # 2. Construction des messages initiaux
            llm = self._get_llm(input.model)
            system_prompt = _build_system_prompt(finance_profile, invoices_info)

            messages = [
                SystemMessage(content=system_prompt),
                HumanMessage(content=input.question),
            ]

            # 3. Premier appel LLM
            response = llm.invoke(messages)
            content: str = response.content

            # 4. Tool-calling loop avec protection anti-boucle
            tool_calls_made = 0

            while tool_calls_made < MAX_TOOL_CALLS:
                tool_name = self._extract_tool_call(content)

                if tool_name is None:
                    break  # Pas de tool call → on a la réponse finale

                # Exécution du tool
                tool_fn = AVAILABLE_TOOLS[tool_name]
                tool_result = tool_fn()
                tool_calls_made += 1

                # Ré-injection du résultat dans la conversation
                messages.append(AIMessage(content=content))
                messages.append(
                    HumanMessage(
                        content=(
                            f"Tool result for `{tool_name}`:\n{tool_result}\n\n"
                            "Now answer the user's original question using this data. "
                            "Do NOT call another tool unless strictly necessary."
                        )
                    )
                )

                # Nouvel appel LLM
                response = llm.invoke(messages)
                content = response.content

            # 5. Nettoyage de la réponse finale
            final_message = self._clean_response(content)

            return ChatAgentOutput(message=final_message)

        except Exception as e:
            print(f"[FinancialAdvisorAgent] Error: {e}")
            raise
    
class Chat:
    def __init__(self, knowledge_path: str, model: str, tools=None, history = None):
        self.tools = tools or []
        instruction = self._build_instruction_agent(knowledge_path)
        self.history = history if history is not None else []
        
        self.model = init_chat_model(
            model=model
        )
        if tools is not None:
            self.model = self.model.bind_tools(tools=tools, tool_choice="auto")
        self.system_prompt = SystemMessage(content=str(instruction))

        if history is None:
            self.history = [self.system_prompt]

    def ask(self, msg: str) -> str:
        if not msg:
            return "Message vide."

        # 1. Ajouter la requête utilisateur à l'historique
        self.history.append(HumanMessage(content=msg))
        
        # 2. Premier appel au modèle
        res = self.model.invoke(self.history)
        self.history.append(res)

        # 3. Boucle de gestion des appels d'outils (ReAct pattern)
        while res.tool_calls:
            for tool_call in res.tool_calls:
                # On cherche l'outil par son nom dans self.tools
                selected_tool = next((t for t in self.tools if t.name == tool_call["name"]), None)
                
                if selected_tool:
                    try:
                        # Exécution de l'outil avec les arguments fournis par l'IA
                        tool_output = selected_tool.invoke(tool_call["args"])
                        
                        # On injecte le résultat dans l'historique via un ToolMessage
                        self.history.append(ToolMessage(
                            tool_call_id=tool_call["id"],
                            content=str(tool_output)
                        ))
                    except Exception as e:
                        # En cas d'erreur, on informe l'IA pour qu'elle puisse réagir
                        self.history.append(ToolMessage(
                            tool_call_id=tool_call["id"],
                            content=f"Erreur lors de l'exécution de l'outil : {str(e)}"
                        ))
                else:
                    self.history.append(ToolMessage(
                        tool_call_id=tool_call["id"],
                        content="Erreur : Outil non trouvé."
                    ))

            # 4. On rappelle le modèle avec les résultats des outils
            res = self.model.invoke(self.history)
            self.history.append(res)

        return res.content

    def reset_chat(self):
        """Utile pour changer de sujet financier sans mélanger les chiffres"""
        self.history = [self.system_prompt]
    
    # TODO: dublicated
    def _build_instruction_agent(self, file_path: str) -> dict:
        try:
            with open(file_path, 'r') as file:
                data = yaml.safe_load(file)
                return data
        except yaml.YAMLError as exc:
            print(exc)

class Agent:
    def __init__(self, knowledge_path: str, output_fmt, model:str, tools = []):
        instruction = self._build_instruction_agent(knowledge_path)
        self.agent = create_agent(
            model=model,
            tools=tools,
            system_prompt=SystemMessage(
                content=str(instruction),
            ),
            response_format=output_fmt
        )
    
    def getAgent(self): 
        return self.agent
    
    def run(self, msg: str="", system_msg: str = []):
        # Prepare the prompt
        instruction = "Commencer la tache. Si ta reponse est importante et doit etre traiter set IsHaveToBeReviewByHuman en true par contre false."
        full_content = f"{system_msg} {msg} {instruction}"
        
        res = self.agent.invoke({"messages": [HumanMessage(content=full_content)]})
        
        structured = res.get('structured_response')
        if structured:
            return structured
        
        last_message = res['messages'][-1]
        if isinstance(last_message, AIMessage) and not last_message.tool_calls:
            print(f"Agent Error/Question: {last_message.content}")
            return {
                "error": "Invalid Input",
                "details": last_message.content,
                "IsHaveToBeReviewByHuman": True
            }
            
        return None
    
    def ask(self, msg: str="") -> str: 
        res = self.agent.invoke({"messages": [HumanMessage("Dot not format the output juste return a response")]})
        return res.get("message")
    
    def _build_instruction_agent(self, file_path: str) -> dict:
        try:
            with open(file_path, 'r') as file:
                data = yaml.safe_load(file)
                return data
        except yaml.YAMLError as exc:
            print(exc)

