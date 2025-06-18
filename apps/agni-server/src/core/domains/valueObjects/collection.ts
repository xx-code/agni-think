import { ValueError } from "@core/errors/valueError";
import ValueObject from "./valueObject";

export class ValueObjectCollection<T extends ValueObject> {
    private collection: T[];

    public __added_object: T[] = [];
    public __deleted_object: T[] = [];

    private onChange?: () => void;

    constructor(init: T[] = [], onChange?: () => void) {
        this.collection = init;
        this.onChange = onChange;
    }

    public set(newCollection: T[]): void {
        this.__added_object = newCollection.filter(
            item => !this.collection.some(el => el.isEqual(item))
        );

        this.__deleted_object = this.collection.filter(
            item => !newCollection.some(el => el.isEqual(item))
        );

        if (this.__added_object.length > 0 || this.__deleted_object.length > 0) {
            this.collection = newCollection;
            this.onChange?.();
        }
    }

    public add(newValue: T): void {
        if (this.collection.some(val => val.isEqual(newValue))) {
            throw new ValueError("VALUE_ALREADY_EXIST");
        }

        this.collection.push(newValue);
        this.__added_object.push(newValue);
        this.onChange?.();
    }

    public update(newValue: T): void {
        const index = this.collection.findIndex(val => val.isEqual(newValue));
        if (index === -1) {
            throw new ValueError("VALUE_NOT_FOUND");
        }

        const oldValue = this.collection[index];
        this.collection[index] = newValue;

        this.__deleted_object.push(oldValue);
        this.__added_object.push(newValue);
        this.onChange?.();
    }

    public delete(value: T): void {
        const index = this.collection.findIndex(val => val.isEqual(value));
        if (index === -1) {
            throw new ValueError("VALUE_NOT_FOUND");
        }

        const [removed] = this.collection.splice(index, 1);
        this.__deleted_object.push(removed);
        this.onChange?.();
    }

    public get(): T[] {
        return [...this.collection];
    }

    public clearChanges(): void {
        this.__added_object = [];
        this.__deleted_object = [];
    }

    public getChanges(): { added: T[], deleted: T[] } {
        return {
            added: [...this.__added_object],
            deleted: [...this.__deleted_object]
        };
    }

    public isEmpty(): boolean {
        return this.collection.length === 0;
    }

    public find(predicate: (item: T) => boolean): T | undefined {
        return this.collection.find(predicate);
    }
}
