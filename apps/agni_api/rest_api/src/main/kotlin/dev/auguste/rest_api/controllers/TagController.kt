package dev.auguste.rest_api.controllers

import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiArchiveTagModel
import dev.auguste.rest_api.controllers.models.ApiCreateTagModel
import dev.auguste.rest_api.controllers.models.ApiUpdateTagModel
import dev.auguste.rest_api.controllers.models.mapApiCreateTag
import dev.auguste.rest_api.controllers.models.mapApiUpdateTag
import usecases.dto.CreatedOutput
import usecases.dto.DeleteOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.tags.dto.CreateTagInput
import usecases.tags.dto.DeleteTagInput
import usecases.tags.dto.GetAllTagInput
import usecases.tags.dto.GetTagOutput
import usecases.tags.dto.UpdateTagInput
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
@RequestMapping("/v2/tags")
class TagController(
    private val createTagUseCase: IUseCase<CreateTagInput, CreatedOutput>,
    private val updateTagUseCase: IUseCase<UpdateTagInput, Unit>,
    private val deleteTagUseCase: IUseCase<DeleteTagInput, DeleteOutput>,
    private val getTagUseCase: IUseCase<UUID, GetTagOutput>,
    private val getAllTagUseCase: IUseCase<GetAllTagInput, ListOutput<GetTagOutput>>
) {

    @PostMapping
    suspend fun createTag(@Valid @RequestBody request: ApiCreateTagModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createTagUseCase.execute(
            mapApiCreateTag(request)
        ).getOrThrow())
    }

    @PutMapping("/{id}")
    suspend fun updateTag(@PathVariable id: UUID, @Valid @RequestBody input: ApiUpdateTagModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateTagUseCase.execute(
            mapApiUpdateTag(id, input)
        ).getOrThrow())
    }

    @PutMapping("/{id}/archive")
    suspend fun archiveCategory(@PathVariable id: UUID, @Valid @RequestBody request: ApiArchiveTagModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateTagUseCase.execute(
            input = UpdateTagInput(
                id = id,
                archive = request.archive
            )
        ).getOrThrow())
    }

    @DeleteMapping("/{id}")
    suspend fun deleteTag(@PathVariable id: UUID): ResponseEntity<DeleteOutput> {
        return ResponseEntity.ok(deleteTagUseCase.execute(
            DeleteTagInput(id)
        ).getOrThrow())
    }

    @GetMapping("/{id}")
    suspend fun getTag(@PathVariable id: UUID): ResponseEntity<GetTagOutput> {
        return ResponseEntity.ok(getTagUseCase.execute(
            id
        ).getOrThrow())
    }

    @GetMapping
    suspend fun getAllTags(query: QueryFilter, isSystem: Boolean? = null, isArchived: Boolean? = null): ResponseEntity<ListOutput<GetTagOutput>> {
        return ResponseEntity.ok(getAllTagUseCase.execute(
            GetAllTagInput(
                query,
                isSystem,
                isArchived
            )
        ).getOrThrow())
    }
}