'use client'

import TextInput from "@/app/components/textInput";

import './index.css'
import { useReducer, useState } from "react";
import Button from "@/app/components/button";
import InputDropDown from "@/app/components/inputDropdown";
import Tag from "@/app/components/tag";
import axios from "axios";
import { isEmpty } from "@/_utils/isEmpty";
import { searchInArr } from "@/_utils/searchInArr";
import { Transaction, TransactionType } from "@/models/transaction";
import { Account } from "@/models/account";
import { Category } from "@/models/category";
import { Tag as TagModel } from "@/models/tag";

type TransactionInput = {
    accountId: string
    amount: number
    description: string 
    categoryId: string 
    date: string 
    typeTransaction: string
    tagIds: string[]
    tag: string
}

type TransactionInputError = {
    account: string
    amount: string
    description: string
    category: string
    tag: string
    type: string
    date: string
}

const initTransactionInput: TransactionInput = {
    accountId: '',
    amount: 0,
    description: '',
    categoryId: '',
    date: '',
    tag: '',
    typeTransaction: 'Credit',
    tagIds: []
}

const initTransactionInputError: TransactionInputError = {
    account: '',
    amount: '',
    description: '',
    category: '',
    date: '',
    tag: '',
    type: ''
}

function createTransactionInputInitial(transaction: Transaction|undefined): TransactionInput {
    if (transaction) {
        return {
            accountId: transaction.accountId,
            categoryId: transaction.category.categoryId,
            description: transaction.description,
            date: transaction.date,
            amount: transaction.amount,
            tag: '',
            tagIds: transaction.tags.map(tag => tag.tagId),
            typeTransaction: transaction.type
        }
    }

    return initTransactionInput
}

