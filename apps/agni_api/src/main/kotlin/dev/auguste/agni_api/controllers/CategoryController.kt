package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiCreateCategoryModel
import dev.auguste.agni_api.controllers.models.ApiUpdateCategoryModel
import dev.auguste.agni_api.controllers.models.mapApiCreateCategoryModel
import dev.auguste.agni_api.controllers.models.mapApiUpdateCategoryModel
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.categories.dto.CreateCategoryInput
import dev.auguste.agni_api.core.usecases.categories.dto.DeleteCategoryInput
import dev.auguste.agni_api.core.usecases.categories.dto.GetCategoryOutput
import dev.auguste.agni_api.core.usecases.categories.dto.UpdateCategoryInput
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
@RequestMapping("/v2/categories")
class CategoryController(
    private val createCategoryUseCase: IUseCase<CreateCategoryInput, CreatedOutput>,
    private val updateCategoryUseCase: IUseCase<UpdateCategoryInput, Unit>,
    private val getCategoryUseCase: IUseCase<UUID, GetCategoryOutput>,
    private val getAllCategoryUseCase: IUseCase<QueryFilter, ListOutput<GetCategoryOutput>>,
    private val deleteCategoryUseCase: IUseCase<DeleteCategoryInput, Unit>
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

    @GetMapping("/{id}")
    fun getCategory(@PathVariable id: UUID): ResponseEntity<GetCategoryOutput> {
        return ResponseEntity.ok(getCategoryUseCase.execAsync(id))
    }

    @GetMapping
    fun getAllCategories(queryFilter: QueryFilter): ResponseEntity<ListOutput<GetCategoryOutput>> {
        return ResponseEntity.ok(getAllCategoryUseCase.execAsync(queryFilter))
    }

    @DeleteMapping
    fun deleteCategory(@RequestParam id: UUID): ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteCategoryUseCase.execAsync(
            DeleteCategoryInput(id)
        ))
    }
}