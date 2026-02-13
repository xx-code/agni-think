package dev.auguste.agni_api.infras.persistences.jbdc_model

import dev.auguste.agni_api.core.entities.Notification
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Table("notifications")
data class JdbcNotificationModel(
    @Id
    @get:JvmName("getIdentifier")
    @Column("notification_id")
    val id: UUID,

    @Column("title")
    val name: String,

    val content: String,

    @Column("is_read")
    val isRead: Boolean,

    val date: LocalDateTime
) : JdbcModel() {
    override fun getId(): UUID {
        return id
    }
}

@Component
class JdbcNotificationModelMapper: IMapper<JdbcNotificationModel, Notification> {
    override fun toDomain(model: JdbcNotificationModel): Notification {
        return Notification(
            id = model.id,
            title = model.name,
            content = model.content,
            dateTime = model.date,
            isRead =model.isRead,
        )
    }

    override fun toModel(entity: Notification): JdbcNotificationModel {
        return JdbcNotificationModel(
            id = entity.id,
            name = entity.title,
            content = entity.content,
            isRead = entity.isRead,
            date = entity.dateTime
        )
    }

    override fun getSortField(): Set<String> {
        return setOf("date")
    }
}