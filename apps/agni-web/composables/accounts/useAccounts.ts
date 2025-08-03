import type { ListResponse } from "~/types/api";
import type { GetAllAccountResponse } from "~/types/api/account";

export default function useAccounts(): UseApiFetchReturn<ListResponse<GetAllAccountResponse>> {

}