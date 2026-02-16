import os
from langchain_ollama import ChatOllama
from agents.dto import ChatFinancialAdvisorAgentInput, ChatAgentOutput
from agents.tools import get_finance_profile, get_budgets, get_saving_goals, query_invoices
ollama_link=os.getenv("OLLAMA_LINK")



class PersonalFinanceAdvisorAgent:
    def __init__(self, ollama_link: str):
        self.url = ollama_link

    def ask(self, input: ChatFinancialAdvisorAgentInput) -> ChatAgentOutput:
        try:
            finance_profile = get_finance_profile()
            invoices_info = query_invoices(input.question)

            llm = ChatOllama(
                base_url=self.url,
                model=input.model,
                temperature=0
            )

            SYSTEM_PROMPT = f"""
                You are a professional Personal Finance Advisor.
                Use the following context to answer the user's questions:

                ---
                CURRENT FINANCE PROFILE:
                {finance_profile.to_str()}

                INVOICE SEARCH RESULTS:
                {invoices_info}
                ---

                INSTRUCTIONS:
                1. Always be concise and data-driven.
                2. If you need more information about specific Budgets or Saving Goals to answer a question, you MUST call a tool by writing exactly:
                TOOL_CALL: get_budgets() 
                OR 
                TOOL_CALL: get_saving_goals()
                3. If you have enough information, provide your final financial advice directly.
            """

            # 2. Construction du prompt avec instructions explicites
            messages = [
                ("system", SYSTEM_PROMPT),
                ("human", input.question)
            ]

            response = llm.invoke(messages)
            content = response.content

            # 3. Logique de Tool-Calling Manuel (si le modèle écrit le nom du tool)
            if "TOOL_CALL: get_budgets()" in content:
                budgets = get_budgets()
                # On ré-injecte le résultat et on demande une réponse finale
                messages.append(("assistant", content))
                messages.append(("user", f"Here is the budget data: {budgets}"))
                response = llm.invoke(messages)
            
            elif "TOOL_CALL: get_saving_goals()" in content:
                goals = get_saving_goals()
                messages.append(("assistant", content))
                messages.append(("user", f"Here is the saving goals data: {goals}"))
                response = llm.invoke(messages)

            return ChatAgentOutput(message=response.content)

        except Exception as e:
            print(f"Error in Agent: {e}")
            raise