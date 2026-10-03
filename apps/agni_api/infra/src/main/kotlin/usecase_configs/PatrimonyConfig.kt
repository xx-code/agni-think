package usecase_configs

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Account
import domain.entities.Invoice
import domain.entities.Patrimony
import domain.entities.PatrimonySnapshot
import domain.entities.Provision
import domain.entities.Fund
import usecases.CreatedOutput
import usecases.ListOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceByPeriodOutput
import usecases.invoices.dto.GetBalancesByPeriodInput
import usecases.invoices.dto.GetInvoiceOutput
import usecases.patrimonies.CreatePatrimony
import usecases.patrimonies.DeletePatrimony
import usecases.patrimonies.GetAllPatrimonies
import usecases.patrimonies.GetPatrimony
import usecases.patrimonies.UpdatePatrimony
import usecases.patrimonies.dto.CreatePatrimonyInput
import usecases.patrimonies.dto.DeletePatrimonyInput
import usecases.patrimonies.dto.GetPatrimonyInput
import usecases.patrimonies.dto.GetPatrimonyOutput
import usecases.patrimonies.dto.UpdatePatrimonyInput
import usecases.patrimonies.snapshots.AddSnapshotToPatrimony
import usecases.patrimonies.snapshots.GetAllSnapshotFromPatrimony
import usecases.patrimonies.snapshots.RemoveSnapshotFromPatrimony
import usecases.patrimonies.snapshots.UpdateSnapshotFromPatrimony
import usecases.patrimonies.snapshots.dto.AddSnapshotToPatrimonyInput
import usecases.patrimonies.snapshots.dto.GetAllSnapshotPatrimonyInput
import usecases.patrimonies.snapshots.dto.GetSnapshotPatrimonyOutput
import usecases.patrimonies.snapshots.dto.RemoveSnapshotFromPatrimonyInput
import usecases.patrimonies.snapshots.dto.UpdateSnapshotFromPatrimonyInput
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
        fundRepo: IRepository<Fund>,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>,
        provisionRepo: IRepository<Provision>,
        invoiceRepo: IRepository<Invoice>,
        getManyInvoices: IUseCase<Set<UUID>, List<GetInvoiceOutput>>
    ): IUseCase<QueryFilter, ListOutput<GetPatrimonyOutput>> {
        return GetAllPatrimonies(
            patrimonyRepo = patrimonyRepo,
            accountRepo = accountRepo,
            patrimonySnapshotRepo = snapshotRepo,
            getBalanceByPeriod = getBalanceByPeriod,
            fundRepo = fundRepo,
            provisionRepo = provisionRepo,
            invoiceRepo = invoiceRepo,
            getManyInvoices = getManyInvoices,
        )
    }

    @Bean
    fun getPatrimony(
        patrimonyRepo: IRepository<Patrimony>,
        accountRepo: IRepository<Account>,
        fundRepo: IRepository<Fund>,
        snapshotRepo: IRepository<PatrimonySnapshot>,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>,
        provisionRepo: IRepository<Provision>,
        invoiceRepo: IRepository<Invoice>,
        getManyInvoices: IUseCase<Set<UUID>, List<GetInvoiceOutput>>
    ): IUseCase<GetPatrimonyInput, GetPatrimonyOutput> {
        return GetPatrimony(
            patrimonyRepo = patrimonyRepo,
            accountRepo = accountRepo,
            patrimonySnapshotRepo = snapshotRepo,
            fundRepo = fundRepo,
            getBalancesByPeriod = getBalanceByPeriod,
            provisionRepo = provisionRepo,
            invoiceRepo = invoiceRepo,
            getManyInvoices = getManyInvoices
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
       snapshotRepo: IRepository<PatrimonySnapshot>,
    ) : IUseCase<AddSnapshotToPatrimonyInput, CreatedOutput> {
        return AddSnapshotToPatrimony(
            patrimonyRepo = patrimonyRepo,
            snapshotPatrimonyRepo = snapshotRepo,
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
        snapshotRepo: IRepository<PatrimonySnapshot>,
        fundRepo: IRepository<Fund>,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>,
        provisionRepo: IRepository<Provision>,
        invoiceRepo: IRepository<Invoice>,
        getManyInvoices: IUseCase<Set<UUID>, List<GetInvoiceOutput>>
    ) : IUseCase<GetAllSnapshotPatrimonyInput, ListOutput<GetSnapshotPatrimonyOutput>> {
        return GetAllSnapshotFromPatrimony(
            snapshotPatrimonyRepo = snapshotRepo,
            fundRepo = fundRepo,
            getBalanceByPeriod = getBalanceByPeriod,
            provisionRepo = provisionRepo,
            invoiceRepo = invoiceRepo,
            getManyInvoices = getManyInvoices
        )
    }
}