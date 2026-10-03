package dev.auguste.agni_api.infras.persistences.jbdc_model

import domain.entities.PatrimonySnapshot
import domain.entities.Profile
import domain.enums.PatrimonySnapshotStatusType
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.util.UUID

@Table("profiles")
data class JdbcProfileModel(
    @Id
    @get:JvmName("getIdentifier")
    val profileId: UUID,

    @Column("max_wishlist_amount")
    val maxWishlistAmount: Double,

    @Column("fix_spend_percentage")
    val fixSpendPercentage: Double,

    @Column("varial_spend_percentage")
    val varialSpendPercentage: Double,

    @Column("saving_percentage")
    val savingPercentage: Double,
    val balanceBuffer: Double
) : JdbcModel() {
    override fun getId(): UUID {
        return profileId
    }
}

@Component
class JdbcProfileMapper: IMapper<JdbcProfileModel, Profile> {
    override fun toDomain(model: JdbcProfileModel): Profile {
        return Profile(
            id = model.id,
            maxWishlistAmount = model.maxWishlistAmount,
            fixSpendPercentage = model.fixSpendPercentage,
            varialSpendPercentage = model.varialSpendPercentage,
            savingPercentage = model.savingPercentage,
            balanceBuffer = model.balanceBuffer
        )
    }

    override fun toModel(entity: Profile): JdbcProfileModel {
        return JdbcProfileModel(
            profileId = entity.id,
            maxWishlistAmount = entity.maxWishlistAmount,
            fixSpendPercentage = entity.fixSpendPercentage,
            varialSpendPercentage = entity.varialSpendPercentage,
            balanceBuffer = entity.balanceBuffer,
            savingPercentage = entity.savingPercentage
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "profile_id",
        "maxWishlistAmount" to "max_wishlist_amount",
        "fixSpendPercentage" to "fix_spend_percentage",
        "varialSpendPercentage" to "varial_spend_percentage",
        "savingPercentage" to "saving_percentage",
        "balanceBuffer" to "balance_buffer"
    )

    override fun getTableName(): String = "profiles"

    override fun getSortField(): Set<String> {
        return setOf()
    }

    override fun getModelClass(): Class<JdbcProfileModel> = JdbcProfileModel::class.java
}