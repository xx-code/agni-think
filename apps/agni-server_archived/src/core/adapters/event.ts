export type EventContent = {
    title: string
    content: string
}

export interface IEventListener {
    update(event: EventContent): void
}

export interface IEventRegister {
    subscribe(event: string, listener: IEventListener): void
    unsubscribe(event: string, listener: IEventListener): void
    notify(event: string, content: EventContent): void
}