import Entity, { TrackableProperty } from "./entity";

export default class Notification extends Entity {
    private title: string
    private content: string
    private isRead: TrackableProperty<boolean>
    private dateTime: Date

    constructor(id: string, title: string, content: string, isRead: boolean, dateTime: Date) {
        super(id)
        this.title = title
        this.content = content
        this.isRead = new TrackableProperty(isRead, this.markHasChange.bind(this))
        this.dateTime = dateTime
    }

    getTitle(): string {
        return this.title
    }

    getContent(): string {
        return this.content
    } 

    getIsRead(): boolean {
        return this.isRead.get() 
    }

    setIsRead(isRead: boolean) {
        this.isRead.set(isRead)
    }

    getDateTime(): Date {
        return this.dateTime
    }
}