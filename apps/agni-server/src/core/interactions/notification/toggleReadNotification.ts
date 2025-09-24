import Repository from "@core/adapters/repository";
import { IUsecase } from "../interfaces";
import Notification from "@core/domains/entities/notification";

export class ToggleReadNotificationUseCase implements IUsecase<string, void> {
    private notificationRepo: Repository<Notification>

    constructor(notificationRepo: Repository<Notification>) {
        this.notificationRepo = notificationRepo
    }

    async execute(id: string): Promise<void> {
        const notification = await this.notificationRepo.get(id)
        if (notification) {
            notification.setIsRead(!notification.getIsRead())
            await this.notificationRepo.update(notification)
        }
    }
}