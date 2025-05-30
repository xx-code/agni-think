import { ValueError } from "@core/errors/valueError";

export class Transaction {
    private id: string;
    private accountRef: string
    private tagRefs: string[]
    private categoryRef: string
    private recordRef: string
    private isFreeze: boolean
    private date: string

    private change: boolean = false

    __add_event_tag: string[] = []
    __delete_event_tag: string[] = []

    constructor(id: string, accountRef: string, recordRef: string, categoryRef: string,  date: string, tagRefs: string[]=[]) {
        this.id = id 
        this.accountRef = accountRef
        this.recordRef = recordRef
        this.tagRefs = tagRefs
        this.categoryRef = categoryRef
        this.isFreeze = false
        this.date = date
    }

    setId(id: string) {
        this.id = id
    }

    getId(): string {
        return this.id
    }

    setTags(tagRefs: string[]) {
        // Check version
        let tag_to_add = tagRefs.filter(tag => this.tagRefs.findIndex(el => el === tag) === -1)
        let tag_to_delete = this.tagRefs.filter(tag => tagRefs.findIndex(compTag => compTag === tag) === -1)

        if (tag_to_add.length > 0 || tag_to_delete.length > 0) {
            this.__add_event_tag = tag_to_add
            this.__delete_event_tag = tag_to_delete

            this.change = true
            this.tagRefs = tagRefs
        }
    }
    
    addTag(tag: string) {
        if (this.tagRefs.includes(tag))
            throw new ValueError('Tag already exist, in transaction. Not duplicate allow.')
        this.__add_event_tag.push(tag)
        this.change = true
        this.tagRefs.push(tag)
    }

    deleteTag(tag: string) {
        let index_tag = this.tagRefs.indexOf(tag)
        if (index_tag < 0)
            throw new ValueError('Tag do not exist, in Transaction.')
        this.__delete_event_tag.push(tag)
        this.change = true
        this.tagRefs.splice(index_tag, 1)
    }

    getTags(): string[] {
        return this.tagRefs
    }

    setAccountRef(accountRef: string) {
        if (this.accountRef !== accountRef)
            this.change = true
        this.accountRef = accountRef
    }

    getAccountRef(): string {
        return this.accountRef
    }

    setCategoryRef(categoryRef: string) {
        if (this.categoryRef !== categoryRef)
            this.change = true 
        this.categoryRef = categoryRef
    }

    setIsFreeze() {
        this.isFreeze = true
    }

    getIsFreeze(): boolean {
        return this.isFreeze
    }

    getCategoryRef(): string {
        return this.categoryRef
    }

    setRecordRef(recordRef: string) {
        if (this.recordRef !== recordRef)
            this.change = true 
        this.recordRef = recordRef
    }

    getRecordRef(): string {
        return this.recordRef
    }

    setDate(date: string) {
        if (this.date !== date) 
            this.date = date
        this.date = date
    }

    getDate(): string {
        return this.date
    }

    hasChange(): boolean {
        return this.change
    }
}
