import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateBankRegisterRequest } from "~/types/api/bank-register";
import { listBankRegistersResponseToListBankRegisters } from "~/mappers/bankRegister";
import type { BankRegisterType } from "~/types/ui/bank-register";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";

export async function createBankRegister(request: CreateBankRegisterRequest): Promise<CreatedRequest> {
    return await ApiLinkBuilder
        .route<CreatedRequest>(API_ROUTES.BANK_REGISTERS.CREATE_BANK_REGISTER)
        .body(request)
        .execute()
}

export async function fetchAllBankRegister(query: QueryFilterRequest): Promise<ListResponse<BankRegisterType>> {
    return await ApiLinkBuilder
        .route(API_ROUTES.BANK_REGISTERS.GET_BANK_REGISTERS)
        .query(query)
        .mapper(listBankRegistersResponseToListBankRegisters)
        .execute()
}
