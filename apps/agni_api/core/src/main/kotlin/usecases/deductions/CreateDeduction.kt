package usecases.deductions

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Deduction
import domain.entities.User
import usecases.dto.CreatedOutput
import usecases.deductions.dto.CreateDeductionInput
import domain.exceptions.AlreadyExistException

class CreateDeduction(
    private val deductionRepo: IRepository<Deduction>,
    auth: IAuthenticateAdapter): UseCase<CreateDeductionInput, CreatedOutput>(auth = auth) {

    override suspend fun process(input: CreateDeductionInput): CreatedOutput {
        val currentUser = getCurrentUser()

        if (deductionRepo.existsByName(input.title))
            throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "deduction")

        val newDeduction = Deduction(
            userId = currentUser.id,
            title = input.title,
            description = input.description,
            base = input.base,
            mode = input.mode
        )

        deductionRepo.create(newDeduction)

        return CreatedOutput(newDeduction.id)
    }
}