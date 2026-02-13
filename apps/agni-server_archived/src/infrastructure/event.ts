import { EventContent, IEventListener, IEventRegister } from "@core/adapters/event";

export class EventRegister implements IEventRegister {
    private listeners: Map<string, IEventListener[]>

    constructor() {
        this.listeners = new Map()
    }

    subscribe(event: string, listener: IEventListener): void {
        if (this.listeners.get(event))
            this.listeners.get(event)?.push(listener)
        else 
            this.listeners.set(event, [listener])
    }

    unsubscribe(event: string, listener: IEventListener): void {
        if (this.listeners.get(event))
        {
            let indexToDel =  this.listeners.get(event)?.findIndex(i => i === listener)
            if (indexToDel !== undefined && indexToDel >= 0)
                this.listeners.get(event)?.splice(indexToDel, 1)
        }
    }

    notify(event: string, content: EventContent): void {
        this.listeners.get(event)?.forEach(i => {
            i.update(content)
        })
    }
}