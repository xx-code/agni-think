package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.tags.CreateTag
import dev.auguste.agni_api.core.usecases.tags.DeleteTag
import dev.auguste.agni_api.core.usecases.tags.GetAllTags
import dev.auguste.agni_api.core.usecases.tags.GetTag
import dev.auguste.agni_api.core.usecases.tags.UpdateTag
import dev.auguste.agni_api.core.usecases.tags.dto.CreateTagInput
import dev.auguste.agni_api.core.usecases.tags.dto.DeleteTagInput
import dev.auguste.agni_api.core.usecases.tags.dto.GetAllTagInput
import dev.auguste.agni_api.core.usecases.tags.dto.GetTagOutput
import dev.auguste.agni_api.core.usecases.tags.dto.UpdateTagInput
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
        tagRepository: IRepository<Tag>
    ): IUseCase<DeleteTagInput, Unit> {
        return DeleteTag(
            tagRepo = tagRepository,
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