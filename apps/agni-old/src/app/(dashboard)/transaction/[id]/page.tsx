'use client'

import './style.css'
import { useEffect, useReducer, useState } from "react";
import { useAccountsFetching } from "../../hooks/accounts";
import { useCategories, useTags } from "../../hooks/system";
import TransactionForm from '@/app/components/forms/transactionForm';
import { useRouter } from 'next/navigation';
import { convertStringToTransactionType, Transaction } from '@/models/transaction';
import { api } from '@/lib/api-clients';
import { convertStringToAccountType } from '@/models/account';

export default function TransactionEditorPage({params: {id}}: { params: {id: string}}) {
    const hookAccount = useAccountsFetching(false)
    const hookCategory = useCategories()
    const hookTag = useTags()
    const [transaction, setTransaction] = useState<Transaction>()
    const [loading, setLoading] = useState(true)
    const router = useRouter()

    async function getTransaction() {
        setLoading(true)
        try {
            if (id !== 'new-transaction') {
                let fetchechTransaction: any = await api.get(`/transactions/${id}`)
                setTransaction({
                    accountId: fetchechTransaction['accountId'],
                    transactionId: fetchechTransaction['transactionId'],
                    amount: fetchechTransaction['amount'],
                    category: {
                        categoryId: fetchechTransaction['id'],
                        name: fetchechTransaction['title'],
                        color: fetchechTransaction['color'],
                        icon: fetchechTransaction['icon']
                    },
                    date: fetchechTransaction['date'],
                    description: fetchechTransaction['description'],
                    tags: fetchechTransaction['tags'].map((tag: any) => ({tagId: tag['id'], value: tag['value'], color: tag['color']})),
                    type: convertStringToTransactionType(fetchechTransaction["type"])!
                })
            }
        } catch(err: any) {
            alert(err)
        }
        setLoading(false)
    }

    const renderBudgetForm = () => {
        if (hookCategory.error) {
            return (
                <div>
                    Error Category: {hookCategory.error}
                </div>
            )
        }

        if (hookTag.error) {
            return (
                <div>
                    Error tag: {hookCategory.error}
                </div>
            )
        }

        if (loading || hookTag.loading || hookAccount.loading) {
            return (
                <div>Loading...</div>
            )
        }

        return (
            <TransactionForm 
                accounts={hookAccount.accounts} 
                categories={hookCategory.categories} 
                tags={hookTag.tags} 
                transaction={transaction} 
                onSubmit={() => router.back()}            
            />
        ) 
    }

    useEffect(() => {
        hookAccount.fetchAllAccounts()
        hookCategory.fetchCategories()
        hookTag.fetchTags()
        getTransaction()
    }, [])
    
    return (
        <div >
            {renderBudgetForm()}
        </div>
    )
}