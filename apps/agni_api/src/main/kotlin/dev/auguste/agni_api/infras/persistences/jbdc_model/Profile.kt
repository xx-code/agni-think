package dev.auguste.agni_api.infras.persistences.jbdc_model

import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.entities.Profile
import dev.auguste.agni_api.core.entities.enums.PatrimonySnapshotStatusType
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
    @Column("profile_id")
    val id: UUID,

    @Column("max_wishlist_amount")
    val maxWishlistAmount: Double,

    @Column("fix_spend_percentage")
    val fixSpendPercentage: Double,

    @Column("varial_spend_percentage")
    val varialSpendPercentage: Double,

    @Column("saving_percentage")
    val savingPercentage: Double
) : JdbcModel() {
    override fun getId(): UUID {
        return id
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
        )
    }

    override fun toModel(entity: Profile): JdbcProfileModel {
        return JdbcProfileModel(
            id = entity.id,
            maxWishlistAmount = entity.maxWishlistAmount,
            fixSpendPercentage = entity.fixSpendPercentage,
            varialSpendPercentage = entity.varialSpendPercentage,
            savingPercentage = entity.savingPercentage
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> {
        TODO("Not yet implemented")
    }

    override fun getTableName(): String {
        TODO("Not yet implemented")
    }

    override fun getSortField(): Set<String> {
        return setOf()
    }

    override fun getModelClass(): Class<JdbcProfileModel> {
        TODO("Not yet implemented")
    }
}