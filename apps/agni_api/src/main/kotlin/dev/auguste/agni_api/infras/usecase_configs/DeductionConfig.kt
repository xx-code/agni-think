package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Deduction
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.deductions.CreateDeduction
import dev.auguste.agni_api.core.usecases.deductions.DeleteDeduction
import dev.auguste.agni_api.core.usecases.deductions.GetAllDeductions
import dev.auguste.agni_api.core.usecases.deductions.GetDeduction
import dev.auguste.agni_api.core.usecases.deductions.UpdateDeduction
import dev.auguste.agni_api.core.usecases.deductions.dto.CreateDeductionInput
import dev.auguste.agni_api.core.usecases.deductions.dto.DeleteDeductionInput
import dev.auguste.agni_api.core.usecases.deductions.dto.GetDeductionOutput
import dev.auguste.agni_api.core.usecases.deductions.dto.UpdateDeductionInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class DeductionConfig {

    @Bean
    fun createDeduction(
        deductionRepo: IRepository<Deduction>,
    ): IUseCase<CreateDeductionInput, CreatedOutput> {
        return CreateDeduction(
            deductionRepo = deductionRepo
        )
    }

    @Bean
    fun deleteDeduction(
        deductionRepo: IRepository<Deduction>,
    ): IUseCase<DeleteDeductionInput, Unit> {
        return DeleteDeduction(
            deductionRepo = deductionRepo
        )
    }

    @Bean
    fun getDeduction(
        deductionRepo: IRepository<Deduction>,
    ): IUseCase<UUID, GetDeductionOutput> {
        return GetDeduction(
            deductionRepo = deductionRepo
        )
    }

    @Bean
    fun getAllDeductions(
        deductionRepo: IRepository<Deduction>,
    ): IUseCase<QueryFilter, ListOutput<GetDeductionOutput>> {
        return GetAllDeductions(
            deductionRepo = deductionRepo
        )
    }

    @Bean
    fun updateDeduction(
        deductionRepo: IRepository<Deduction>,
    ): IUseCase<UpdateDeductionInput, Unit> {
        return UpdateDeduction(
            deductionRepo = deductionRepo
        )
    }
}