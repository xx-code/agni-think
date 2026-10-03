package usecase_configs

import adapters.events.IEventRegister
import adapters.repositories.IRepository
import domain.entities.Budget
import usecases.dto.BackgroundTaskOut
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
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
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import usecases.UseCase
import java.util.UUID

@Configuration
class BudgetConfig {
    
    @Bean
    fun createBudget(
        budgetRepo: IRepository<Budget>,
    ) : UseCase<CreateBudgetInput, CreatedOutput> {
        return CreateBudget(
            budgetRepo = budgetRepo
        )
    }
    
    @Bean 
    fun updateBudget(
        budgetRepo : IRepository<Budget>,
    ) : UseCase<UpdateBudgetInput, Unit> {
        return UpdateBudget(
            budgetRepo = budgetRepo
        )
    }
    
    @Bean
    fun deleteBudget(
        budgetRepo : IRepository<Budget>,
    ) : UseCase<DeleteBudgetInput, Unit> {
        return DeleteBudget(
            budgetRepo = budgetRepo,
        )
    }
    
    @Bean
    fun getBudget(
        budgetRepo : IRepository<Budget>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    ) : UseCase<UUID, GetBudgetOutput> {
        return GetBudget(
            budgetRepo = budgetRepo,
            getBalance = getBalance
        )
    }
    
    @Bean
    fun getAllBudgets(
        budgetRepo : IRepository<Budget>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    ) : UseCase<GetAllBudgetInput, ListOutput<GetBudgetOutput>> {
        return GetAllBudgets(
            budgetRepo = budgetRepo,
            getBalance = getBalance
        )
    }

    @Bean
    fun updateDueBudget(
        budgetRepo : IRepository<Budget>,
        eventRegister: IEventRegister
    ): UseCase<Unit, BackgroundTaskOut> {
        return UpdateDueBudget(
            budgetRepo = budgetRepo,
            eventRegister = eventRegister,
        )
    }
}