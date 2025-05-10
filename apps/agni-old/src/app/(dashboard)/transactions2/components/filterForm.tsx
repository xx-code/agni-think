'use client'

import Button from "@/app/components/button"
import InputDropDown from "@/app/components/inputDropdown"
import { useReducer, useState } from "react"
import TextInput from "@/app/components/textInput"
import { Category } from "@/models/category"
import { Tag as TagModel } from "@/models/tag"
import { searchInArr } from "@/_utils/searchInArr"
import Tag from "@/app/components/tag"

export type FilterInput = {
    category: string 
    tag: string
    dateStart: string
    dateEnd: string
    categoriesSelected: string[]
    tagsSelected: string[]
}

const iniFilterInput: FilterInput = {
    category: '',
    tag: '',
    dateStart: '',
    dateEnd: '',
    categoriesSelected: [],
    tagsSelected: []
}

export type ActionType = {
    type: string
    field: string
    value: string
}

function reducer(state: FilterInput, action: ActionType) {
    if (action.type === 'update_field') {
        return {
            ...state,
            [action.field]: action.value
        }
    }

    if (action.type === 'add_category') {
        if (state.categoriesSelected.find(cat => cat === action.value) === undefined) {
            let categories = Object.assign([], state.categoriesSelected)
            categories.push(action.value)
            return {
                ...state,
                categoriesSelected: categories
            }
        }
    }

    if (action.type === 'remove_category') {
        let categories = Object.assign([], state.categoriesSelected)
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
        let tags = Object.assign([], state.tagsSelected)
        if (state.tagsSelected.find(tag => tag === action.value) === undefined) {
            tags.push(action.value)
            return {
                ...state,
                tagsSelected: tags
            }
        } 
    }

    if (action.type === 'remove_tag') {
        let tags = Object.assign([], state.tagsSelected)
        let indexTag = tags.findIndex(tag => tag === action.value)
        if (indexTag !== -1) {
            tags.splice(indexTag, 1)
            return {
                ...state,
                tagsSelected: tags
            }
        }
    }

    if (action.type === 'clean') {
        return iniFilterInput
    }

    return state
}

type Props = {
    categories: Category[]
    tags: TagModel[]
    onChangeFilter: (filter: FilterInput) => void
    onCleanFilter: (filter: FilterInput) => void 
}

export function FilterForm({ onChangeFilter, onCleanFilter, categories, tags }: Props) {
    const [filterInput, dispatch] = useReducer(reducer, iniFilterInput)

    const [isUpdate, setUpdate] = useState(false)
    const [searchTags, setSearchTag] = useState(tags)
    const [searchCategories, setSearchCategories] = useState(categories)

    function handleInputFilter(event: any) {
        if (event.target.name === 'tag') {
            let tagsFound = searchInArr(event.target.value, tags.map(tag => tag.value));
            setSearchTag(tags.filter(tag => tagsFound.includes(tag.value)));
        }
    
        if (event.target.name === 'category') {
            let categoriesFound = searchInArr(event.target.value, categories.map(category => category.name));
            setSearchCategories(categories.filter(category => categoriesFound.includes(category.name)));
        }

        if (event.target.name === "dateEnd" || event.target.name === "dateStart")
            setUpdate(true)
    
        dispatch({
            type: 'update_field', 
            field: event.target.name, 
            value: event.target.value
        })
    }

    function handleOnClickOption(name: string, value: string) {
        if (name === 'category')
            dispatch({type: 'add_category', field: '', value: value})

        if (name === 'tag')
            dispatch({type: 'add_tag', field: '', value: value})

        setUpdate(true)
    }

    function handleOnClickDeleteSelection(name: string, value: string) {
        if (name === 'category')
            dispatch({type: 'remove_category', field: '', value: value})

        if (name === 'tag')
            dispatch({type: 'remove_tag', field: '', value: value})

        setUpdate(true)
    }

    function cleanFilter() {
        dispatch({type: 'clean', field: '', value: ''})
        onCleanFilter(iniFilterInput)
        setUpdate(false)
    }

    const displayCategory = (categoryId: string) => {
        let category = categories.find((category) => category.categoryId === categoryId)
        if (category) {
            return category.name
        }
        return categoryId
    }

    const displayTag = (tagId: string) => {
        let tag = tags.find((tag) => tag.tagId === tagId)
        if (tag) {
            return tag.value
        }
        return tagId
    }

    const handleValid = () => {
        onChangeFilter(filterInput)
        setUpdate(false)
    }

    return (
        <div className="filter-part">
            <div className="filter-part-content">
                <div className="filter-title-content">
                    <h3>Filtrage</h3>
                    <Button backgroundColor="transparent" colorText="#6755D7" title="reintialise" onClick={cleanFilter}/>
                </div>
                <div className="filter-dropdown-content">
                    <InputDropDown 
                        type="text"
                        label={"Categorie"}
                        value={filterInput.category}
                        name="category"
                        onChange={handleInputFilter}
                        options={searchCategories.map(cat => ({value: cat.categoryId, displayValue: cat.name}))}
                        onClickOption={handleOnClickOption}
                        error={null}
                        overOnBlur={undefined} 
                        placeholder={""} 
                        remover={false}                    
                    />
                    <div className="flex flex-wrap">
                        {
                            filterInput.categoriesSelected.map((category_id, index) => <Tag key={index} title={displayCategory(category_id)} onDelete={() => handleOnClickDeleteSelection('category', category_id)} color={undefined}/> )
                        }
                    </div>
                </div>
                <div className="filter-dropdown-content">
                    <InputDropDown 
                        type="text"
                        label={"Tag"}
                        value={filterInput.tag}
                        name={"tag"}
                        onChange={handleInputFilter}
                        options={searchTags.map((tag) => ({value: tag.tagId, displayValue: tag.value}))}
                        onClickOption={handleOnClickOption}
                        error={null}
                        overOnBlur={undefined} 
                        placeholder={""} 
                        remover={false}                    
                    />
                    <div className="flex flex-wrap">
                        {
                            filterInput.tagsSelected.map((tag, index) => <Tag key={index} title={displayTag(tag)} onDelete={() => handleOnClickDeleteSelection('tag', tag)} color={undefined}/> )
                        }
                    </div>
                </div>
                <div>
                    <TextInput 
                        type={"date"} 
                        title={"Date de debut"} 
                        value={filterInput.dateStart} 
                        name={"dateStart"} 
                        onChange={handleInputFilter} 
                        error={null} 
                    />
                    <TextInput 
                        type={"date"} 
                        title={"Date de fin"} 
                        value={filterInput.dateEnd} 
                        name={"dateEnd"} 
                        onChange={handleInputFilter} 
                        error={null} 
                    />
                </div>
                <div>
                    {
                        isUpdate ? 
                            <Button title={"Valider"} backgroundColor={"#6755D7"} colorText={"white"} onClick={handleValid} />
                            :
                            <></>
                    }
                    
                </div>
            </div>
        </div>
    )
}
