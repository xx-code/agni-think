package dev.auguste.agni_api.infras.usecase_configs

import adapters.IChecker
import adapters.repositories.IRepository
import domain.entities.Tag
import usecases.CreatedOutput
import usecases.DeleteOutput
import usecases.ListOutput
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
import java.util.UUID

@Configuration
class TagConfig {

    @Bean
    fun createTag(
       tagRepository: IRepository<Tag>
    ): IUseCase<CreateTagInput, CreatedOutput> {
        return CreateTag(
            tagRepo = tagRepository,
        )
    }

    @Bean
    fun getTag(
        tagRepository: IRepository<Tag>
    ): IUseCase<UUID, GetTagOutput> {
        return GetTag(
            tagRepo = tagRepository
        )
    }

    @Bean
    fun deleteTag(
        tagRepository: IRepository<Tag>,
        checker: IChecker<Tag>
    ): IUseCase<DeleteTagInput, DeleteOutput> {
        return DeleteTag(
            tagRepo = tagRepository,
            checker = checker
        )
    }

    @Bean
    fun getAllTags(
        tagRepository: IRepository<Tag>
    ): IUseCase<GetAllTagInput, ListOutput<GetTagOutput>> {
        return GetAllTags(
            tagRepo = tagRepository
        )
    }

    @Bean
    fun updateTag(
        tagRepository: IRepository<Tag>
    ): IUseCase<UpdateTagInput, Unit> {
        return UpdateTag(
            tagRepo = tagRepository,
        )
    }
}