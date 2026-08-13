import type { UpdateAccountRequest } from "~/types/api/account";
import type { CreateFundRequest } from "~/types/api/fund";
import type { FundForm } from "~/types/form/fund";
import type { FundType } from "~/types/ui/fund";

export function fundFormToCreateFundRequest(form: FundForm): CreateFundRequest {
    return form
} 

export function fundFormToUpdateFundRequest(form: FundForm): UpdateAccountRequest {
    return form
}

export function fundToFundForm(data: FundType): FundForm {
    return data
}