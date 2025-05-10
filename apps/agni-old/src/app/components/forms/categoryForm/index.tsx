'use client'

import TextInput from "@/app/components/textInput"
import { useReducer, useState } from "react"
import Button from "@/app/components/button"
import { Category } from "@/models/category"
import { isEmpty } from "@/_utils/isEmpty"
import { api, CallApiError } from "@/lib/api-clients"

type CategoryInput = {
    id: string
    title: string,
    color: string
    icon: string
}

let initCategoryInput: CategoryInput =  {
    id: "",
    title: "",
    color: "",
    icon: ""
}

function createCategoryInputInitial(category: Category|undefined): CategoryInput {
    if (category) {
        return {
            id: category.categoryId,
            title: category.name,
            color: category.color ? category.color : "",
            icon: category.icon
        }
    }

    return initCategoryInput
}

type CategoryInputError = {
    title: string
    color: string
    icon: string
}

const initCategoryInputError: CategoryInputError = {
    title: "",
    color:"",
    icon: ""
}

const verifyInput = (input: CategoryInput): {isOk: boolean, errors: CategoryInputError} => {
    let isOk = true;
    let errors: CategoryInputError = initCategoryInputError

    if (isEmpty(input.title)) {
        errors = {...errors, title: 'Ce champs ne doit pas etre vide'};
        isOk = false;
    } 

    if (isEmpty(input.icon)) {
        errors = {...errors, icon: 'Ce champs ne doit pas etre vide'}
        isOk = false
    } 

    return {
        isOk: isOk,
        errors: errors
    }
}

type ActionReducer = {
    type: string
    field: string
    value: any
}

function reducer(state: CategoryInput, action: ActionReducer): any {
    
    if (action.type === 'update_field') {
        return {
            ...state,
            [action.field]: action.value
        }
    }

    return state
}

type Props = {
    currentCategory: undefined|Category,
    onSave: () => void
}

export default function CategoryForm({currentCategory, onSave}: Props) {
    const [input, dispatch] = useReducer(reducer, currentCategory, createCategoryInputInitial)
    const [error, setError] = useState<CategoryInputError>(initCategoryInputError) 

    function handleInput(e: React.ChangeEvent<HTMLInputElement>) {
        dispatch({
            type: 'update_field',
            field: e.target.name,
            value: e.target.value
        })
    }

    async function save() {
        try {
            let {isOk, errors} = verifyInput(input)
            setError(errors)
            if (isOk) {
                await api.post(`/categories`, input);

                onSave()
            } 
        } catch(error: any) {
            let resError: CallApiError = error
            alert(resError.message)
        }
    }

    async function update() {
        try {
            let {isOk, errors} = verifyInput(input)
            setError(errors)
            if (isOk) {
                await api.put(`/categories/${currentCategory!.categoryId}`, input);

                onSave()
            }
        } catch(error: any) {
            let resError: CallApiError = error
            alert(resError.message) 
        }
    }

    return (
        <div className="add-budget-modal">        
            <TextInput 
                title="Description" 
                name="title" 
                type="text" 
                value={input.title} 
                onChange={handleInput} 
                error={error.title} 
            />
            <TextInput 
                title="Icon" 
                name="icon" 
                type="text" 
                value={input.icon} 
                onChange={handleInput} 
                error={error.icon} 
            />
            <TextInput 
                title="Couleur" 
                name="color" 
                type="text" 
                value={input.color} 
                onChange={handleInput} 
                error={error.color} 
            />
            
            <div className="flex justify-center">
                <Button 
                    title={currentCategory ? "Modifier Categorie" : "Creer Categorie"} 
                    onClick={currentCategory ? update: save} 
                    backgroundColor="#6755D7" 
                    colorText="white" 
                />
            </div> 
        </div>
    )
}