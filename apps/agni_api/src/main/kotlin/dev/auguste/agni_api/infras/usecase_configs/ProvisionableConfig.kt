package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Provisionable
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.provisionable.CreateProvisionable
import dev.auguste.agni_api.core.usecases.provisionable.DeleteProvisionable
import dev.auguste.agni_api.core.usecases.provisionable.GetAllProvisionable
import dev.auguste.agni_api.core.usecases.provisionable.GetProvisionable
import dev.auguste.agni_api.core.usecases.provisionable.UpdateProvisionable
import dev.auguste.agni_api.core.usecases.provisionable.dto.CreateProvisionableInput
import dev.auguste.agni_api.core.usecases.provisionable.dto.DeleteProvisionableInput
import dev.auguste.agni_api.core.usecases.provisionable.dto.GetProvisionableOutput
import dev.auguste.agni_api.core.usecases.provisionable.dto.UpdateProvisionableInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class ProvisionableConfig {

    @Bean
    fun createProvisionable(
        provisionableRepo: IRepository<Provisionable>
    ): IUseCase<CreateProvisionableInput, CreatedOutput> {
        return CreateProvisionable(
            provisionableRepo = provisionableRepo
        )
    }

    @Bean
    fun updateProvisionable(
        provisionableRepo: IRepository<Provisionable>
    ): IUseCase<UpdateProvisionableInput, Unit> {
        return UpdateProvisionable(
            provisionableRepo = provisionableRepo
        )
    }

    @Bean
    fun deleteProvisionable(
        provisionableRepo: IRepository<Provisionable>
    ): IUseCase<DeleteProvisionableInput, Unit> {
        return DeleteProvisionable(
            provisionableRepo = provisionableRepo
        )
    }

    @Bean
    fun getProvisionable(
        provisionableRepo: IRepository<Provisionable>
    ): IUseCase<UUID, GetProvisionableOutput> {
        return GetProvisionable(
            provisionableRepo = provisionableRepo
        )
    }

    @Bean
    fun getAllProvisionables(
        provisionableRepo: IRepository<Provisionable>
    ): IUseCase<QueryFilter, ListOutput<GetProvisionableOutput>> {
       return GetAllProvisionable(
           provisionableRepo = provisionableRepo
       )
    }
}