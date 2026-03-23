from backend import get_bank_transaction_untreated, get_categories, get_tags,get_deductions, get_bank_registers, create_transaction, treat_external_transaction, push_notification_to_backend
from backend_dto import CreateInvoiceRequest, InvoiceStatusRequestType, NotificationRequest
from agents.agent import Agent
from agents.tools import wrap_tool_query_rag_invoice

def treat_new_bank_transaction(model: str="google_genai:gemini-2.5-flash-lite"):
    new_bank_trans = get_bank_transaction_untreated()
    bank_register = get_bank_registers()
    fmt_system_account = "Linker between Bank Account and System Account: "
    for bank in bank_register:
        fmt = [f"(System AccountId: {account.accountId}, Bank account (bank transaction accountId): {account.bankRegisterId} )" for account in bank.accounts] 
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

    new_transaction_id = []

    clerk = Agent(
        knowledge_path="./agents/knowledge/clerk.yaml", 
        output_fmt=CreateInvoiceRequest, 
        model=model, tools=[wrap_tool_query_rag_invoice])
    
    for trans in new_bank_trans:
        task = f"Traite la transaction bancaire: {trans}"
        output = clerk.run(task, [fmt_cats, fmt_tags, fmt_deductions, fmt_system_account])
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