package dev.auguste.agni_api.core.usecases.tags

import dev.auguste.agni_api.core.adapters.IChecker
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.usecases.DeleteOutput
import dev.auguste.agni_api.core.usecases.tags.dto.DeleteTagInput

class DeleteTag(
    private val tagRepo: IRepository<Tag>,
    private val checker: IChecker<Tag>
    ): IUseCase<DeleteTagInput, DeleteOutput> {

    override fun execAsync(input: DeleteTagInput): DeleteOutput {
        val tag = tagRepo.get(input.tagId) ?: throw DomainException.NotFound.Tag(input.tagId)

        if (tag.isSystem)
            throw DomainException.BusinessLogic.CantDeleteSystemTag(tag.value)

        if (checker.isInUse(tag))
            return DeleteOutput.inUse()


        tagRepo.delete(input.tagId)

        return DeleteOutput.success()
    }
}