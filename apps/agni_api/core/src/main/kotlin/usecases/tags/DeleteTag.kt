package usecases.tags

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.IChecker
import adapters.repositories.IRepository
import domain.entities.Tag
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import usecases.dto.DeleteOutput
import usecases.tags.dto.DeleteTagInput

class DeleteTag(
    private val tagRepo: IRepository<Tag>,
    private val checker: IChecker<Tag>,
    auth: IAuthenticateAdapter
    ): UseCase<DeleteTagInput, DeleteOutput>(auth = auth) {

    override suspend fun process(input: DeleteTagInput): DeleteOutput {
        val tag = tagRepo.get(input.tagId) ?: throw NotFoundException.SingleEntity(input.tagId, "tag")

        if (tag.isSystem)
            throw ValidationException.CantDeleteSystemTag(tag.value)

        if (checker.isInUse(tag))
            return DeleteOutput.inUse()


        tagRepo.delete(input.tagId)

        return DeleteOutput.success()
    }
}