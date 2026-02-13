package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiCreateTagModel
import dev.auguste.agni_api.controllers.models.ApiUpdateTagModel
import dev.auguste.agni_api.controllers.models.mapApiCreateTag
import dev.auguste.agni_api.controllers.models.mapApiUpdateTag
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.tags.dto.CreateTagInput
import dev.auguste.agni_api.core.usecases.tags.dto.DeleteTagInput
import dev.auguste.agni_api.core.usecases.tags.dto.GetTagOutput
import dev.auguste.agni_api.core.usecases.tags.dto.UpdateTagInput
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
@RequestMapping("/v2/tags")
class TagController(
    private val createTagUseCase: IUseCase<CreateTagInput, CreatedOutput>,
    private val updateTagUseCase: IUseCase<UpdateTagInput, Unit>,
    private val deleteTagUseCase: IUseCase<DeleteTagInput, Unit>,
    private val getTagUseCase: IUseCase<UUID, GetTagOutput>,
    private val getAllTagUseCase: IUseCase<QueryFilter, ListOutput<GetTagOutput>>
) {

    @PostMapping
    fun createTag(@Valid @RequestBody request: ApiCreateTagModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createTagUseCase.execAsync(
            mapApiCreateTag(request)
        ))
    }

    @PutMapping("/{id}")
    fun updateTag(@PathVariable id: UUID, @Valid @RequestBody input: ApiUpdateTagModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateTagUseCase.execAsync(
            mapApiUpdateTag(id, input)
        ))
    }

    @DeleteMapping("/{id}")
    fun deleteTag(@PathVariable id: UUID): ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteTagUseCase.execAsync(
            DeleteTagInput(id)
        ))
    }

    @GetMapping("/{id}")
    fun getTag(@PathVariable id: UUID): ResponseEntity<GetTagOutput> {
        return ResponseEntity.ok(getTagUseCase.execAsync(
            id
        ))
    }

    @GetMapping
    fun getAllTags(query: QueryFilter): ResponseEntity<ListOutput<GetTagOutput>> {
        return ResponseEntity.ok(getAllTagUseCase.execAsync(
            query
        ))
    }
}