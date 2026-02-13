import { Knex } from "knex";
import Mapper, { KnexFilterExtendAdapter, KnexTable } from "./mapper";
import Notification from "@core/domains/entities/notification";
import { KnexModel } from "./model";
import { NotificaitonExtendFilter } from "@core/adapters/repository";

export class KnexNotificationTable implements KnexTable {
    getTableName(): string {
        return 'notifications'
    }
    async createTable(knex: Knex): Promise<void> {
        if (!await knex.schema.hasTable('notifications'))
            await knex.schema.createTable('notifications', (table) => {
                table.string('notification_id').primary()
                table.string('title')
                table.text('content')
                table.boolean('is_read')
                table.datetime('date')
            })
    }
}

export type NotificationModel = KnexModel & {
    notification_id: string
    title: string
    content: string
    is_read: boolean
    date: Date
}

export class NotificationModelMapper implements Mapper<Notification, NotificationModel> {
    toDomain(model: NotificationModel): Notification {
        return new Notification(
            model.notification_id,
            model.title,
            model.content,
            model.is_read,
            new Date(model.date)
        )
    }
    fromDomain(entity: Notification): NotificationModel {
        return {
            notification_id: entity.getId(),
            title: entity.getTitle(),
            content: entity.getContent(),
            is_read: entity.getIsRead(),
            date: entity.getDateTime()
        }
    }
    getSortFilterFields(): string[] {
        return ['date']
    }
    getIdField(): string {
        return 'notification_id' 
    }
    getNameField(): string {
        return 'content'
    }
}

export class NotificaitonExtendFilterAdapter implements KnexFilterExtendAdapter<Notification, NotificationModel> {
    filterQuery(query: Knex.QueryBuilder, filtersExtend: NotificaitonExtendFilter): void {
        if (filtersExtend.isRead !== undefined)
            query.where('is_read', '=', filtersExtend.isRead)
    }
} 