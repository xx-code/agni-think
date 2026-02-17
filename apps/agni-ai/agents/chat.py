import os
from langchain_ollama import ChatOllama
from agents.dto import ChatFinancialAdvisorAgentInput, ChatAgentOutput
from agents.tools import get_finance_profile, get_budgets, get_saving_goals, query_invoices
ollama_link=os.getenv("OLLAMA_LINK")

from langchain_core.messages import SystemMessage, HumanMessage, AIMessage

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
            invoices_info = query_invoices(input.question)

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