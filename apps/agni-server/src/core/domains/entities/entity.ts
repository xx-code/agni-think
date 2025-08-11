import { IObjectEquality } from '@core/domains/interface/equality';
import { stringify } from 'querystring';

export default abstract class Entity implements IObjectEquality {
    private id: string;
    private change: boolean;

    private createdAt: Date;
    private updatedAt: Date;

    constructor(id: string) {
        this.id = id;
        this.createdAt = new Date(Date.now());
        this.updatedAt = new Date(Date.now());
        this.change = false;
    }

    isEqual(object: Entity): boolean {
        if (object === null || object === undefined) return false;

        if (this.constructor !== object.constructor) return false;

        return this.id === object.getId(); 
    };

    public getId(): string {
        return this.id; 
    }

    protected setId(id: string) {
        this.id = id;
    }
    
    protected markHasChange() {
        this.change = true;
        this.updatedAt = new Date(Date.now());
    }

    public hasChange(): boolean {
        return this.change;
    }

    public resetChangeState() {
        this.change = false;
    }

    public getCreatedAt(): Date {
        return this.createdAt; 
    }
    
    public setCreatedAt(date: Date) {
        this.createdAt = date;
    }

    public getUpdatedAt(): Date {
        return this.updatedAt;
    }

    public setUpdatedAt(date: Date) {
        this.updatedAt = date;
    }
}

export class TrackableProperty<T> {
    private value: T;
    private onChange: () => void;

    constructor(value: T, onChange: () => void) {
        this.value = value;
        this.onChange = onChange;
    }

    public get(): T {
        return this.value;
    }

    public set(newValue: T, isDifferent: (a: T, b: T) => boolean = (a, b) => a !== b) {
        if (isDifferent(this.value, newValue)) {
            this.onChange() 
        }
        this.value = newValue;
    }
}

