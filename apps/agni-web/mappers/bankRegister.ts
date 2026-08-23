import type { ListResponse } from "~/types/api";
import type { GetBankRegisterResponse } from "~/types/api/bank-register";
import type { BankRegisterType } from "~/types/ui/bank-register";

export function bankRegisterResponseToBankRegister(data: GetBankRegisterResponse): BankRegisterType {
    return {
        id: data.bankRegisterId,
        title: data.title,
        active: data.isActive,
        accounts: data.accounts.map(i => ({
            accountId: i.accountId,
            bankAccountId: i.bankAccountId,
            accountName: i.accountName
        }))
    }
}

export function listBankRegistersResponseToListBankRegisters(data: ListResponse<GetBankRegisterResponse>): ListResponse<BankRegisterType> {
    return {
        items: data.items.map(i => bankRegisterResponseToBankRegister(i)),
        total: data.total
    }
}
