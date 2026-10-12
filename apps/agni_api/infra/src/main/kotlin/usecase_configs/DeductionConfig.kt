package usecase_configs

import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.Deduction
import domain.entities.User
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
import usecases.deductions.dto.GetAllDeductionInput
import java.util.UUID

@Configuration
class DeductionConfig {

    @Bean
    fun createDeduction(
        deductionRepo: IRepository<Deduction>,
        auth: IAuthenticateAdapter
    ): UseCase<CreateDeductionInput, CreatedOutput> {
        return CreateDeduction(
            deductionRepo = deductionRepo,
            auth = auth
        )
    }

    @Bean
    fun deleteDeduction(
        deductionRepo: IRepository<Deduction>,
        auth: IAuthenticateAdapter
    ): UseCase<DeleteDeductionInput, Unit> {
        return DeleteDeduction(
            deductionRepo = deductionRepo,
            auth = auth
        )
    }

    @Bean
    fun getDeduction(
        deductionRepo: IRepository<Deduction>,
        auth: IAuthenticateAdapter
    ): UseCase<UUID, GetDeductionOutput> {
        return GetDeduction(
            deductionRepo = deductionRepo,
            auth = auth
        )
    }

    @Bean
    fun getAllDeductions(
        deductionRepo: IRepository<Deduction>,
        auth: IAuthenticateAdapter
    ): UseCase<GetAllDeductionInput, ListOutput<GetDeductionOutput>> {
        return GetAllDeductions(
            deductionRepo = deductionRepo,
            auth = auth
        )
    }

    @Bean
    fun updateDeduction(
        deductionRepo: IRepository<Deduction>,
        auth: IAuthenticateAdapter
    ): UseCase<UpdateDeductionInput, Unit> {
        return UpdateDeduction(
            deductionRepo = deductionRepo,
            auth = auth
        )
    }
}