const verifyInput = (input: TransactionInput): {isOk: boolean, errors: TransactionInputError} => {
    let isOk = true 
    let errors: TransactionInputError = initTransactionInputError

    if(isEmpty(input.accountId)) {
        isOk = false
        errors.account = "Vous devez selectionner un compte"
    } 

    if (input.amount <= 0) {
        isOk = false
        errors.amount = "Le prix doit etre superieur a 0"
    }

    if (isEmpty(input.categoryId)) {
        isOk = false 
        errors.category = "Vous devez selectionner une categorie"
    }

    if (isEmpty(input.description)) {
        isOk = false 
        errors.description = "Ajouter une description a la transaction"
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

    if (action.type === 'add_tag') {
        let tags = Object.assign([], state.tagIds)
        if (state.tagIds.find(tag => tag === action.value) === undefined) {
            tags.push(action.value)
            return {
                ...state,
                tag: '',
                tagsSelected: tags
            }
        } 
    }

    if (action.type === 'remove_tag') {
        let tags = Object.assign([], state.tagIds)
        let indexTag = tags.findIndex(tag => tag === action.value)
        if (indexTag !== -1) {
            tags.splice(indexTag, 1)
            return {
                ...state,
                tagsSelected: tags
            }
        }
    }

    return state
}

type Props = {
    accounts: Account[]
    categories: Category[]
    tags: TagModel[]
    transaction: Transaction | undefined
    onSubmit: () => void
}

export default function TransactionForm({accounts, categories, tags, transaction, onSubmit}: Props) {
    const [inputTransaction, dispatch] = useReducer(reducer, transaction, createTransactionInputInitial );
    const [errorInputTransaction, setErrorInputTransaction] = useState<TransactionInputError>(initTransactionInputError);

    const [searchTags, setSearchTag] = useState(tags)
    const [searchCategories, setSearchCategories] = useState(categories)

    function handleInputTransaction(event: any) {
        if (event.target.name === 'tag') {
            let tagsFound = searchInArr(event.target.value, tags.map(tag => tag.value));
            setSearchTag(tags.filter(tag => tagsFound.includes(tag.value)));
        }
  
        if (event.target.name === 'category') {
            let categoriesFound = searchInArr(event.target.value, categories.map(category => category.name));
            setSearchCategories(categories.filter(category => categoriesFound.includes(category.name)));
        }
  
        dispatch({
            type: 'update_field', 
            field: event.target.name, 
            value: event.target.value
        })
    }

    function handleSelectOption(name: any, value: any) {
        if (name === "account")
            dispatch({type: 'update_field', field: name, value: value})

        if (name === "category") {
            dispatch({type: 'update_field', field: name, value: value})
            setSearchCategories(categories)
        }

        if (name === "tag") {
            dispatch({type: "add_tag", value: value, field: ''})
            setSearchTag(tags)
        }

        if (name === "blur_tag") {
            if (!isEmpty(value)) {
                let tagFound = tags.find(tag => tag.value.toLowerCase() === value.toLowerCase())
                
                let tagValue = value 
                if (tagFound)
                    tagValue = tagFound.tagId

                dispatch({type: "update_field", value: "", field: 'tag'})
                dispatch({type: "add_tag", value: tagValue, field: ''})
                setSearchTag(tags)
            }
        }
            
    }
  
    function handleDeleteOption(name: string, id: string) {
        if (name === 'tag')
            dispatch({type: 'remove_tag', field: '', value: id})
    }

    const displayAccount = (accountId: string) => {
        let acc = accounts.find((account) => account.accountId === accountId)
        if (acc) {
            return acc.name
        }
        return accountId
    }

    const displayCategory = (categoryId: string) => {
        let category = categories.find((category) => category.categoryId === categoryId)
        if (category) {
            return category.name
        }
        return category
    }

    const displayTag = (tagId: string) => {
        let tag = tags.find((tag) => tag.tagId === tagId)
        if (tag) {
            return tag.value
        }
        return tagId
    }

    async function save() {
        try {
            const {isOk, errors} = verifyInput(inputTransaction)
            setErrorInputTransaction(errors)
            if (isOk) {
                await axios.post(`/transactions`, inputTransaction)
                onSubmit()
            }
        } catch(err: any) {
            alert(err)
        }
    }

    async function update() {
        try {
            const {isOk, errors} = verifyInput(inputTransaction)
            setErrorInputTransaction(errors)
            if (isOk) {
                await axios.put(`/transactions/${transaction!.transactionId}`, inputTransaction)
                onSubmit()
            }
        } catch(err: any) {
            alert(err)
        }
    }
    
    return (
        <div className="transaction-content">
            <div className='nav-modal-add-new-transaction flex'>
                <div className={inputTransaction.typeTransaction === TransactionType.CREDIT ? 'nav-btn is-active-nav-btn' : 'nav-btn'} onClick={() => dispatch({type: 'update_field', field: 'type', value: TransactionType.CREDIT})} >
                    Gains
                </div>
                <div className={inputTransaction.typeTransaction === TransactionType.DEBIT ? 'nav-btn is-active-nav-btn' : 'nav-btn'} onClick={() => dispatch({type: 'update_field', field: 'type', value: TransactionType.DEBIT})}>
                    Depense
                </div>
            </div>
            <div className='modal-body'>
                <div>
                    <InputDropDown 
                        type={'text'}
                        label={'Comptes'}
                        value={displayAccount(inputTransaction.accountId)}
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
                        type={'text'} 
                        title={'Description'} 
                        value={inputTransaction.description} 
                        name={'description'} 
                        onChange={handleInputTransaction} 
                        error={errorInputTransaction.description} 
                    />
                    <InputDropDown 
                        type={'text'}
                        label={'Categorie'}
                        value={displayCategory(inputTransaction.categoryId)}
                        name={'category'}
                        onChange={handleInputTransaction}
                        options={searchCategories.map(cat => ({ displayValue: cat.name, value: cat.categoryId }))}
                        onClickOption={handleSelectOption}
                        error={errorInputTransaction.category}
                        overOnBlur={undefined} 
                        placeholder={""} 
                        remover={false}                        
                    />
                    <TextInput 
                        type={'date'} 
                        title={'date'} 
                        value={inputTransaction.date} 
                        name={'date'} 
                        onChange={handleInputTransaction}  
                        error={errorInputTransaction.date} 
                    />
                    <InputDropDown 
                        type={'text'}
                        label={'tag'}
                        value={inputTransaction.tag}
                        name={'tag'}
                        onChange={handleInputTransaction}
                        options={searchTags.map(tag => ({ displayValue: tag.value, value: tag.tagId }))}
                        onClickOption={handleSelectOption}
                        error={errorInputTransaction.tag}
                        overOnBlur={() => handleSelectOption('blur_tag', inputTransaction.tag)} 
                        placeholder={""} 
                        remover={false}                        
                    />
                    <div className='flex flex-wrap' style={{marginBottom: '1em'}}>
                        {
                            inputTransaction.tagIds.map((tag, index) => (
                                <Tag 
                                    key={index} 
                                    title={displayTag(tag)} 
                                    onDelete={() => handleDeleteOption('tag', tag)} 
                                    color={undefined} />
                            ))
                        }
                    </div>
                </div>
                <div className='flex justify-around'>
                    <Button title={'Annuler'} backgroundColor={'#1E3050'} colorText={'white'} onClick={onSubmit} />
                    <Button title={ transaction !== undefined ? 'Modifier' : 'Ajouter'} backgroundColor={'#6755D7'} colorText={'white'} onClick={transaction !== undefined ? update : save} />
                </div>
            </div>
        </div>
    )
}