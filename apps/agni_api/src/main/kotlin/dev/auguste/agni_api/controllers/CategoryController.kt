package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiArchiveCategoryModel
import dev.auguste.agni_api.controllers.models.ApiCreateCategoryModel
import dev.auguste.agni_api.controllers.models.ApiUpdateCategoryModel
import dev.auguste.agni_api.controllers.models.mapApiCreateCategoryModel
import dev.auguste.agni_api.controllers.models.mapApiUpdateCategoryModel
import adapters.dto.QueryFilter
import usecases.CreatedOutput
import usecases.DeleteOutput
import usecases.ListOutput
import usecases.categories.dto.CreateCategoryInput
import usecases.categories.dto.DeleteCategoryInput
import usecases.categories.dto.GetAllCategoryInput
import usecases.categories.dto.GetCategoryOutput
import usecases.categories.dto.UpdateCategoryInput
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v2/categories")
class CategoryController(
    private val createCategoryUseCase: IUseCase<CreateCategoryInput, CreatedOutput>,
    private val updateCategoryUseCase: IUseCase<UpdateCategoryInput, Unit>,
    private val getCategoryUseCase: IUseCase<UUID, GetCategoryOutput>,
    private val getAllCategoryUseCase: IUseCase<GetAllCategoryInput, ListOutput<GetCategoryOutput>>,
    private val deleteCategoryUseCase: IUseCase<DeleteCategoryInput, DeleteOutput>
) {

    @PostMapping
    fun createCategory(@Valid @RequestBody request: ApiCreateCategoryModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createCategoryUseCase.execAsync(
            mapApiCreateCategoryModel(request)
        ))
    }

    @PutMapping("/{id}")
    fun updateCategory(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateCategoryModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateCategoryUseCase.execAsync(
            mapApiUpdateCategoryModel(id, request)
        ))
    }


    @PutMapping("/{id}/archive")
    fun archiveCategory(@PathVariable id: UUID, @Valid @RequestBody request: ApiArchiveCategoryModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateCategoryUseCase.execAsync(
            input = UpdateCategoryInput(
                id = id,
                isArchived = request.archive
            )
        ))
    }

    @GetMapping("/{id}")
    fun getCategory(@PathVariable id: UUID): ResponseEntity<GetCategoryOutput> {
        return ResponseEntity.ok(getCategoryUseCase.execAsync(id))
    }

    @GetMapping
    fun getAllCategories(queryFilter: QueryFilter, isSystem: Boolean? = null, isArchived: Boolean? = null): ResponseEntity<ListOutput<GetCategoryOutput>> {
        return ResponseEntity.ok(getAllCategoryUseCase.execAsync(GetAllCategoryInput(
            queryFilter,
            isSystem,
            isArchived
        )))
    }

    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: UUID): ResponseEntity<DeleteOutput> {
        return ResponseEntity.ok(deleteCategoryUseCase.execAsync(
            DeleteCategoryInput(id)
        ))
    }
}