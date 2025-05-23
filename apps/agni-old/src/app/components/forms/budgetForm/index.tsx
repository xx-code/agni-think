'use client'

import InputDropDown from "@/app/components/inputDropdown"
import TextInput from "@/app/components/textInput"
import { useReducer, useState } from "react"
import Button from "@/app/components/button"
import { Category } from "@/models/category"
import { Tag as TagModel } from "@/models/tag"
import { BudgetDetail, convertStringToPeriod, Period } from "@/models/budget"
import { isEmpty } from "@/_utils/isEmpty"
import { api, CallApiError } from "@/lib/api-clients"
import Tag from "@/app/components/tag"

type BudgetInput = {
    title: string,
    category: string
    tag: string
    target: number
    dateStart: string
    period: string
    periodTime: number 
    dateEnd: string
    categoryIds: string[]
    tagIds: string[]
}

let initBudgetInput: BudgetInput =  {
    title: "",
    category: '',
    tag: '',
    target: 0,
    dateStart: "",
    period: "",
    periodTime: 0,
    dateEnd: "",
    categoryIds: [],
    tagIds: []
}

function createBudgetInputInitial(budget: BudgetDetail|undefined): BudgetInput {
    if (budget) {
        return {
            title: budget.title,
            category: '',
            tag: '',
            categoryIds: budget.categories.map(cat => cat.categoryId),
            tagIds: budget.tags.map(tag => tag.tagId),
            period: budget.period ? convertStringToPeriod(budget.period) : '',
            periodTime: budget.periodTime,
            dateEnd: budget.endDate ? budget.endDate : '',
            dateStart: budget.startDate,
            target: budget.target
        }
    }

    return initBudgetInput
}

type BudgetInputError = {
    title: string
    target: string
    category: string
    dateStart: string
    dateEnd: string
    period: string
    periodTime: string
    tag: string
}

const initBudgetInputError: BudgetInputError = {
    title: "",
    target: "",
    category: "",
    dateStart: "",
    dateEnd: "",
    period: "",
    periodTime: "",
    tag: ""
}

const verifyInput = (input: BudgetInput): {isOk: boolean, errors: BudgetInputError} => {
    let isOk = true;
    let errors: BudgetInputError = initBudgetInputError

    if (isEmpty(input.title)) {
        errors = {...errors, title: 'Ce champs ne doit pas etre vide'};
        isOk = false;
    } 

    if ((input.categoryIds.length === 0 && input.tagIds.length === 0) && isEmpty(input.dateEnd)) {
        errors = {...errors, category: 'Vous devez selection au moins une categorie'};
        isOk = false;
    }

    if (!isEmpty(input.period) && input.target <= 0) {
        errors = {...errors, periodTime: 'Ce champs doit etre superieur a zero'};
        isOk = false;
    }

    if (isEmpty(input.dateStart)) {
        errors = {...errors, dateStart: 'Ce champs ne doit pas etre vide'}
        isOk = false
    }


    if (input.target <= 0) {
        errors = {...errors, target: 'Doit etre superieur a 0'};
        isOk = false;
    } 

    return {
        isOk: isOk,
        errors: errors
    }
}

type ActionReducer = {
    type: 'update_field' | 'add_category' | 'remove_category' | 'add_tag' | 'remove_tag'
    field: string
    value: any
}

