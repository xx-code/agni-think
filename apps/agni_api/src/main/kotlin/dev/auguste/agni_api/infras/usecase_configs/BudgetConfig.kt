package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.budgets.CreateBudget
import dev.auguste.agni_api.core.usecases.budgets.DeleteBudget
import dev.auguste.agni_api.core.usecases.budgets.GetAllBudgets
import dev.auguste.agni_api.core.usecases.budgets.GetBudget
import dev.auguste.agni_api.core.usecases.budgets.UpdateBudget
import dev.auguste.agni_api.core.usecases.budgets.UpdateDueBudget
import dev.auguste.agni_api.core.usecases.budgets.dto.CreateBudgetInput
import dev.auguste.agni_api.core.usecases.budgets.dto.DeleteBudgetInput
import dev.auguste.agni_api.core.usecases.budgets.dto.GetBudgetOutput
import dev.auguste.agni_api.core.usecases.budgets.dto.UpdateBudgetInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class BudgetConfig {
    
    @Bean
    fun createBudget(
        budgetRepo: IRepository<Budget>,
        savingGoalRepo: IRepository<SavingGoal>,
    ) : IUseCase<CreateBudgetInput, CreatedOutput> {
        return CreateBudget(
            budgetRepo = budgetRepo,
            savingGoalRepo = savingGoalRepo,
        )
    }
    
    @Bean 
    fun updateBudget(
        budgetRepo : IRepository<Budget>,
        savingGoalRepo: IRepository<SavingGoal>,
    ) : IUseCase<UpdateBudgetInput, Unit> {
        return UpdateBudget(
            budgetRepo = budgetRepo,
            savingGoalRepo = savingGoalRepo,
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
        savingGoalRepo: IRepository<SavingGoal>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    ) : IUseCase<UUID, GetBudgetOutput> {
        return GetBudget(
            budgetRepo = budgetRepo,
            savingGoalRepo = savingGoalRepo,
            getBalance = getBalance,
        )
    }
    
    @Bean
    fun getAllBudgets(
        budgetRepo : IRepository<Budget>,
        savingGoalRepo: IRepository<SavingGoal>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    ) : IUseCase<QueryFilter, ListOutput<GetBudgetOutput>> {
        return GetAllBudgets(
            budgetRepo = budgetRepo,
            savingGoalRepo = savingGoalRepo,
            getBalance = getBalance
        )
    }

    @Bean
    fun updateDueBudget(
        budgetRepo : IRepository<Budget>,
        eventRegister: IEventRegister
    ): IUseCase<Unit, BackgroundTaskOut> {
        return UpdateDueBudget(
            budgetRepo = budgetRepo,
            eventRegister = eventRegister,
        )
    }
}