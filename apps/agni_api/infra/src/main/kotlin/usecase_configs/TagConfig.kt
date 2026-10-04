package usecase_configs

import adapters.IChecker
import adapters.repositories.IRepository
import domain.entities.Tag
import usecases.dto.CreatedOutput
import usecases.dto.DeleteOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.tags.CreateTag
import usecases.tags.DeleteTag
import usecases.tags.GetAllTags
import usecases.tags.GetTag
import usecases.tags.UpdateTag
import usecases.tags.dto.CreateTagInput
import usecases.tags.dto.DeleteTagInput
import usecases.tags.dto.GetAllTagInput
import usecases.tags.dto.GetTagOutput
import usecases.tags.dto.UpdateTagInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import usecases.UseCase
import java.util.UUID

@Configuration
class TagConfig {

    @Bean
    fun createTag(
       tagRepository: IRepository<Tag>
    ): UseCase<CreateTagInput, CreatedOutput> {
        return CreateTag(
            tagRepo = tagRepository,
        )
    }

    @Bean
    fun getTag(
        tagRepository: IRepository<Tag>
    ): UseCase<UUID, GetTagOutput> {
        return GetTag(
            tagRepo = tagRepository
        )
    }

    @Bean
    fun deleteTag(
        tagRepository: IRepository<Tag>,
        checker: IChecker<Tag>
    ): UseCase<DeleteTagInput, DeleteOutput> {
        return DeleteTag(
            tagRepo = tagRepository,
            checker = checker
        )
    }

    @Bean
    fun getAllTags(
        tagRepository: IRepository<Tag>
    ): UseCase<GetAllTagInput, ListOutput<GetTagOutput>> {
        return GetAllTags(
            tagRepo = tagRepository
        )
    }

    @Bean
    fun updateTag(
        tagRepository: IRepository<Tag>
    ): UseCase<UpdateTagInput, Unit> {
        return UpdateTag(
            tagRepo = tagRepository,
        )
    }
}