function reducer(state: BudgetInput, action: ActionReducer): any {
    
    if (action.type === 'update_field') {
        return {
            ...state,
            [action.field]: action.value
        }
    }

    if (action.type === 'add_category') {
        if (state.categoryIds.find(cat => cat === action.value) === undefined) {
            let categories = Object.assign([], state.categoryIds)
            categories.push(action.value)
            return {
                ...state,
                categoriesSelected: categories
            }
        }
    }

    if (action.type === 'remove_category') {
        let categories = Object.assign([], state.categoryIds)
        let indexCat = categories.findIndex(cat => cat === action.value)
        if (indexCat !== -1) {
            categories.splice(indexCat, 1)
            return {
                ...state,
                categoriesSelected: categories
            }
        }
    }

    if (action.type === 'add_tag') {
        if (state.tagIds.find(tag => tag === action.value) === undefined) {
            let tags = Object.assign([], state.tagIds)
            tags.push(action.value)
            return {
                ...state,
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
    currentBudget: undefined|BudgetDetail,
    categories: Category[],
    tags: TagModel[]
    onSave: () => void
}

export default function BudgetForm({currentBudget, categories, tags, onSave}: Props) {
    const [input, dispatch] = useReducer(reducer, currentBudget, createBudgetInputInitial)
    const [error, setError] = useState<BudgetInputError>(initBudgetInputError) 

    function handleInput(e: React.ChangeEvent<HTMLInputElement>) {
        dispatch({
            type: 'update_field',
            field: e.target.name,
            value: e.target.value
        })
    }

    function handleOnClickOption(name: string, value: string) {
        if (name === 'category')
            dispatch({type: 'add_category', field: '', value: value})

        if (name === 'tag')
            dispatch({type: 'add_tag', field: '', value: value})

        if (name === 'period') {
            if (!isEmpty(value)) {
                dispatch({type: 'update_field', field: name, value: Object.values(Period).find(val => val === value)})
            } else {
                dispatch({type: 'update_field', field: name, value: ''})
            }
        }
            
    }

    function handleOnClickDeleteSelection(name: string, value: string) {
        if (name === 'category')
            dispatch({type: 'remove_category', field: '', value: value})

        if (name === 'tag')
            dispatch({type: 'remove_tag', field: '', value: value})
    }

    async function save() {
        try {
            let {isOk, errors} = verifyInput(input)
            setError(errors)
            if (isOk) {
                await api.post(`/budgets`, input);

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
                await api.put(`/budgets/${currentBudget!.id}`, input);

                onSave()
            }
        } catch(error: any) {
            let resError: CallApiError = error
            alert(resError.message) 
        }
    }

    const displayCategory = (catId: string) => {
        let category = categories.find((category) => category.categoryId === catId)
        if (category) {
            return category.name
        }
        return ""
    }

    const displayTag = (tagId: string) => {
        let tag = tags.find((tag) => tag.tagId === tagId)
        if (tag) {
            return tag.value
        }
        return ""
    }

    return (
        <div className="add-budget-modal">        
            <TextInput 
                title="Nom budget" 
                name="title" 
                type="text" 
                value={input.title} 
                onChange={handleInput} 
                error={error.title} 
            />
            <InputDropDown 
                label="Categorie" 
                name="category" 
                type="text" 
                value={input.category} 
                onChange={handleInput} 
                options={categories.map(category => ({displayValue: category.name, value: category.categoryId}))} 
                onClickOption={handleOnClickOption} 
                error={error.category}  
                overOnBlur={undefined} 
                placeholder=""
                remover={false}
            /> 
            <div className="flex flex-wrap" style={{marginTop: "-12px"}}>
                {
                    input.categoryIds.map((category: string, key: any) => 
                        <Tag key={key} title={displayCategory(category)} onDelete={() => handleOnClickDeleteSelection('category', category)} color={undefined}/>
                    )
                }
            </div>
            <TextInput title="Date" name="dateStart" type="date" value={input.dateStart} onChange={handleInput} error={error.dateStart} />
    
            <InputDropDown 
                label="Periode"
                name="period"
                type="text"
                value={input.period}
                onChange={() => {}}
                error={error.period} 
                placeholder="" 
                options={Object.values(Period).map(period => ({displayValue: period, value: period }))} 
                onClickOption={handleOnClickOption} 
                overOnBlur={undefined} 
                remover
            />
            {
                !isEmpty(input.period) ?
                    <div style={{width: '45%'}}>
                        <TextInput title="Nb Periode" name="periodTime" type="number" value={input.periodTime} onChange={handleInput} error={error.periodTime} />  
                    </div>
                :
                    <></>
            }
            
            
            <div style={{width: '60%'}}>
                <TextInput title="Cible" name="target" type="number" value={input.target} onChange={handleInput} error={error.target} />  
            </div>

            <InputDropDown 
                label="Tag"
                name="tag"
                type="text"
                value={input.tag}
                onChange={handleInput}
                options={tags.map((tag) => ({ displayValue: tag.value, value: tag.tagId }))}
                onClickOption={handleOnClickOption}
                error={error.tag}
                overOnBlur={() => { } } 
                placeholder=""     
                remover={false}               
            /> 
            <div className="flex flex-wrap" style={{marginTop: "-12px"}}>
                {
                    input.tagIds.map((tag: string, key: any) => 
                        <Tag key={key} title={displayTag(tag)} onDelete={() => handleOnClickDeleteSelection('tag', tag)} color={undefined}/>
                    )
                }
            </div>
            <TextInput 
                title="Date de fin" 
                name="dateEnd" 
                type="date" 
                value={input.dateEnd} 
                onChange={handleInput} 
                error={error.dateEnd} 
            />
            <div className="flex justify-center">
                <Button 
                    title={currentBudget ? "Modifier Budget" : "Creer Budget"} 
                    onClick={currentBudget ? update: save} 
                    backgroundColor="#6755D7" 
                    colorText="white" 
                />
            </div> 
        </div>
    )
}