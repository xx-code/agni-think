package persistences.jbdc_model

import domain.entities.Notification
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import persistences.IMapper
import java.time.LocalDateTime
import java.util.UUID

@Table("notifications")
data class JdbcNotificationModel(
    @Id
    @get:JvmName("getIdentifier")
    val notificationId: UUID,

    val title: String,

    val content: String,

    @Column("is_read")
    val isRead: Boolean,

    val date: LocalDateTime
) : JdbcModel() {
    override fun getId(): UUID {
        return notificationId
    }
}

@Component
class JdbcNotificationModelMapper: IMapper<JdbcNotificationModel, Notification> {
    override fun toDomain(model: JdbcNotificationModel): Notification {
        return Notification(
            id = model.id,
            title = model.title,
            content = model.content,
            dateTime = model.date,
            isRead =model.isRead,
        )
    }

    override fun toModel(entity: Notification): JdbcNotificationModel {
        return JdbcNotificationModel(
            notificationId = entity.id,
            title = entity.title,
            content = entity.content,
            isRead = entity.isRead,
            date = entity.dateTime
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "notification_id",
        "title" to "title",
        "content" to "content",
        "dateTime" to "date",
        "isRead" to "is_read"
    )

    override fun getTableName(): String = "notifications"

    override fun getSortField(): Set<String> {
        return setOf("date")
    }

    override fun getModelClass(): Class<JdbcNotificationModel> = JdbcNotificationModel::class.java
}