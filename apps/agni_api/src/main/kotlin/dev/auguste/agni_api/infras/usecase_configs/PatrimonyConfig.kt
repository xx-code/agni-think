package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Patrimony
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput
import dev.auguste.agni_api.core.usecases.patrimonies.CreatePatrimony
import dev.auguste.agni_api.core.usecases.patrimonies.DeletePatrimony
import dev.auguste.agni_api.core.usecases.patrimonies.GetAllPatrimonies
import dev.auguste.agni_api.core.usecases.patrimonies.GetPatrimony
import dev.auguste.agni_api.core.usecases.patrimonies.UpdatePatrimony
import dev.auguste.agni_api.core.usecases.patrimonies.dto.CreatePatrimonyInput
import dev.auguste.agni_api.core.usecases.patrimonies.dto.DeletePatrimonyInput
import dev.auguste.agni_api.core.usecases.patrimonies.dto.GetPatrimonyOutput
import dev.auguste.agni_api.core.usecases.patrimonies.dto.UpdatePatrimonyInput
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.AddSnapshotToPatrimony
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.GetAllSnapshotFromPatrimony
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.RemoveSnapshotFromPatrimony
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.UpdateSnapshotFromPatrimony
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.AddSnapshotToPatrimonyInput
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.GetAllSnapshotPatrimonyInput
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.GetSnapshotPatrimonyOutput
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.RemoveSnapshotFromPatrimonyInput
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.UpdateSnapshotFromPatrimonyInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class PatrimonyConfig {

    @Bean
    fun createPatrimony(
        patrimonyRepo: IRepository<Patrimony>,
        accountRepo: IRepository<Account>,
        snapshotRepo: IRepository<PatrimonySnapshot>,
        unitOfWork: IUnitOfWork
    ): IUseCase<CreatePatrimonyInput, CreatedOutput> {
        return CreatePatrimony(
            patrimonyRepo = patrimonyRepo,
            accountRepo = accountRepo,
            snapshotRepo = snapshotRepo,
            unitOfWork = unitOfWork
        )
    }

    @Bean
    fun deletePatrimony(
        patrimonyRepo: IRepository<Patrimony>,
        snapshotRepo: IRepository<PatrimonySnapshot>,
        unitOfWork: IUnitOfWork
    ): IUseCase<DeletePatrimonyInput, Unit> {
        return DeletePatrimony(
            patrimonyRepo = patrimonyRepo,
            patrimonySnapshotRepo = snapshotRepo,
            unitOfWork = unitOfWork
        )
    }

    @Bean
    fun getAllPatrimonies(
        patrimonyRepo: IRepository<Patrimony>,
        accountRepo: IRepository<Account>,
        snapshotRepo: IRepository<PatrimonySnapshot>,
        savingGoalRepo: IRepository<SavingGoal>,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceOutput>>
    ): IUseCase<QueryFilter, ListOutput<GetPatrimonyOutput>> {
        return GetAllPatrimonies(
            patrimonyRepo = patrimonyRepo,
            accountRepo = accountRepo,
            patrimonySnapshotRepo = snapshotRepo,
            savingGoalRepo = savingGoalRepo,
            getBalanceByPeriod = getBalanceByPeriod,
        )
    }

    @Bean
    fun getPatrimony(
        patrimonyRepo: IRepository<Patrimony>,
        accountRepo: IRepository<Account>,
        snapshotRepo: IRepository<PatrimonySnapshot>,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceOutput>>
    ): IUseCase<UUID, GetPatrimonyOutput> {
        return GetPatrimony(
            patrimonyRepo = patrimonyRepo,
            accountRepo = accountRepo,
            patrimonySnapshotRepo = snapshotRepo,
            getBalancesByPeriod = getBalanceByPeriod,
        )
    }

    @Bean
    fun updatePatrimony(
        patrimonyRepo: IRepository<Patrimony>,
        accountRepo: IRepository<Account>,
    ): IUseCase<UpdatePatrimonyInput, Unit> {
        return UpdatePatrimony(
            patrimonyRepo = patrimonyRepo,
            accountRepo = accountRepo,
        )
    }

    @Bean
    fun addSnapshotToPatrimonies(
       patrimonyRepo: IRepository<Patrimony>,
       snapshotRepo: IRepository<PatrimonySnapshot>
    ) : IUseCase<AddSnapshotToPatrimonyInput, CreatedOutput> {
        return AddSnapshotToPatrimony(
            patrimonyRepo = patrimonyRepo,
            snapshotPatrimonyRepo = snapshotRepo
        )
    }

    @Bean
    fun removeSnapshotFromPatrimonies(
        snapshotRepo: IRepository<PatrimonySnapshot>
    ) : IUseCase<RemoveSnapshotFromPatrimonyInput, Unit> {
        return RemoveSnapshotFromPatrimony(
            snapshotRepo = snapshotRepo
        )
    }

    @Bean
    fun updateSnapshotFromPatrimonies(
        snapshotRepo: IRepository<PatrimonySnapshot>
    ) : IUseCase<UpdateSnapshotFromPatrimonyInput, Unit> {
        return UpdateSnapshotFromPatrimony(
            snapshotRepo = snapshotRepo
        )
    }

    @Bean
    fun getAllSnapshotsFromPatrimonies(
       snapshotRepo: IRepository<PatrimonySnapshot>
    ) : IUseCase<GetAllSnapshotPatrimonyInput, ListOutput<GetSnapshotPatrimonyOutput>> {
        return GetAllSnapshotFromPatrimony(
            snapshotPatrimonyRepo = snapshotRepo
        )
    }
}