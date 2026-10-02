package dev.auguste.agni_api.infras.usecase_configs

import adapters.events.IEventRegister
import adapters.repositories.IRepository
import domain.entities.Budget
import domain.entities.Fund
import usecases.BackgroundTaskOut
import usecases.CreatedOutput
import usecases.ListOutput
import usecases.budgets.CreateBudget
import usecases.budgets.DeleteBudget
import usecases.budgets.GetAllBudgets
import usecases.budgets.GetBudget
import usecases.budgets.UpdateBudget
import usecases.budgets.UpdateDueBudget
import usecases.budgets.dto.CreateBudgetInput
import usecases.budgets.dto.DeleteBudgetInput
import usecases.budgets.dto.GetAllBudgetInput
import usecases.budgets.dto.GetBudgetOutput
import usecases.budgets.dto.UpdateBudgetInput
import usecases.interfaces.ISuspendableUseCase
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class BudgetConfig {
    
    @Bean
    fun createBudget(
        budgetRepo: IRepository<Budget>,
        fundRepo: IRepository<Fund>,
    ) : IUseCase<CreateBudgetInput, CreatedOutput> {
        return CreateBudget(
            budgetRepo = budgetRepo,
            fundRepo = fundRepo,
        )
    }
    
    @Bean 
    fun updateBudget(
        budgetRepo : IRepository<Budget>,
    ) : IUseCase<UpdateBudgetInput, Unit> {
        return UpdateBudget(
            budgetRepo = budgetRepo
        )
    }
    
    @Bean
    fun deleteBudget(
        budgetRepo : IRepository<Budget>,
    ) : IUseCase<DeleteBudgetInput, Unit> {
        return DeleteBudget(
            budgetRepo = budgetRepo,
        )
    }
    
    @Bean
    fun getBudget(
        budgetRepo : IRepository<Budget>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    ) : IUseCase<UUID, GetBudgetOutput> {
        return GetBudget(
            budgetRepo = budgetRepo,
            getBalance = getBalance
        )
    }
    
    @Bean
    fun getAllBudgets(
        budgetRepo : IRepository<Budget>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    ) : IUseCase<GetAllBudgetInput, ListOutput<GetBudgetOutput>> {
        return GetAllBudgets(
            budgetRepo = budgetRepo,
            getBalance = getBalance
        )
    }

    @Bean
    fun updateDueBudget(
        budgetRepo : IRepository<Budget>,
        eventRegister: IEventRegister
    ): ISuspendableUseCase<Unit, BackgroundTaskOut> {
        return UpdateDueBudget(
            budgetRepo = budgetRepo,
            eventRegister = eventRegister,
        )
    }
}