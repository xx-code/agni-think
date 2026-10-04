package usecase_configs

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.Deduction
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.deductions.CreateDeduction
import usecases.deductions.DeleteDeduction
import usecases.deductions.GetAllDeductions
import usecases.deductions.GetDeduction
import usecases.deductions.UpdateDeduction
import usecases.deductions.dto.CreateDeductionInput
import usecases.deductions.dto.DeleteDeductionInput
import usecases.deductions.dto.GetDeductionOutput
import usecases.deductions.dto.UpdateDeductionInput
import usecases.interfaces.IUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import usecases.UseCase
import java.util.UUID

@Configuration
class DeductionConfig {

    @Bean
    fun createDeduction(
        deductionRepo: IRepository<Deduction>,
    ): UseCase<CreateDeductionInput, CreatedOutput> {
        return CreateDeduction(
            deductionRepo = deductionRepo
        )
    }

    @Bean
    fun deleteDeduction(
        deductionRepo: IRepository<Deduction>,
    ): UseCase<DeleteDeductionInput, Unit> {
        return DeleteDeduction(
            deductionRepo = deductionRepo
        )
    }

    @Bean
    fun getDeduction(
        deductionRepo: IRepository<Deduction>,
    ): UseCase<UUID, GetDeductionOutput> {
        return GetDeduction(
            deductionRepo = deductionRepo
        )
    }

    @Bean
    fun getAllDeductions(
        deductionRepo: IRepository<Deduction>,
    ): UseCase<QueryFilter, ListOutput<GetDeductionOutput>> {
        return GetAllDeductions(
            deductionRepo = deductionRepo
        )
    }

    @Bean
    fun updateDeduction(
        deductionRepo: IRepository<Deduction>,
    ): UseCase<UpdateDeductionInput, Unit> {
        return UpdateDeduction(
            deductionRepo = deductionRepo
        )
    }
}