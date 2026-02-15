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
import dev.auguste.agni_api.core.usecases.provisionable.dto.CreateProvisionInput
import dev.auguste.agni_api.core.usecases.provisionable.dto.DeleteProvisionInput
import dev.auguste.agni_api.core.usecases.provisionable.dto.GetProvisionOutput
import dev.auguste.agni_api.core.usecases.provisionable.dto.UpdateProvisionInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class ProvisionConfig {

    @Bean
    fun createProvision(
        provisionRepo: IRepository<Provision>
    ): IUseCase<CreateProvisionInput, CreatedOutput> {
        return CreateProvisionable(
            provisionRepo = provisionRepo
        )
    }

    @Bean
    fun updateProvision(
        provisionRepo: IRepository<Provision>
    ): IUseCase<UpdateProvisionInput, Unit> {
        return UpdateProvisionable(
            provisionRepo = provisionRepo
        )
    }

    @Bean
    fun deleteProvision(
        provisionRepo: IRepository<Provision>
    ): IUseCase<DeleteProvisionInput, Unit> {
        return DeleteProvisionable(
            provisionRepo = provisionRepo
        )
    }

    @Bean
    fun getProvision(
        provisionRepo: IRepository<Provision>
    ): IUseCase<UUID, GetProvisionOutput> {
        return GetProvision(
            provisionRepo = provisionRepo
        )
    }

    @Bean
    fun getAllProvision(
        provisionRepo: IRepository<Provision>
    ): IUseCase<QueryFilter, ListOutput<GetProvisionOutput>> {
       return GetAllProvisionable(
           provisionRepo = provisionRepo
       )
    }
}