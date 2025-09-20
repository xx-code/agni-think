import IAgentScoringGoal, { AgentGoalScoringInput, AgentGoalScoringOutput } from "@core/agents/agentGoalScoring";

const moment = require('moment');

export default class AgentGoalScoring implements IAgentScoringGoal {
    process(input: AgentGoalScoringInput): Promise<AgentGoalScoringOutput> {
        const goalScorings: AgentGoalScoringOutput['goals'] = [];

        const resultScores: {score: number, id: string}[] = []
        for(const scoreGoal of input.saveGoals) {
            let nbPeriod = 0; 
            if (scoreGoal.dueDate)
                nbPeriod = moment(scoreGoal.dueDate.toISOString()).diff(moment(), 'week', true);

            let urgence_estimation = 0;

            if (nbPeriod > 0) {
                urgence_estimation =  (scoreGoal.target - scoreGoal.currentBalance) / (input.futureAllocateAmout * (nbPeriod/2));
            }

            const desir_estimation = Math.max(0, Math.min(1, 0.9 - (scoreGoal.desirValue - 1) * 1));

            const score = (urgence_estimation + desir_estimation + scoreGoal.importance) / (3 + 3 + 3);
            
            resultScores.push({
                id: scoreGoal.id,
                score: score
            })
        }

        for(const score of resultScores) {
            goalScorings.push({
                id: score.id,
                score: score.score,
                suggestAmount: input.allocateAmount 
            })
        }

        return Promise.resolve({ goals: goalScorings }) ;
    }
} 