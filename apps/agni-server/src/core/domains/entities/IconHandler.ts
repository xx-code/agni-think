class IconHandler {
    private id: string = ''
    private name: string = ''
    private value: string = ''
    private heberger: string = ''

    private change: boolean = false 
    constructor(id: string, name: string, heberger: string) {
        this.id = id
        this.name = name 
        this.heberger = heberger
    }

    setId(id: string) {
        this.id = id
    }

    getId(): string {
        return this.id 
    }

    setValue(value: string) {
        this.value = value
    }

    getValue(): string {
        return this.value
    }

    setName(name: string) {
        if (this.name !== name)
            this.change = true
        this.name = name
    }

    getName(): string {
        return this.name
    }

    setHeberger(heberger: string) {
        if (this.heberger !== heberger)
            this.change = true
        this.heberger = heberger
    }

    getHeberger(): string {
        return this.heberger
    }

    hasChange(): boolean {
        return this.change
    }
}