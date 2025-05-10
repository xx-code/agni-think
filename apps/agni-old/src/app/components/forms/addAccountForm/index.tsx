'use client'

import TextInput from "@/app/components/textInput";

import './index.css'
import { useReducer, useState } from "react";
import Button from "@/app/components/button";
import { isEmpty } from "@/_utils/isEmpty";
import { api, CallApiError } from "@/lib/api-clients";
import { Period } from "@/models/budget";


type AccountInput = {
    title: string,
    type: string
}

type AccountInputError = {
    title: string
    type: string
}

const initAccountInput: AccountInput = {
    title: '',
    type: ''
}

const initAccountInputError: AccountInputError = {
    title: '',
    type: ''
}


const verifyInput = (input: AccountInput): {isOk: boolean, errors: AccountInputError} => {
    let isOk = true 
    let errors: AccountInputError = initAccountInputError

    if(isEmpty(input.title)) {
        isOk = false
        errors.title = "Vous devez selectionner un compte"
    } 

    if(isEmpty(input.type)) {
        isOk = false
        errors.title = "Vous devez selectionner un type"
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

function reducer(state: AccountInput, action: ActionInput) {
    if (action.type === 'update_field') {
        return {
            ...state,
            [action.field]: action.value
        }
    }

    return state
}

type Props = {
    onSubmit: () => void
}

export default function AddAccountForm({onSubmit}: Props) {
    const [inputAccount, dispatch] = useReducer(reducer, initAccountInput );
    const [errorInputAccount, setErrorInputAccount] = useState<AccountInputError>(initAccountInputError);

    function handleInputAccount(event: React.ChangeEvent<HTMLInputElement>) {
        dispatch({
            type: 'update_field', 
            field: event.target.name, 
            value: event.target.value
        })
    }

    async function save() {
        try {
            const {isOk, errors} = verifyInput(inputAccount)
            setErrorInputAccount(errors)
            if (isOk) {
                await api.post(`/accounts`, inputAccount)
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
                    <TextInput 
                        type={'text'}
                        title={'Nom du compte'}
                        value={inputAccount.title}
                        name={'account'}
                        onChange={handleInputAccount}
                        error={errorInputAccount.title}
                    />
                </div>
                <div>
                    <p>Type Account: </p>
                    <div>
                        {
                            Object.values(Period).map((value, index) => {
                                return (
                                    <label key={index}>
                                        <input type="radio" name="type" value={value} onChange={handleInputAccount} defaultChecked={inputAccount.type===value} />
                                        Value
                                    </label>
                                )
                            })
                        }
                    </div>
                </div>
                <div className='flex justify-around'>
                    <Button title={'Annuler'} backgroundColor={'#1E3050'} colorText={'white'} onClick={onSubmit} />
                    <Button title={'Ajouter'} backgroundColor={'#6755D7'} colorText={'white'} onClick={save} />
                </div>
            </div>
        </div>
    )
}