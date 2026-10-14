package usecase_configs

import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Account
import domain.entities.Invoice
import domain.entities.Patrimony
import domain.entities.PatrimonySnapshot
import domain.entities.Provision
import domain.entities.Fund
import domain.entities.User
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
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
import usecases.UseCase
import usecases.patrimonies.dto.GetAllPatrimonyInput
import java.util.UUID

@Configuration
class PatrimonyConfig {

    @Bean
    fun createPatrimony(
        patrimonyRepo: IRepository<Patrimony>,
        accountRepo: IRepository<Account>,
        snapshotRepo: IRepository<PatrimonySnapshot>,
        unitOfWork: IUnitOfWork,
        auth: IAuthenticateAdapter
    ): UseCase<CreatePatrimonyInput, CreatedOutput> {
        return CreatePatrimony(
            patrimonyRepo = patrimonyRepo,
            accountRepo = accountRepo,
            snapshotRepo = snapshotRepo,
            unitOfWork = unitOfWork,
            auth = auth
        )
    }

    @Bean
    fun deletePatrimony(
        patrimonyRepo: IRepository<Patrimony>,
        snapshotRepo: IRepository<PatrimonySnapshot>,
        unitOfWork: IUnitOfWork,
        auth: IAuthenticateAdapter
    ): UseCase<DeletePatrimonyInput, Unit> {
        return DeletePatrimony(
            patrimonyRepo = patrimonyRepo,
            patrimonySnapshotRepo = snapshotRepo,
            unitOfWork = unitOfWork,
            auth = auth
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
        getManyInvoices: IUseCase<Set<UUID>, List<GetInvoiceOutput>>,
        auth: IAuthenticateAdapter
    ): UseCase<GetAllPatrimonyInput, ListOutput<GetPatrimonyOutput>> {
        return GetAllPatrimonies(
            patrimonyRepo = patrimonyRepo,
            accountRepo = accountRepo,
            patrimonySnapshotRepo = snapshotRepo,
            getBalanceByPeriod = getBalanceByPeriod,
            fundRepo = fundRepo,
            provisionRepo = provisionRepo,
            invoiceRepo = invoiceRepo,
            getManyInvoices = getManyInvoices,
            auth = auth,
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
        getManyInvoices: IUseCase<Set<UUID>, List<GetInvoiceOutput>>,
        auth: IAuthenticateAdapter
    ): UseCase<GetPatrimonyInput, GetPatrimonyOutput> {
        return GetPatrimony(
            patrimonyRepo = patrimonyRepo,
            accountRepo = accountRepo,
            patrimonySnapshotRepo = snapshotRepo,
            fundRepo = fundRepo,
            getBalancesByPeriod = getBalanceByPeriod,
            provisionRepo = provisionRepo,
            invoiceRepo = invoiceRepo,
            getManyInvoices = getManyInvoices,
            auth = auth
        )
    }

    @Bean
    fun updatePatrimony(
        patrimonyRepo: IRepository<Patrimony>,
        accountRepo: IRepository<Account>,
        auth: IAuthenticateAdapter
    ): UseCase<UpdatePatrimonyInput, Unit> {
        return UpdatePatrimony(
            patrimonyRepo = patrimonyRepo,
            accountRepo = accountRepo,
            auth = auth,
        )
    }

    @Bean
    fun addSnapshotToPatrimonies(
       patrimonyRepo: IRepository<Patrimony>,
       snapshotRepo: IRepository<PatrimonySnapshot>,
       auth: IAuthenticateAdapter
    ) : UseCase<AddSnapshotToPatrimonyInput, CreatedOutput> {
        return AddSnapshotToPatrimony(
            patrimonyRepo = patrimonyRepo,
            snapshotPatrimonyRepo = snapshotRepo,
            auth = auth,
        )
    }

    @Bean
    fun removeSnapshotFromPatrimonies(
        snapshotRepo: IRepository<PatrimonySnapshot>,
        auth: IAuthenticateAdapter
    ) : UseCase<RemoveSnapshotFromPatrimonyInput, Unit> {
        return RemoveSnapshotFromPatrimony(
            snapshotRepo = snapshotRepo,
            auth = auth
        )
    }

    @Bean
    fun updateSnapshotFromPatrimonies(
        snapshotRepo: IRepository<PatrimonySnapshot>,
        auth: IAuthenticateAdapter
    ) : UseCase<UpdateSnapshotFromPatrimonyInput, Unit> {
        return UpdateSnapshotFromPatrimony(
            snapshotRepo = snapshotRepo,
            auth = auth
        )
    }

    @Bean
    fun getAllSnapshotsFromPatrimonies(
        snapshotRepo: IRepository<PatrimonySnapshot>,
        fundRepo: IRepository<Fund>,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>,
        provisionRepo: IRepository<Provision>,
        invoiceRepo: IRepository<Invoice>,
        getManyInvoices: IUseCase<Set<UUID>, List<GetInvoiceOutput>>,
        auth: IAuthenticateAdapter
    ) : UseCase<GetAllSnapshotPatrimonyInput, ListOutput<GetSnapshotPatrimonyOutput>> {
        return GetAllSnapshotFromPatrimony(
            snapshotPatrimonyRepo = snapshotRepo,
            fundRepo = fundRepo,
            getBalanceByPeriod = getBalanceByPeriod,
            provisionRepo = provisionRepo,
            invoiceRepo = invoiceRepo,
            getManyInvoices = getManyInvoices,
            auth = auth
        )
    }
}