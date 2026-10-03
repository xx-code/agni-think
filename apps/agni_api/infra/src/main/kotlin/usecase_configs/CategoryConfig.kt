package usecase_configs

import adapters.IChecker
import adapters.repositories.IRepository
import domain.entities.Category
import usecases.CreatedOutput
import usecases.DeleteOutput
import usecases.ListOutput
import usecases.categories.CreateCategory
import usecases.categories.DeleteCategory
import usecases.categories.GetAllCategory
import usecases.categories.GetCategory
import usecases.categories.UpdateCategory
import usecases.categories.dto.CreateCategoryInput
import usecases.categories.dto.DeleteCategoryInput
import usecases.categories.dto.GetAllCategoryInput
import usecases.categories.dto.GetCategoryOutput
import usecases.categories.dto.UpdateCategoryInput
import usecases.interfaces.IUseCase
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
        categoryRepository: IRepository<Category>,
        checker: IChecker<Category>
    ): IUseCase<DeleteCategoryInput, DeleteOutput> {
        return DeleteCategory(
            categoryRepo = categoryRepository,
             checker
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