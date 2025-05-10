import { api, CallApiError } from "@/lib/api-clients";
import { Transaction, TransactionType } from "@/models/transaction";
import moment from "moment";
import { useState } from "react";

export type QueryTransactions = {
    page: number,
    limit: number,
    sortBy: string | undefined,
    sortSense: 'asc' | 'desc' | undefined,
    accountFilter: string[], 
    categoryFilter: string[],
    tagFilter: string[],
    dateStart: string | undefined,
    dateEnd: string | undefined,
    type: string | undefined,
    minPrice: number | undefined,
    maxPrice: number | undefined
}

export const useTransactionPagination = () => {
    const [pagination, setPagination] = useState({currentPage: 1, maxPage: 1})
    const [transactions, setTransactions] = useState<Transaction[]>([])
    const [errorPagination, setErrorPagination] = useState<CallApiError|null>(null)

    const fetchTransactionPagination = async (query: QueryTransactions) => {
        try {
            let response: any = await api.get('/transactions', {
                params: query
            })


            setPagination({currentPage: query.page, maxPage: response['maxPages']})
            setTransactions(response['transactions'])
        } catch(error: any) {
            let apiError: CallApiError = error
            setErrorPagination(apiError)
        }
    } 

    return {pagination, transactions, fetchTransactionPagination, errorPagination}
}

export type ResumeMonth = {
    pastMonth: number|undefined
    currentMonth: number|undefined
} 

export const useResumeMonth = () => {
    const [totalSpend, setTotalSpend] = useState<ResumeMonth>({ pastMonth: undefined, currentMonth: undefined });
    const [totalGains, setTotalGains] = useState<ResumeMonth>({ pastMonth: undefined, currentMonth: undefined });

    const getTotalBalance = async (type: string): Promise<ResumeMonth> => {
        return new Promise(async (resolve, reject) => {
            try {              
              let currentMonthDate = moment().startOf('month').format('YYYY-MM-DD')
              let pastMonthDate = moment().endOf('month').format('YYYY-MM-DD')
        
              const resPast: any = await api.get('/transactions-balance', {
                params: {
                    dateStart: currentMonthDate,
                    dateEnd: pastMonthDate,
                    type: type
                }
              });
        
              const resCurrent: any = await api.get('/transactions-balance', {
                params: {
                    dateStart: currentMonthDate,
                    dateEnd: pastMonthDate,
                    type: type
                }
              });
        
              resolve({pastMonth: resPast['balance'], currentMonth: resCurrent['balance'] });
            } catch (error) {
              reject(error);
            }
        })
    }

    const fetchTotals = async () => {
        try {
            const spend = await getTotalBalance("DEBIT");
            const gains = await getTotalBalance("CREDIT");
            setTotalSpend(spend);
            setTotalGains(gains);
        } catch (error) {
            console.error(error);
        }
    };

    return { totalSpend, totalGains, fetchTotals };
}

export const useBalances = () => {
    const [totalSpend, setTotalSpend] = useState<number>();
    const [totalGains, setTotalGains] = useState<number>();

    const getTotalBalance = async (query: QueryTransactions): Promise<number> => {
        return new Promise(async (resolve, reject) => {
            try {
              const res: any = await api.get('/transactions-balance', {
                params: query
              });
        
              resolve(Math.abs(res['balance']))
            } catch (error) {
              reject(error);
            }
        })
    }      

    const fetchTotals = async (query: QueryTransactions) => {
        try {
            let queryDebit: QueryTransactions  = {...query, type: "Debit"}
            const spend = await getTotalBalance(queryDebit);

            let queryCredit: QueryTransactions  = {...query, type: "Credit"}
            const gains = await getTotalBalance(queryCredit);

            setTotalSpend(spend);
            setTotalGains(gains);
        } catch (error) {
            console.error(error);
        }
    };

    return { totalSpend, totalGains, fetchTotals };
}