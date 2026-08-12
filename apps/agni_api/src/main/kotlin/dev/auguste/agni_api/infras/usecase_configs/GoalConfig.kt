package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.FinanceContext
import dev.auguste.agni_api.core.adapters.IFinanceContext
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.entities.Goal
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.goals.CreateGoal
import dev.auguste.agni_api.core.usecases.goals.DeleteGoal
import dev.auguste.agni_api.core.usecases.goals.GetAllGoals
import dev.auguste.agni_api.core.usecases.goals.GetGoal
import dev.auguste.agni_api.core.usecases.goals.UpdateGoal
import dev.auguste.agni_api.core.usecases.goals.dto.CreateGoalInput
import dev.auguste.agni_api.core.usecases.goals.dto.GetAllGoalInput
import dev.auguste.agni_api.core.usecases.goals.dto.GetGoalOutput
import dev.auguste.agni_api.core.usecases.goals.dto.UpdateGoalInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class GoalConfig {

    @Bean
    fun financeContext(
        fundRepo: IRepository<SavingGoal>,
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