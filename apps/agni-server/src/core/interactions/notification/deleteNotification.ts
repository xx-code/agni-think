import Repository from "@core/adapters/repository";
import { IUsecase } from "../interfaces";
import Notification from "@core/domains/entities/notification";

export class DeleteNotifcationUseCase implements IUsecase<string, void> {
    private notificationRepo: Repository<Notification>

    constructor(notificationRepo: Repository<Notification>) {
        this.notificationRepo = notificationRepo
    }

    async execute(id: string): Promise<void> {
        if (await this.notificationRepo.get(id))
            await this.notificationRepo.delete(id)
    }
}