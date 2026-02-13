import { IUsecase } from "../interfaces"
import IAgentScoringGoal, { InputSaveGoalAgent } from "@core/agents/agentGoalScoring"
import IAgentPlanningAdvisor, { GoalPlanningAdvisor } from "@core/agents/agentPlanningAdvisor"
import { AccountType, ImportanceGoal, IntensityEmotionalDesir, Period } from "@core/domains/constants"
import { GetEstimationLeftAmoutDto, RequestEstimationLeftAmount } from "./estimationleftAmount"
import Repository from "@core/adapters/repository"
import { Account } from "@core/domains/entities/account"
import { SaveGoal } from "@core/domains/entities/saveGoal"
import { GetUID } from "@core/adapters/libs"
import { MomentDateService } from "@core/domains/entities/libs"

export type SuggestPlanningWishSpend = {
    amount: number
    description: string
}

export type SuggestGoalPlanning = {
    saveGoalId: string
    title: string
    amountSuggest: number
    reason: string
}

export type RequestSuggestPlanningSaveGoal = {
    estimationPeriodStart: Date,
    estimationPeriodEnd: Date
    amountToAllocate: number
    futureAmountToAllocate: number
    wishSpends?: SuggestPlanningWishSpend[]
}

export type GetAllSuggestPlanningDto = {
    suggestGoalPlanning: SuggestGoalPlanning[]  
    comment: string
} 

export class SuggestPlanningSaveGoalUseCase implements IUsecase<RequestSuggestPlanningSaveGoal, GetAllSuggestPlanningDto> {

    private estimationLeftAmountUseCase: IUsecase<RequestEstimationLeftAmount, GetEstimationLeftAmoutDto>

    private accountRepo: Repository<Account>;
    private saveGoalRepo: Repository<SaveGoal>;

    private goalRankingAgent: IAgentScoringGoal
    private planningAdvisor: IAgentPlanningAdvisor

    constructor( 
    estimationLeftAmountUseCase: IUsecase<RequestEstimationLeftAmount, GetEstimationLeftAmoutDto>,
    accountRepo: Repository<Account>,
    saveGoalRepo: Repository<SaveGoal>,
    goalRankingAgent: IAgentScoringGoal,
    planningAdvisor: IAgentPlanningAdvisor) {
        this.estimationLeftAmountUseCase = estimationLeftAmountUseCase
        this.accountRepo = accountRepo
        this.saveGoalRepo = saveGoalRepo
        this.goalRankingAgent = goalRankingAgent
        this.planningAdvisor = planningAdvisor
    }

    async execute(request: RequestSuggestPlanningSaveGoal): Promise<GetAllSuggestPlanningDto> {
        
        const workingStartDate = request.estimationPeriodStart;
        const workingEndDate = request.estimationPeriodEnd;

        const accounts = await this.accountRepo.getAll({
            offset: 0,
            limit: 0,
            queryAll: true, 
        });

        let currentInvestissment = 0
        let currentSaving = 0
        for(const account of accounts.items) {
            if (account.getType() == AccountType.BROKING)
                currentInvestissment += account.getBalance()

            if (account.getType() == AccountType.SAVING)
                currentSaving += account.getBalance()
        }

        const responseEstimationUc = await this.estimationLeftAmountUseCase.execute({
            startDate: workingStartDate,
            endDate: workingEndDate
        });

        // const estimationAmountToAllocate = responseEstimationUc.estimateAmount;

        // if (estimationAmountToAllocate <= 0)
        //     return {
        //         comment: "Vous n'avez pas d'argent a allouer desoler",
        //         suggestGoalPlanning: []
        //     };

        const saveGoalsForRankingAgent: InputSaveGoalAgent[] = []

        const saveGoals = await this.saveGoalRepo.getAll({ queryAll: true, offset: 0, limit: 0})

        for(const saveGoal of  saveGoals.items) {
            saveGoalsForRankingAgent.push({
                id: saveGoal.getId(),
                currentBalance: saveGoal.getBalance().getAmount(),
                desirValue: saveGoal.getDesirValue(),
                importance: saveGoal.getImportance(),
                target: saveGoal.getTarget().getAmount(),
                dueDate: saveGoal.getWishDueDate()
            });
        }
        if (request.wishSpends)
            for(const wishSpend of request.wishSpends) {
                saveGoalsForRankingAgent.push({
                    id: GetUID(),
                    currentBalance: 0,
                    desirValue: IntensityEmotionalDesir.OBSESSION,
                    dueDate: MomentDateService.getUTCDateAddition(new Date(), Period.DAY, 2),
                    importance: ImportanceGoal.NORMAL,
                    target: wishSpend.amount
                })        
            }
        
        const resultRankings = await this.goalRankingAgent.process({
            allocateAmount: request.amountToAllocate,
            futureAllocateAmout: request.futureAmountToAllocate,
            saveGoals: saveGoalsForRankingAgent
        });

        const saveGoalsForAdvisorAgent: GoalPlanningAdvisor[] = []
        for(const ranking of resultRankings.goals) {
            const saveGoal = saveGoals.items.find(i => i.getId() === ranking.id)
            if (saveGoal && saveGoal.getBalance().getAmount() < saveGoal.getTarget().getAmount()) {
                saveGoalsForAdvisorAgent.push({
                    id: saveGoal.getId(),
                    currentBalance: saveGoal.getBalance().getAmount(),
                    description: saveGoal.getDescription(),
                    desirValue: saveGoal.getDesirValue(),
                    importance: saveGoal.getImportance(),
                    score: ranking.score,
                    target: saveGoal.getTarget().getAmount(),
                    wishDueDate: saveGoal.getWishDueDate()?.toISOString()
                })
            } 
        }

        const resultPlannings = await this.planningAdvisor.process({
            amountToAllocate: request.amountToAllocate,
            futureAmountToAllocate: request.futureAmountToAllocate,
            currentAmountInInvestissment: currentInvestissment,
            currentAmountInSaving: currentSaving,
            income: responseEstimationUc.balanceScheduleIncome,
            percentForSavingAndInvestissment: 10,
            goals: saveGoalsForAdvisorAgent,
            wishSpends: request.wishSpends?.map(i => ({ amount: i.amount, description: i.description}))  || []    
        });

        const suggestGoals: SuggestGoalPlanning[] = []

        for(const suggestGoal of resultPlannings.goals) {
            const saveGoal = saveGoals.items.find(i => i.getId() === suggestGoal.id)
            if (saveGoal) {
                suggestGoals.push({
                    saveGoalId: suggestGoal.id,
                    title: saveGoal.getTitle(),
                    amountSuggest: suggestGoal.suggestAmount,
                    reason: suggestGoal.reasoning
                })
            }
        }

        return {
            comment: resultPlannings.comment,
            suggestGoalPlanning: suggestGoals
        }
    }

}