'use client'

import TextInput from "@/app/components/textInput";

import './index.css'
import { useReducer, useState } from "react";
import Button from "@/app/components/button";
import InputDropDown from "@/app/components/inputDropdown";
import { isEmpty } from "@/_utils/isEmpty";
import { api } from "@/lib/api-clients";
import { Account } from "@/models/account";

type TransactionInput = {
    accountRefTo: string
    accountRefFrom: string
    amount: number
    date: string
}

type TransactionInputError = {
    accountFrom: string
    accountTo: string
    amount: string
    date: string
}

const initTransactionInput: TransactionInput = {
    accountRefFrom: '',
    accountRefTo: '',
    amount: 0,
    date: ''
}

const initTransactionInputError: TransactionInputError = {
    accountFrom: '',
    accountTo: '',
    amount: '',
    date: ''
}

const verifyInput = (input: TransactionInput): {isOk: boolean, errors: TransactionInputError} => {
    let isOk = true 
    let errors: TransactionInputError = initTransactionInputError

    if(isEmpty(input.accountRefFrom)) {
        isOk = false
        errors.accountFrom = "Vous devez selectionner un compte"
    } 

    if(isEmpty(input.accountRefTo)) {
        isOk = false
        errors.accountTo = "Vous devez selectionner un compte"
    } 

    if (input.amount <= 0) {
        isOk = false
        errors.amount = "Le prix doit etre superieur a 0"
    }

    return {
        isOk,
        errors
    }
} 

type ActionInput = {
    type: string
    field: string
    value: string
}

function reducer(state: TransactionInput, action: ActionInput) {
    if (action.type === 'update_field') {
        return {
            ...state,
            [action.field]: action.value
        }
    }

    return state
}

type Props = {
    accounts: Account[]
    onSubmit: () => void
}

export default function TransfertTransactionForm({accounts, onSubmit}: Props) {
    const [inputTransaction, dispatch] = useReducer(reducer, initTransactionInput)
    const [errorInputTransaction, setErrorInputTransaction] = useState<TransactionInputError>(initTransactionInputError)

    function handleInputTransaction(event: any) {
        dispatch({
            type: 'update_field', 
            field: event.target.name, 
            value: event.target.value
        })
    }

    function handleSelectOption(name: any, value: any) {
        dispatch({type: 'update_field', field: name, value: value})
    }

    const displayAccount = (accountId: string) => {
        let tag = accounts.find((account) => account.accountId === accountId)
        if (tag) {
            return tag.name
        }
        return accountId
    }

    async function save() {
        try {
            const {isOk, errors} = verifyInput(inputTransaction)
            setErrorInputTransaction(errors)
            if (isOk) {
                await api.post(`/transactions-transfert`, inputTransaction)
                onSubmit()
            }
        } catch(err: any) {
            alert(err)
        }
    }
    
    return (
        <div className="transaction-content">
            <div className='modal-body'>
                <div>
                    <InputDropDown 
                        type={'text'}
                        label={'Comptes'}
                        value={displayAccount(inputTransaction.accountRefFrom)}
                        name={'accountFrom'}
                        onChange={() => {}}
                        options={accounts.map(account => ({ value: account.accountId, displayValue: account.name }))}
                        onClickOption={handleSelectOption}
                        error={errorInputTransaction.accountFrom}
                        overOnBlur={undefined} 
                        placeholder={""} 
                        remover={false}
                    />
                    <InputDropDown 
                        type={'text'}
                        label={'Comptes'}
                        value={displayAccount(inputTransaction.accountRefTo)}
                        name={'accountTo'}
                        onChange={() => {}}
                        options={accounts.map(account => ({ value: account.accountId, displayValue: account.name }))}
                        onClickOption={handleSelectOption}
                        error={errorInputTransaction.accountTo}
                        overOnBlur={undefined} 
                        placeholder={""} 
                        remover={false}
                    />
                    <TextInput 
                        type={'number'} 
                        title={'Prix'} 
                        value={inputTransaction.amount} 
                        name={'amount'} 
                        onChange={handleInputTransaction}  
                        error={errorInputTransaction.amount}  
                    />
                    <TextInput 
                        type={'date'} 
                        title={'date'} 
                        value={inputTransaction.date} 
                        name={'date'} 
                        onChange={handleInputTransaction}  
                        error={errorInputTransaction.date} 
                    />
                <div className='flex justify-around'>
                    <Button title={'Annuler'} backgroundColor={'#1E3050'} colorText={'white'} onClick={onSubmit} />
                    <Button title={'Transferer'} backgroundColor={'#6755D7'} colorText={'white'} onClick={save} />
                </div>
            </div>
        </div>
    </div>
    )
}