package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiCreateCurrencyModel
import dev.auguste.agni_api.controllers.models.ApiUpdateCurrencyModel
import dev.auguste.agni_api.controllers.models.mapApiCreateCurrency
import dev.auguste.agni_api.controllers.models.mapApiUpdateCurrency
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.currencies.dto.CreateCurrencyInput
import dev.auguste.agni_api.core.usecases.currencies.dto.DeleteCurrencyInput
import dev.auguste.agni_api.core.usecases.currencies.dto.GetCurrencyOutput
import dev.auguste.agni_api.core.usecases.currencies.dto.UpdateCurrencyInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
    fun createCurrency(@Valid @RequestBody request: ApiCreateCurrencyModel) : ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createCurrencyUseCase.execAsync(
            mapApiCreateCurrency(request)
        ))
    }

    @PutMapping("/{id}")
    fun updateCurrency(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateCurrencyModel) : ResponseEntity<Unit> {
        return ResponseEntity.ok(updateCurrencyUseCase.execAsync(
            mapApiUpdateCurrency(id,request)
        ))
    }

    @DeleteMapping("/{id}")
    fun deleteCurrency(@PathVariable id: UUID) : ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteCurrencyUseCase.execAsync(
            DeleteCurrencyInput(id)
        ))
    }

    @GetMapping("/{id}")
    fun getCurrency(@PathVariable id: UUID) : ResponseEntity<GetCurrencyOutput> {
        return ResponseEntity.ok(getCurrencyUseCase.execAsync(
            id
        ))
    }

    @GetMapping
    fun getAllCurrencies(query: QueryFilter) : ResponseEntity<ListOutput<GetCurrencyOutput>> {
        return ResponseEntity.ok(getAllCurrenciesUseCase.execAsync(
            query
        ))
    }
}