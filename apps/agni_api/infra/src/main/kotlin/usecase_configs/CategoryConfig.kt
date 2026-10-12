package usecase_configs

import adapters.IAuthenticateAdapter
import adapters.IChecker
import adapters.readers.IGetAllCategoryReader
import adapters.repositories.IRepository
import domain.entities.Category
import domain.entities.User
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
        categoryRepository: IRepository<Category>,
        auth: IAuthenticateAdapter
        ): UseCase<CreateCategoryInput, CreatedOutput> {
        return CreateCategory(
            categoryRepo = categoryRepository,
            auth = auth
        )
    }

    @Bean
    fun deleteCategory(
        categoryRepository: IRepository<Category>,
        checker: IChecker<Category>,
        auth: IAuthenticateAdapter
    ): UseCase<DeleteCategoryInput, DeleteOutput> {
        return DeleteCategory(
            categoryRepo = categoryRepository,
             checker,
            auth = auth
        )
    }

    @Bean
    fun getAllCategories(
        categoryRepository: IGetAllCategoryReader,
        auth: IAuthenticateAdapter
    ): UseCase<GetAllCategoryInput, ListOutput<GetCategoryOutput>> {
       return GetAllCategory(
           categoryRepo = categoryRepository,
           auth = auth
       )
    }

    @Bean
    fun getCategory(
        categoryRepository: IRepository<Category>,
        auth: IAuthenticateAdapter
    ): UseCase<UUID, GetCategoryOutput> {
        return GetCategory(
            categoryRepo = categoryRepository,
            auth = auth
        )
    }

    @Bean
    fun updateCategory(
        categoryRepository: IRepository<Category>,
        auth: IAuthenticateAdapter
    ): UseCase<UpdateCategoryInput, Unit> {
        return UpdateCategory(
            categoryRepo = categoryRepository,
            auth = auth
        )
    }
}