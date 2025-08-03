import type { ListResponse } from "~/types/api";
import type { GetAccountWithPastInfoResponse } from "~/types/api/account";

export default function useAccountsWitPastBalance(dateStart: Date, dateEnd: Date): 
UseApiFetchReturn<ListResponse<GetAccountWithPastInfoResponse>> {

}