from backend import get_bank_transaction_untreated, get_categories, get_tags,get_deductions, get_bank_registers, create_transaction, get_budgets, treat_external_transaction, push_notification_to_backend
from backend_dto import CreateInvoiceRequest, InvoiceStatusRequestType, NotificationRequest, CreatedResponse
from agents.agent import Agent
from agents.tools import wrap_tool_query_rag_invoice
from dataclasses import dataclass

@dataclass
class FormatPrePrompt:
    fmt_system_accounts: str
    fmt_categories: str
    fmt_tags: str
    fmt_budgets: str
    fmt_deductions: str

def clerk_setup_preprompt() -> FormatPrePrompt:
    bank_register = get_bank_registers()
    fmt_system_account = "Linker between Bank Account and System Account: "
    for bank in bank_register:
        fmt = [f"Match external Account Id: {account.bankRegisterId} to AccountId: {account.accountId};" for account in bank.accounts] 
        fmt_system_account += str(fmt)


    cats = get_categories()
    fmt_cats = "System Categories: "
    for cat in cats:
        fmt_cats += f"(id: {cat.id}, name: {cat.title});"
    tags = get_tags()
    fmt_tags = "System Tags: "
    for tag in tags:
        fmt_tags += f"(id: {tag.id}, name: {tag.value});"
    fmt_deductions = "System Deductions: "
    deductions = get_deductions()
    for ded in deductions:
        fmt_deductions += f"(id: {ded.id}, base: {ded.base}, mode: {ded.mode})"
    budgets = get_budgets()
    fmt_budgets = "System Budgets: "
    for budget in budgets:
        fmt_budgets += f"(id: {budget.id}, title: {budget.title})"


    return FormatPrePrompt(
        fmt_system_accounts=fmt_system_account,
        fmt_categories=fmt_cats,
        fmt_tags=fmt_tags,
        fmt_deductions=fmt_deductions,
        fmt_budgets=fmt_budgets
    )


def treat_new_bank_transaction(model: str="google_genai:gemini-2.5-flash-lite"):
    print("AI: Run treat_new_transaction")
    new_bank_trans = get_bank_transaction_untreated()
    new_transaction_id = []

    if len(new_bank_trans) > 0:
        pre_prompt = clerk_setup_preprompt()
        clerk = Agent(
            knowledge_path="./agents/knowledge/clerk.yaml", 
            output_fmt=CreateInvoiceRequest, 
            model=model, tools=[wrap_tool_query_rag_invoice])
        
        for trans in new_bank_trans:
            task = f"Traite la transaction bancaire: {trans}"
            output = clerk.run(task, [pre_prompt.fmt_categories, pre_prompt.fmt_tags, pre_prompt.fmt_deductions, pre_prompt.fmt_budgets, 
                                      pre_prompt.fmt_system_accounts])
            if (isinstance(output, CreateInvoiceRequest)):
                output.status = InvoiceStatusRequestType.Pending
                res_created = create_transaction(input=output)
                treat_external_transaction(trans.id)
                new_transaction_id.append(res_created.newId)
            else:
                msg = f"Le traitement de la transaction ${trans} a echouer \n"
                if output is not None:
                    msg += output.get("detail")
                push_notification_to_backend(NotificationRequest(title="Le Greffier a rencontrer un probleme", content=msg))
            
    return new_transaction_id


def clerk_treat_transaction(transaction_str: str, model: str="google_genai:gemini-2.5-flash-lite") -> CreatedResponse:
    pre_prompt = clerk_setup_preprompt()
    clerk = Agent(
        knowledge_path="./agents/knowledge/clerk.yaml", 
        output_fmt=CreateInvoiceRequest, 
        model=model, tools=[wrap_tool_query_rag_invoice])
    task = f"Traite la transaction ou facture: {transaction_str} et n'existe pas a utile le rag_invoice pour avoir un exemple"
    output = clerk.run(task, [pre_prompt.fmt_categories, pre_prompt.fmt_tags, pre_prompt.fmt_deductions,
                               pre_prompt.fmt_budgets, pre_prompt.fmt_system_accounts])
                    
    if (isinstance(output, CreateInvoiceRequest)):
        output.status = InvoiceStatusRequestType.Pending
        return create_transaction(input=output)
    else:
        msg = f"Le traitement de la transaction ${transaction_str} a echouer \n"
        if output is not None:
            msg += output.get("detail")
        raise msg
        # push_notification_to_backend(NotificationRequest(title="Le Greffier a rencontrer un probleme", content=msg))