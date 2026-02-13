import { EventContent, IEventListener } from "@core/adapters/event";
import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import Notification from "@core/domains/entities/notification";
import { GetUID } from "@core/adapters/libs";
import { CreatedDto } from "@core/dto/base";

export type RequestPushNotification = {
    title: string
    content: string
    dateTime: Date
}

export class PushNotificationUseCase implements IUsecase<RequestPushNotification, CreatedDto>, IEventListener  { 
    private notificationRepo: Repository<Notification>

    constructor(notificationRepo: Repository<Notification>) {
        this.notificationRepo = notificationRepo
    }

    async execute(request: RequestPushNotification): Promise<CreatedDto> {
        const notification = new Notification(GetUID(), request.title, request.content, false, request.dateTime)
        await this.notificationRepo.create(notification)
        return {newId: notification.getId()} 
    }

    update(event: EventContent): void {
        this.execute({
            title: event.title,
            content: event.content,
            dateTime: new Date() 
        })
    }
}