package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.provisionable.CreateProvisionable
import dev.auguste.agni_api.core.usecases.provisionable.DeleteProvisionable
import dev.auguste.agni_api.core.usecases.provisionable.GetAllProvisionable
import dev.auguste.agni_api.core.usecases.provisionable.GetProvision
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
        provisionRepo: IRepository<Provision>
    ): IUseCase<CreateProvisionableInput, CreatedOutput> {
        return CreateProvisionable(
            provisionRepo = provisionRepo
        )
    }

    @Bean
    fun updateProvisionable(
        provisionRepo: IRepository<Provision>
    ): IUseCase<UpdateProvisionableInput, Unit> {
        return UpdateProvisionable(
            provisionRepo = provisionRepo
        )
    }

    @Bean
    fun deleteProvisionable(
        provisionRepo: IRepository<Provision>
    ): IUseCase<DeleteProvisionableInput, Unit> {
        return DeleteProvisionable(
            provisionRepo = provisionRepo
        )
    }

    @Bean
    fun getProvisionable(
        provisionRepo: IRepository<Provision>
    ): IUseCase<UUID, GetProvisionableOutput> {
        return GetProvision(
            provisionRepo = provisionRepo
        )
    }

    @Bean
    fun getAllProvisionables(
        provisionRepo: IRepository<Provision>
    ): IUseCase<QueryFilter, ListOutput<GetProvisionableOutput>> {
       return GetAllProvisionable(
           provisionRepo = provisionRepo
       )
    }
}