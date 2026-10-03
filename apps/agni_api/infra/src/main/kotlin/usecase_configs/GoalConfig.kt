package usecase_configs

import adapters.FinanceContext
import adapters.IFinanceContext
import adapters.repositories.IRepository
import domain.entities.Category
import domain.entities.Goal
import domain.entities.Fund
import usecases.CreatedOutput
import usecases.ListOutput
import usecases.goals.CreateGoal
import usecases.goals.DeleteGoal
import usecases.goals.GetAllGoals
import usecases.goals.GetGoal
import usecases.goals.UpdateGoal
import usecases.goals.dto.CreateGoalInput
import usecases.goals.dto.GetAllGoalInput
import usecases.goals.dto.GetGoalOutput
import usecases.goals.dto.UpdateGoalInput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class GoalConfig {

    @Bean
    fun financeContext(
        fundRepo: IRepository<Fund>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
        categoryRepo: IRepository<Category>
    ): IFinanceContext {
        return FinanceContext(
            fundRepo = fundRepo,
            getBalance = getBalance,
            categoryRepo = categoryRepo
        )
    }

    @Bean
    fun createGoal(
        goalRepo: IRepository<Goal>,
        financeContext: IFinanceContext
    ): IUseCase<CreateGoalInput, CreatedOutput> {
        return CreateGoal(
            goalRepo = goalRepo,
            financeContext = financeContext
        )
    }

    @Bean
    fun getGoal(
        goalRepo: IRepository<Goal>,
        financeContext: IFinanceContext,
    ): IUseCase<UUID, GetGoalOutput> {
        return GetGoal(
            goalRepo = goalRepo,
            financeContext = financeContext
        )
    }

    @Bean fun getAllGoal(
        goalRepo: IRepository<Goal>,
        financeContext: IFinanceContext,
    ): IUseCase<GetAllGoalInput, ListOutput<GetGoalOutput>> {
        return GetAllGoals(
            goalRepo = goalRepo,
            financeContext = financeContext
        )
    }

    @Bean
    fun deleteGoal(
        goalRepo: IRepository<Goal>
    ): IUseCase<UUID, Unit> {
        return DeleteGoal(
            goalRepo
        )
    }

    @Bean
    fun updateGoal(
        goalRepo: IRepository<Goal>,
        financeContext: IFinanceContext
    ): IUseCase<UpdateGoalInput, Unit> {
        return UpdateGoal(
            goalRepo,
            financeContext
        )
    }

}