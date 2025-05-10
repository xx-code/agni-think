'use client';

import './page.css';
import { useEffect, useState } from "react";
import { QueryTransactions, useBalances, useTransactionPagination } from "../hooks/transactions";
import { useAccountsFetching } from "../hooks/accounts";
import { FilterForm, FilterInput } from "./components/filterForm";
import TopBarInfoAllTransaction from "./components/topBarInfoAllTransaction";
import { useCategories, useTags } from "../hooks/system";
import { useRouter } from "next/navigation";
import { CardTranscationValue } from "@/app/components/cardTransaction";
import { cleanTransactionsPageStorage } from '@/_utils/cleanLocalStorage';
import { convertStringToTransactionType, Transaction } from '@/models/transaction';
import { isEmpty } from '@/_utils/isEmpty';
import { api, CallApiError } from '@/lib/api-clients';
import TransactionPaginations from '../_home22/components/list-transaction';

export default function Transactions() {
    const { accounts, loading, error, fetchAllAccounts } = useAccountsFetching()
    const { transactions, errorPagination, fetchTransactionPagination, pagination } = useTransactionPagination()
    const { totalGains, totalSpend, fetchTotals } = useBalances()
    const hookCategories = useCategories()
    const hookTags = useTags()
    const router = useRouter()
    const [accountSelected, setAccountSelected ] = useState(0)
    const [filter, setFilter] = useState<FilterInput>({categoriesSelected: [], category: '', tag: '', dateEnd: '', dateStart: '', tagsSelected: []})

    function setupRequestFetching(page: number, accountSelected: number, inputFilter: FilterInput) {
        if (page >= 1 || page <= pagination.maxPage) {
          let height_win_need = window.innerHeight / 2;
          let limit = Math.floor(height_win_need / 100);
          const requestPagination: QueryTransactions = {
            page: page,
            limit: limit,
            accountFilter: accountSelected > 0 ? [accounts[accountSelected].accountId] : [],
            categoryFilter: inputFilter.categoriesSelected,
            tagFilter: inputFilter.tagsSelected,
            sortBy: undefined,
            sortSense: undefined,
            dateStart: inputFilter.dateStart,
            dateEnd: inputFilter.dateEnd,
            type: undefined,
            minPrice: undefined,
            maxPrice: undefined
          }
          fetchTransactionPagination(requestPagination)
          fetchTotals(requestPagination)
        }
    }

    function filterTransaction(inputFilter: FilterInput) {
        setupRequestFetching(1, accountSelected, inputFilter)
        setFilter(inputFilter)
    }

    function mapperTransaction(trans: Transaction): CardTranscationValue {
        return {
          transactionId: trans.transactionId,
          amount: trans.amount,
          category: { category_id: trans.category.categoryId, icon: trans.category.icon, color: trans.category.color, title: trans.category.name },
          date: trans.date,
          description: trans.description,
          tags: trans.tags.map(tag => ({ tag_id: tag.tagId, color: tag.color, value: tag.value })),
          type: convertStringToTransactionType(trans.type)! 
        }
    }

    async function deleteTransaction(id: string) {
        try {
          await api.delete(`/transactions/${id}`);

        } catch (error: any) {
            let resError: CallApiError = error
            alert(resError.message) 
        }
    }
    

    async function selectAccount(accountId: string) {
        let index = accounts.findIndex(account => account.accountId === accountId)
        if (index !== -1) {
            setAccountSelected(index)
            setupRequestFetching(1, index, filter)
        }
    }

    async function showTransaction(id: string, filter: FilterInput) {
        localStorage.setItem('currentPage', pagination.currentPage.toString())
        localStorage.setItem('tag_filter', filter.tagsSelected.toString())
        localStorage.setItem('category_filter', filter.categoriesSelected.toString())

        router.push(`/transaction/${id}`)
    }

    const renderFilterForm = () => {
        if (hookCategories.loading || hookTags.loading) {
            return <div>Loading...</div>
        }

        return (
            <FilterForm 
                categories={hookCategories.categories} 
                tags={hookTags.tags} 
                onChangeFilter={filterTransaction} 
                onCleanFilter={filterTransaction}                         
            />
        )
    }



    const renderTransactionPagination = () => {
        if (errorPagination) {
            return <h1>{errorPagination.message}</h1>
        }

        return (
            <TransactionPaginations
                transactions={transactions.map(trans => mapperTransaction(trans))}
                onEdit={(id: string) => showTransaction(id, filter)}
                onDelete={deleteTransaction}
                currentPage={pagination.currentPage}
                maxPage={pagination.maxPage}
                previous={() => setupRequestFetching(pagination.currentPage - 1, accountSelected, filter)}
                next={() => setupRequestFetching(pagination.currentPage + 1, accountSelected, filter)}
            />
        )
    }

    const renderTopInfo = () => {
        if (loading) {
            return loading
        }

        return (
            <TopBarInfoAllTransaction 
                accounts={accounts.map(account => ({accountTitle: account.name, accountId: account.accountId }))} 
                accountSelected={accountSelected}
                onSelectAccount={selectAccount} 
                totalSpend={totalSpend} 
                totalEarning={totalGains}                
            />
        )
    } 

    useEffect(() => {
       hookCategories.fetchCategories()
       hookTags.fetchTags()
       fetchAllAccounts()

       let inputFiler = filter
       let currentPage = 1 

       if (!isEmpty(localStorage.getItem('currentPage')))
            currentPage = Number(localStorage.getItem('currentPage'))
       console.log(currentPage)
       if (!isEmpty(localStorage.getItem('category_filter'))) {
            let catFilter = localStorage.getItem('category_filter')!.split(',')
            inputFiler.categoriesSelected = catFilter
            setFilter({...filter, categoriesSelected: catFilter})
       }
            

       if (!isEmpty(localStorage.getItem('tag_filter'))) {
            let tagFilter = localStorage.getItem('tag_filter')!.split(',')
            inputFiler.tagsSelected = tagFilter 
            setFilter({...filter, tagsSelected: tagFilter})
       }

       console.log(inputFiler)

       setupRequestFetching(currentPage, 0, inputFiler)
       cleanTransactionsPageStorage()
    }, [])

    return (
        <>
            <div className="transactions">
                {
                    renderTopInfo()
                }
                <div className="transactions-content">
                    <div className="left-content">
                        <div className="list">
                            {renderTransactionPagination()}
                        </div>
                    </div>
                    <div className="right-content">
                        {renderFilterForm()}
                    </div>
                </div>
            </div>
        </>
    )
}