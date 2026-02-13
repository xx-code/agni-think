package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.usecases.currencies.dto.CreateCurrencyInput
import dev.auguste.agni_api.core.usecases.currencies.dto.UpdateCurrencyInput
import jakarta.validation.constraints.NotEmpty
import java.util.UUID

data class ApiCreateCurrencyModel(
    @field:NotEmpty("Name must not be empty")
    val name: String,
    @field:NotEmpty("Symbol must not be empty")
    val symbol: String,
    val local: String?,
    val rateToBase: Double?,
    val isBase: Boolean?
)

data class ApiUpdateCurrencyModel(
    val name: String?,
    val symbol: String?,
    val local: String?,
    val rateToBase: Double?,
    val isBase: Boolean?
)

fun mapApiCreateCurrency(model: ApiCreateCurrencyModel): CreateCurrencyInput {
    return CreateCurrencyInput(
        name = model.name,
        symbol = model.symbol,
        locale = model.local,
        rateToBase = model.rateToBase,
        isBase = model.isBase
    )
}

fun mapApiUpdateCurrency(id: UUID, model: ApiUpdateCurrencyModel): UpdateCurrencyInput {
    return UpdateCurrencyInput(
        id = id,
        name = model.name,
        symbol = model.symbol,
        locale = model.local,
        rateToBase = model.rateToBase,
        isBase = model.isBase
    )
}