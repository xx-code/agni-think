import { api, CallApiError } from "@/lib/api-clients"
import { Account, AccountType, convertStringToAccountType } from "@/models/account"
import { useState } from "react"

export function useAccountsFetching(doCalculAllBalance: boolean = true) {
    const [accounts, setAccounts] = useState<Account[]>([])
    const [loading, setLoading] = useState<boolean>(true)
    const [error, setError] = useState<any|null>(null)

    const fetchAllAccounts = async () => {
        try {
            setLoading(true)
            const fetchedAccounts: any[] = await api.get('accounts')
            let accounts:Account[] = []
            for(let fetchedAccount of fetchedAccounts) {
                let type = convertStringToAccountType(fetchedAccount['type'])
                if (type)
                    accounts.push(
                        {
                            accountId: fetchedAccount["accountId"],
                            name: fetchedAccount['title'],
                            balance: fetchedAccount['balance'],
                            type: type
                        }
                    )
            }
            
            if (doCalculAllBalance) {
                let totalBalance = accounts.reduce((sum, current) => sum + current.balance, 0)
                accounts.unshift({accountId: '', balance: totalBalance, name: "All", type: AccountType.CHECKING})
            }
         
            setAccounts(accounts)
        } catch(error: any) {
            let apiError: CallApiError = error
            setError(apiError)
        } finally {
            setLoading(false)
        }
    }
    
    return {accounts, loading, error, fetchAllAccounts}
}