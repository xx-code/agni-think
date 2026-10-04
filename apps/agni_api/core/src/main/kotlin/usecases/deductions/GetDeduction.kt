package usecases.deductions

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Deduction
import usecases.deductions.dto.GetDeductionOutput
import domain.exceptions.NotFoundException
import java.util.UUID

class GetDeduction(private val deductionRepo: IRepository<Deduction>): UseCase<UUID, GetDeductionOutput>() {
    override suspend fun process(input: UUID): GetDeductionOutput {
        val deduction = deductionRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "deduction")

        return GetDeductionOutput(
            id = deduction.id,
            title = deduction.title,
            description = deduction.description,
            base = deduction.base.value,
            mode = deduction.mode.value
        )
    }
}