import type { ListResponse } from "~/types/api";
import type { GetBankRegisterResponse } from "~/types/api/bank-register";
import type { BankRegisterType } from "~/types/ui/bank-register";

export function bankRegisterResponseToBankRegister(data: GetBankRegisterResponse): BankRegisterType {
    return {
        ...data,
        isActive: data.active,
        accounts: data.accounts.map(i => ({
            accountId: i.accountId,
            accountName: i.accountName,
            bankAccountId: i.bankRegisterId,
            bankName: i.bankAccountName,
            isActive: (i.accountId !== undefined && i.accountId !== null)
        }))
    }
}

export function listBankRegistersResponseToListBankRegisters(data: ListResponse<GetBankRegisterResponse>): ListResponse<BankRegisterType> {
    return {
        items: data.items.map(i => bankRegisterResponseToBankRegister(i)),
        total: data.total
    }
}
