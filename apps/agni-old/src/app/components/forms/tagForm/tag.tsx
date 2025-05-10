'use client'

import TextInput from "@/app/components/textInput"
import { useReducer, useState } from "react"
import Button from "@/app/components/button"
import { Tag } from "@/models/tag"
import { isEmpty } from "@/_utils/isEmpty"
import { api, CallApiError } from "@/lib/api-clients"

type TagInput = {
    id: string
    value: string,
    color: string
}

let initTagInput: TagInput =  {
    id: "",
    value: "",
    color: "",
}

function createTagInputInitial(Tag: Tag|undefined): TagInput {
    if (Tag) {
        return {
            id: Tag.tagId,
            value: Tag.value,
            color: Tag.color ? Tag.color : ""
        }
    }

    return initTagInput
}

type TagInputError = {
    title: string
    color: string
}

const initTagInputError: TagInputError = {
    title: "",
    color:"",
}

const verifyInput = (input: TagInput): {isOk: boolean, errors: TagInputError} => {
    let isOk = true;
    let errors: TagInputError = initTagInputError

    if (isEmpty(input.value)) {
        errors = {...errors, title: 'Ce champs ne doit pas etre vide'};
        isOk = false;
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

function reducer(state: TagInput, action: ActionReducer): any {
    
    if (action.type === 'update_field') {
        return {
            ...state,
            [action.field]: action.value
        }
    }

    return state
}

type Props = {
    currentTag: undefined|Tag,
    onSave: () => void
}

export default function TagForm({currentTag, onSave}: Props) {
    const [input, dispatch] = useReducer(reducer, currentTag, createTagInputInitial)
    const [error, setError] = useState<TagInputError>(initTagInputError) 

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
                await api.post(`/tags`, input);

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
                await api.put(`/tags/${currentTag!.tagId}`, input);

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
                value={input.value} 
                onChange={handleInput} 
                error={error.title} 
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
                    title={currentTag ? "Modifier Tag" : "Creer Tag"} 
                    onClick={currentTag ? update: save} 
                    backgroundColor="#6755D7" 
                    colorText="white" 
                />
            </div> 
        </div>
    )
}