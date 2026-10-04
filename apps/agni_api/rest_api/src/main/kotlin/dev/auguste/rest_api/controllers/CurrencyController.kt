package dev.auguste.rest_api.controllers

import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiCreateCurrencyModel
import dev.auguste.rest_api.controllers.models.ApiUpdateCurrencyModel
import dev.auguste.rest_api.controllers.models.mapApiCreateCurrency
import dev.auguste.rest_api.controllers.models.mapApiUpdateCurrency
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.currencies.dto.CreateCurrencyInput
import usecases.currencies.dto.DeleteCurrencyInput
import usecases.currencies.dto.GetCurrencyOutput
import usecases.currencies.dto.UpdateCurrencyInput
import usecases.interfaces.IUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v2/currencies")
class CurrencyController(
    private val createCurrencyUseCase: IUseCase<CreateCurrencyInput, CreatedOutput>,
    private val updateCurrencyUseCase: IUseCase<UpdateCurrencyInput, Unit>,
    private val deleteCurrencyUseCase: IUseCase<DeleteCurrencyInput, Unit>,
    private val getCurrencyUseCase: IUseCase<UUID, GetCurrencyOutput>,
    private val getAllCurrenciesUseCase: IUseCase<QueryFilter, ListOutput<GetCurrencyOutput>>
) {

    @PostMapping
    suspend fun createCurrency(@Valid @RequestBody request: ApiCreateCurrencyModel) : ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createCurrencyUseCase.execute(
            mapApiCreateCurrency(request)
        ).getOrThrow())
    }

    @PutMapping("/{id}")
    suspend fun updateCurrency(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateCurrencyModel) : ResponseEntity<Unit> {
        return ResponseEntity.ok(updateCurrencyUseCase.execute(
            mapApiUpdateCurrency(id, request)
        ).getOrThrow())
    }

    @DeleteMapping("/{id}")
    suspend fun deleteCurrency(@PathVariable id: UUID) : ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteCurrencyUseCase.execute(
            DeleteCurrencyInput(id)
        ).getOrThrow())
    }

    @GetMapping("/{id}")
    suspend fun getCurrency(@PathVariable id: UUID) : ResponseEntity<GetCurrencyOutput> {
        return ResponseEntity.ok(getCurrencyUseCase.execute(
            id
        ).getOrThrow())
    }

    @GetMapping
    suspend fun getAllCurrencies(query: QueryFilter) : ResponseEntity<ListOutput<GetCurrencyOutput>> {
        return ResponseEntity.ok(getAllCurrenciesUseCase.execute(
            query
        ).getOrThrow())
    }
}