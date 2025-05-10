'use client'


import './index.css'
import { useReducer, useState } from "react";
import Button from "@/app/components/button";
import InputDropDown from "../../inputDropdown";
import { api, CallApiError } from "@/lib/api-clients";
import { isEmpty } from "@/_utils/isEmpty";
import { Account } from '@/models/account';
;

type DeleteSaveGoalInput = {
    accountDepositId: string
}

type DeleteSaveGoalError = {
    account: string
}

const initAccountInput: DeleteSaveGoalInput = {
    accountDepositId: '',
}

const initAccountInputError: DeleteSaveGoalError = {
    account: '',
}


const verifyInput = (input: DeleteSaveGoalInput): {isOk: boolean, errors: DeleteSaveGoalError} => {
    let isOk = true 
    let errors: DeleteSaveGoalError = initAccountInputError

    if(isEmpty(input.accountDepositId)) {
        isOk = false
        errors.account = "Vous devez selectionner un compte"
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

function reducer(state: DeleteSaveGoalInput, action: ActionInput) {
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
    saveGoalId: string
    onSubmit: () => void
}

export default function DeleteSaveGoalForm({onSubmit, accounts, saveGoalId}: Props) {
    const [inputAccount, dispatch] = useReducer(reducer, initAccountInput );
    const [errorInputAccount, setErrorInputAccount] = useState<DeleteSaveGoalError>(initAccountInputError);

    
    function handleSelectOption(name: any, value: any) {
        if (name === "account")
            dispatch({type: 'update_field', field: name, value: value})     
    }

    async function save() {
        try {
            const {isOk, errors} = verifyInput(inputAccount)
            setErrorInputAccount(errors)
            if (isOk) {
                await api.delete(`/save-goals`, {
                    data: inputAccount
                })
                onSubmit()
            }
        } catch(error: any) {
            let resError: CallApiError = error
            alert(resError.message) 
        }
    }

    const displayAccount = (accountId: string) => {
        let acc = accounts.find((account) => account.accountId === accountId)
        if (acc) {
            return acc.name
        }
        return accountId
    }
    
    return (
        <div className="transaction-content">
            <div className='modal-body'>
                <div>
                    <InputDropDown 
                        type={'text'}
                        label={'Comptes'}
                        value={displayAccount(inputAccount.accountDepositId)}
                        name={'account'}
                        onChange={() => {}}
                        options={accounts.map(account => ({ value: account.accountId, displayValue: account.name }))}
                        onClickOption={handleSelectOption}
                        error={errorInputAccount.account}
                        overOnBlur={undefined} 
                        placeholder={""} 
                        remover={false}
                    />
                </div>
                <div className='flex justify-around'>
                    <Button title={'Annuler'} backgroundColor={'#1E3050'} colorText={'white'} onClick={onSubmit} />
                    <Button title={'Ajouter'} backgroundColor={'#6755D7'} colorText={'white'} onClick={save} />
                </div>
            </div>
        </div>
    )
}