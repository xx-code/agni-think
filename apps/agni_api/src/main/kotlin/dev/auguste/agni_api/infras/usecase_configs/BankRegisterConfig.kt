package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.BankRegister
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.bank_registers.CreateBankRegister
import dev.auguste.agni_api.core.usecases.bank_registers.DeleteBankRegister
import dev.auguste.agni_api.core.usecases.bank_registers.GetAllBankRegisters
import dev.auguste.agni_api.core.usecases.bank_registers.UpdateBankRegister
import dev.auguste.agni_api.core.usecases.bank_registers.dto.CreateBankRegisterInput
import dev.auguste.agni_api.core.usecases.bank_registers.dto.DeleteBankRegisterInput
import dev.auguste.agni_api.core.usecases.bank_registers.dto.GetBankRegisterOutput
import dev.auguste.agni_api.core.usecases.bank_registers.dto.UpdateBankRegisterInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BankRegisterConfig {
    @Bean
    fun createBankRegister(
        bankRegisterRepo: IRepository<BankRegister>,
        accountRepo: IRepository<Account>,
    ): IUseCase<CreateBankRegisterInput, CreatedOutput> {
        return CreateBankRegister(
            bankRegisterRepo,
            accountRepo = accountRepo
        )
    }

    @Bean
    fun deleteBankRegister(bankRegisterRepo: IRepository<BankRegister>): IUseCase<DeleteBankRegisterInput, Unit>  {
        return DeleteBankRegister(bankRegisterRepo)
    }

    @Bean
    fun getAllBankRegister(
        bankRegisterRepo: IRepository<BankRegister>,
        accountRepo: IRepository<Account>
    ): IUseCase<QueryFilter, ListOutput<GetBankRegisterOutput>> {
        return GetAllBankRegisters(
            bankRegisterRepo,
            accountRepo
        )
    }

    @Bean
    fun updateBankRegister(
        bankRegisterRepo: IRepository<BankRegister>,
        accountRepo: IRepository<Account>
    ) : IUseCase<UpdateBankRegisterInput, Unit> {
        return UpdateBankRegister(bankRegisterRepo, accountRepo)
    }
}