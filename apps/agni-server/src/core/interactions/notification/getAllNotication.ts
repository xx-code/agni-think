import { ListDto, QueryFilter } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import Repository, { NotificaitonExtendFilter } from "@core/adapters/repository";
import Notification from "@core/domains/entities/notification";

export type NotificationQueryFilter = QueryFilter & {
    isRead?: boolean
}

export type GetAllNotificationDto = {
    id: string
    title: string
    content: string
    isRead: boolean
    dateTime: string
}

export class GetAllNotificationUseCases implements IUsecase<NotificationQueryFilter, ListDto<GetAllNotificationDto>> {
    private notificationRepo: Repository<Notification>

    constructor(notificationRepo: Repository<Notification>){
        this.notificationRepo = notificationRepo
    }

    async execute(request: NotificationQueryFilter): Promise<ListDto<GetAllNotificationDto>> {
        const extendFilter = new NotificaitonExtendFilter()
        extendFilter.isRead = request.isRead
        const res = await this.notificationRepo.getAll({
            limit: request.limit,
            offset: request.offset,
            queryAll: request.queryAll
        }, extendFilter) 

        return {
            items: res.items.map(i => ({
                id: i.getId(),
                title: i.getTitle(),
                content: i.getContent(),
                isRead: i.getIsRead(),
                dateTime: i.getDateTime().toISOString()
            })),
            totals: res.total
        }
    }
}