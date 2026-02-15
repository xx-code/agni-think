package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.categories.CreateCategory
import dev.auguste.agni_api.core.usecases.categories.DeleteCategory
import dev.auguste.agni_api.core.usecases.categories.GetAllCategory
import dev.auguste.agni_api.core.usecases.categories.GetCategory
import dev.auguste.agni_api.core.usecases.categories.UpdateCategory
import dev.auguste.agni_api.core.usecases.categories.dto.CreateCategoryInput
import dev.auguste.agni_api.core.usecases.categories.dto.DeleteCategoryInput
import dev.auguste.agni_api.core.usecases.categories.dto.GetAllCategoryInput
import dev.auguste.agni_api.core.usecases.categories.dto.GetCategoryOutput
import dev.auguste.agni_api.core.usecases.categories.dto.UpdateCategoryInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class CategoryConfig {

    @Bean
    fun createCategory(
        categoryRepository: IRepository<Category>
    ): IUseCase<CreateCategoryInput, CreatedOutput> {
        return CreateCategory(
            categoryRepo = categoryRepository
        )
    }

    @Bean
    fun deleteCategory(
        categoryRepository: IRepository<Category>
    ): IUseCase<DeleteCategoryInput, Unit> {
        return DeleteCategory(
            categoryRepo = categoryRepository
        )
    }

    @Bean
    fun getAllCategories(
        categoryRepository: IRepository<Category>
    ): IUseCase<GetAllCategoryInput, ListOutput<GetCategoryOutput>> {
       return GetAllCategory(
           categoryRepo = categoryRepository
       )
    }

    @Bean
    fun getCategory(
        categoryRepository: IRepository<Category>
    ): IUseCase<UUID, GetCategoryOutput> {
        return GetCategory(
            categoryRepo = categoryRepository
        )
    }

    @Bean
    fun updateCategory(
        categoryRepository: IRepository<Category>
    ): IUseCase<UpdateCategoryInput, Unit> {
        return UpdateCategory(
            categoryRepo = categoryRepository
        )
    }
}