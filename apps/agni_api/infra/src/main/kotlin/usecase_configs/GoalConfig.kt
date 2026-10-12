package usecase_configs

import adapters.FinanceContext
import adapters.IAuthenticateAdapter
import adapters.IFinanceContext
import adapters.events.IEventRegister
import adapters.repositories.IRepository
import domain.entities.Category
import domain.entities.Goal
import domain.entities.Fund
import domain.entities.User
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
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
import usecases.UseCase
import usecases.dto.BackgroundTaskOut
import usecases.goals.SyncGoal
import java.util.UUID

@Configuration
class GoalConfig {

    @Bean
    fun financeContext(
        fundRepo: IRepository<Fund>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
        categoryRepo: IRepository<Category>,
        auth: IAuthenticateAdapter
    ): IFinanceContext {
        return FinanceContext(
            fundRepo = fundRepo,
            getBalance = getBalance,
            categoryRepo = categoryRepo,
            auth = auth
        )
    }

    @Bean
    fun createGoal(
        goalRepo: IRepository<Goal>,
        financeContext: IFinanceContext,
        auth: IAuthenticateAdapter
    ): UseCase<CreateGoalInput, CreatedOutput> {
        return CreateGoal(
            goalRepo = goalRepo,
            financeContext = financeContext,
            auth = auth
        )
    }

    @Bean
    fun getGoal(
        goalRepo: IRepository<Goal>,
        financeContext: IFinanceContext,
        auth: IAuthenticateAdapter
    ): UseCase<UUID, GetGoalOutput> {
        return GetGoal(
            goalRepo = goalRepo,
            financeContext = financeContext,
            auth = auth
        )
    }

    @Bean fun getAllGoal(
        goalRepo: IRepository<Goal>,
        financeContext: IFinanceContext,
        auth: IAuthenticateAdapter
    ): UseCase<GetAllGoalInput, ListOutput<GetGoalOutput>> {
        return GetAllGoals(
            goalRepo = goalRepo,
            financeContext = financeContext,
            auth = auth
        )
    }

    @Bean
    fun deleteGoal(
        goalRepo: IRepository<Goal>,
        auth: IAuthenticateAdapter
    ): UseCase<UUID, Unit> {
        return DeleteGoal(
            goalRepo,
            auth
        )
    }

    @Bean
    fun updateGoal(
        goalRepo: IRepository<Goal>,
        financeContext: IFinanceContext,
        auth: IAuthenticateAdapter
    ): UseCase<UpdateGoalInput, Unit> {
        return UpdateGoal(
            goalRepo,
            financeContext,
            auth
        )
    }

    @Bean
    fun syncGoal(
        goalRepo: IRepository<Goal>,
        financeContext: IFinanceContext,
        eventRegister: IEventRegister,
    ): UseCase<Unit, BackgroundTaskOut> {
        return SyncGoal(
            eventManager = eventRegister,
            goalRepo = goalRepo,
            financeContext = financeContext
        )
    }

}