'use client'

import TextInput from "@/app/components/textInput";

import './index.css'
import { useReducer, useState } from "react";
import Button from "@/app/components/button";
import InputDropDown from "@/app/components/inputDropdown";
import { isEmpty } from "@/_utils/isEmpty";
import { Account } from "@/models/account";
import { api, CallApiError } from "@/lib/api-clients";

type TransactionInput = {
    account: string
    amount: number
    date: string 
}

type TransactionInputError = {
    account: string
    amount: string
    date: string
}

const initTransactionInput: TransactionInput = {
    account: '',
    amount: 0,
    date: ''
}

const initTransactionInputError: TransactionInputError = {
    account: '',
    amount: '',
    date: ''
}

const verifyInput = (input: TransactionInput): {isOk: boolean, errors: TransactionInputError} => {
    let isOk = true 
    let errors: TransactionInputError = initTransactionInputError

    if(isEmpty(input.account)) {
        isOk = false
        errors.account = "Vous devez selectionner un compte"
    } 

    if (input.amount <= 0) {
        isOk = false
        errors.amount = "Le prix doit etre superieur a 0"
    }

    if (isEmpty(input.date)) {
        isOk = false
        errors.date = "Vous devez choisir une date de transaction"
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

export default function FreezeTransactionForm({accounts, onSubmit}: Props) {
    const [inputTransaction, dispatch] = useReducer(reducer, initTransactionInput );
    const [errorInputTransaction, setErrorInputTransaction] = useState<TransactionInputError>(initTransactionInputError);

    function handleInputTransaction(event: any) {
        dispatch({
            type: 'update_field', 
            field: event.target.name, 
            value: event.target.value
        })
    }

    function handleSelectOption(name: any, value: any) {
        if (name === "account")
            dispatch({type: 'update_field', field: name, value: value})  
    }

    const displayAccount = (accountId: string) => {
        let acc = accounts.find((account) => account.accountId === accountId)
        if (acc) {
            return acc.name
        }
        return accountId
    }

    async function save() {
        try {
            const {isOk, errors} = verifyInput(inputTransaction)
            setErrorInputTransaction(errors)
            if (isOk) {
                await api.post(`/freeze-transactions`, inputTransaction)
                onSubmit()
            }
        } catch(error: any) {
            let resError: CallApiError = error
            alert(resError.message) 
        }
    }

    return (
        <div className="transaction-content">
            <div className='modal-body'>
                <div>
                    <InputDropDown 
                        type={'text'}
                        label={'Comptes'}
                        value={displayAccount(inputTransaction.account)}
                        name={'account'}
                        onChange={() => {}}
                        options={accounts.map(account => ({ value: account.accountId, displayValue: account.name }))}
                        onClickOption={handleSelectOption}
                        error={errorInputTransaction.account}
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
                </div>
                <div className='flex justify-around'>
                    <Button title={'Annuler'} backgroundColor={'#1E3050'} colorText={'white'} onClick={onSubmit} />
                    <Button title={'Geler'} backgroundColor={'#6755D7'} colorText={'white'} onClick={save} />
                </div>
            </div>
        </div>
    )
}