import { ValueError } from "@core/errors/valueError"

export class DeductionType {
    private id: string
    private title: string
    private description: string
    private change: boolean = false

    constructor(id: string, title: string, description: string) {
        this.id = id
        this.title = title 
        this.description = description
    }

    getId(): string {
        return this.id 
    }
    
    setId(id: string) {
        this.id = id
    }

    setTitle(title: string) {
        if (this.title !== title)
            this.change = true
        this.title = title
    }

    getTitle(): string {
        return this.title
    }

    setDescription(description: string) { 
        if (this.description !== description)
            this.change = true
        this.description = description
    }

    getDescription(): string {
        return this.description
    }

    hasChange(): boolean {
        return this.change
    }
}

export class Deduction { 
    private id: string
    private deductionType: DeductionType
    private rate: number
    private change: boolean = false

    constructor(id: string, deductionType: DeductionType, rate: number) {
        this.id = id 
        this.deductionType = deductionType
        
        if (rate <= 0)
            throw new ValueError("Rate must be great than 0")

        this.rate = rate
    }

    setId(id: string) {
        this.id = id
    }

    getId(): string {
        return this.id
    }

    setDeductionType(deductionType: DeductionType) {
        if (this.deductionType !== deductionType)
            this.change = true
        this.deductionType = deductionType
    }

    getDeductionType(): DeductionType {
        return this.deductionType
    }

    setRate(rate: number) {
        if (rate <= 0)
            throw new ValueError("Rate must be great than 0")

        if (this.rate !== rate)
            this.change = true
        
        this.rate = rate
    }

    getRate(): number {
        return this.rate
    }

    hasChange(): boolean {
        return this.change
    }
}
