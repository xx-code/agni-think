package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.usecases.profiles.dto.CreateProfileInput
import dev.auguste.agni_api.core.usecases.profiles.dto.UpdateProfileInput
import jakarta.validation.constraints.Min
import java.util.UUID

data class ApiCreateProfileModel(
    @field:Min(0,"Max wishList amount should be greater or equal to 0")
    val maxWishlistAmount: Double,

    @field:Min(0,"Fix percentage should be greater or equal to 0")
    val fixSpendPercentage: Double,

    @field:Min(0,"variable percentage should be greater or equal to 0")
    val varialSpendPercentage: Double,

    @field:Min(0,"variable percentage should be greater or equal to 0")
    val savingPercentage: Double
)

data class ApiUpdateProfileModel(
    @field:Min(0,"Max wishList amount should be greater or equal to 0")
    val maxWishlistAmount: Double?,

    @field:Min(0,"Fix percentage should be greater or equal to 0")
    val fixSpendPercentage: Double?,

    @field:Min(0,"variable percentage should be greater or equal to 0")
    val varialSpendPercentage: Double?,

    @field:Min(0,"variable percentage should be greater or equal to 0")
    val savingPercentage: Double?
)

// by pass single app user
val tempPrivateProfileKey = UUID.fromString("457ae73e-8124-4d3b-ab2b-d6a404c6b4d3")

fun mapApiCreateProfileToCreateProfile(input: ApiCreateProfileModel) : CreateProfileInput {
    return CreateProfileInput(
        maxWishlistAmount = input.maxWishlistAmount,
        fixSpendPercentage = input.fixSpendPercentage,
        varialSpendPercentage = input.varialSpendPercentage,
        savingPercentage = input.savingPercentage
    )
}

fun mapApiUpdateProfileToUpdateProfile(id: UUID, input: ApiUpdateProfileModel) : UpdateProfileInput {

    return UpdateProfileInput(
        id = tempPrivateProfileKey,
        maxWishlistAmount = input.maxWishlistAmount,
        fixSpendPercentage = input.fixSpendPercentage,
        varialSpendPercentage = input.varialSpendPercentage,
        savingPercentage = input.savingPercentage
    )
}