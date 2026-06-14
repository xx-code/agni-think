package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiAddSuggestionModel
import dev.auguste.agni_api.controllers.models.mapApiAddSuggestionModel
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.entities.enums.AgentSuggestionStatusType
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.AddSuggestionInput
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.ConfirmSuggestionInput
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.GetAllSuggestionInput
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.GetSuggestionOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v2/agent-suggestions")
class AgentSuggestionController(
    private val addSuggestion: IUseCase<AddSuggestionInput, CreatedOutput>,
    private val confirmSuggestion: IUseCase<ConfirmSuggestionInput, Unit>,
    private val getAllSuggestions: IUseCase<GetAllSuggestionInput, ListOutput<GetSuggestionOutput>>
) {
    @PostMapping
    fun addSuggestion(@RequestBody input: ApiAddSuggestionModel): ResponseEntity<CreatedOutput> {
        
        return ResponseEntity.ok(addSuggestion.execAsync(mapApiAddSuggestionModel(input)))
    }

    @GetMapping
    fun getAllSuggestions(query: QueryFilter, status: String? = null): ResponseEntity<ListOutput<GetSuggestionOutput>> {
        return ResponseEntity.ok(getAllSuggestions.execAsync(
            GetAllSuggestionInput(
                query = query,
                status = status?.let { AgentSuggestionStatusType.fromString(it) }
            )
        ))
    }

    @PostMapping("/{id}/confirm")
    fun confirmSuggestion(@PathVariable id: UUID, isConfirm: Boolean): ResponseEntity<Unit> {
        return ResponseEntity.ok(confirmSuggestion.execAsync(
            input = ConfirmSuggestionInput(
               id,
                isAccept = isConfirm
            )
        ))
    }
}