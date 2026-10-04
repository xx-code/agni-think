package usecase_configs

import adapters.IChecker
import adapters.repositories.IRepository
import domain.entities.Category
import usecases.dto.CreatedOutput
import usecases.dto.DeleteOutput
import usecases.dto.ListOutput
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
import usecases.UseCase
import java.util.UUID

@Configuration
class CategoryConfig {

    @Bean
    fun createCategory(
        categoryRepository: IRepository<Category>
    ): UseCase<CreateCategoryInput, CreatedOutput> {
        return CreateCategory(
            categoryRepo = categoryRepository
        )
    }

    @Bean
    fun deleteCategory(
        categoryRepository: IRepository<Category>,
        checker: IChecker<Category>
    ): UseCase<DeleteCategoryInput, DeleteOutput> {
        return DeleteCategory(
            categoryRepo = categoryRepository,
             checker
        )
    }

    @Bean
    fun getAllCategories(
        categoryRepository: IRepository<Category>
    ): UseCase<GetAllCategoryInput, ListOutput<GetCategoryOutput>> {
       return GetAllCategory(
           categoryRepo = categoryRepository
       )
    }

    @Bean
    fun getCategory(
        categoryRepository: IRepository<Category>
    ): UseCase<UUID, GetCategoryOutput> {
        return GetCategory(
            categoryRepo = categoryRepository
        )
    }

    @Bean
    fun updateCategory(
        categoryRepository: IRepository<Category>
    ): UseCase<UpdateCategoryInput, Unit> {
        return UpdateCategory(
            categoryRepo = categoryRepository
        )
    }
}