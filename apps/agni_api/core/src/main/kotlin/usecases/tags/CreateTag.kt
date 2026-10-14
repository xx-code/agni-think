package usecases.tags

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Color
import domain.entities.Tag
import usecases.dto.CreatedOutput
import domain.exceptions.AlreadyExistException
import domain.exceptions.ValidationException
import usecases.tags.dto.CreateTagInput

class CreateTag(
    private val tagRepo: IRepository<Tag>,
    auth: IAuthenticateAdapter): UseCase<CreateTagInput, CreatedOutput>(auth = auth) {

    override suspend fun process(input: CreateTagInput): CreatedOutput {
        val currentUser = getCurrentUser()

        if (tagRepo.existsByName(input.value))
            throw AlreadyExistException.EntitiesByField(mapOf("name" to input.value), "tag")

        if (input.isSystem == true)
            throw ValidationException.UserCannotCreateSystemTag()

        val newTag = Tag(userId = currentUser.id, value = input.value, color = Color(input.color), isSystem = input.isSystem ?: false)

        tagRepo.create(newTag)

        return CreatedOutput(newTag.id)
    }
}