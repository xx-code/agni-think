package usecase_configs

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.Account
import domain.entities.BankRegister
import usecases.CreatedOutput
import usecases.ListOutput
import usecases.bank_registers.CreateBankRegister
import usecases.bank_registers.DeleteBankRegister
import usecases.bank_registers.GetAllBankRegisters
import usecases.bank_registers.GetBankRegisterByAccess
import usecases.bank_registers.UpdateBankRegister
import usecases.bank_registers.dto.CreateBankRegisterInput
import usecases.bank_registers.dto.DeleteBankRegisterInput
import usecases.bank_registers.dto.GetBankRegisterByAccessCodeInput
import usecases.bank_registers.dto.GetBankRegisterOutput
import usecases.bank_registers.dto.UpdateBankRegisterInput
import usecases.interfaces.IUseCase
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
    fun getBankRegisterByAccessCode(
        accountRepo: IRepository<Account>,
        bankRegisterRepo: IRepository<BankRegister>
    ): IUseCase<GetBankRegisterByAccessCodeInput, GetBankRegisterOutput> {
        return GetBankRegisterByAccess(
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