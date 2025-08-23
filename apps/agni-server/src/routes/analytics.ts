import { RequestEstimationLeftAmount } from "@core/interactions/analystic/estimationleftAmount";
import { RequestSuggestPlanningSaveGoal } from "@core/interactions/analystic/suggestPlanningSaveGoalUseCase";
import { Router } from "express";
import { body, matchedData, query, validationResult } from "express-validator";
import container from 'src/di_contenair';

const router = Router();

router.get('/v1/analytics/estimation-left-amount', 
    query('startDate').notEmpty().isISO8601().toDate(),
    query('endDate').notEmpty().isISO8601().toDate()
    , async (req, res) => {
    try {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestEstimationLeftAmount = matchedData(req);
            const result = await container.analyticUseCase?.estimateLeftAmount.execute(data);
            
            res.status(200).send(result);
            return;
        }

        res.status(400).send({ errors: result.array() });
    } catch(err) {
        console.log(err)
        res.status(400).send({ errors: [ err ]});
    }
});

router.post('/v1/analytics/save-goal-planning', 
    body('comment').isString().optional(),
    body('estimationPeriodStart').notEmpty().isISO8601().toDate(),
    body('estimationPeriodEnd').notEmpty().isISO8601().toDate(),
    body('wishGoals').isArray().optional(),
    body('wishGoals.*.goalId').exists().isString(),
    body('wishGoals.*.amountSuggest').exists().isNumeric(),
    body('wishSpends').isArray().optional(),
    body('wishSpends.*.amount').exists().isNumeric(),
    body('wishSpends.*.description').exists().isString(),
    async (req,  res) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestSuggestPlanningSaveGoal = matchedData(req);
                const result = await container.analyticUseCase?.planningSaveGoalAdvisor.execute(data);
                
                res.status(200).send(result);
                return;
        }

        res.status(400).send({ errors: result.array() });
    } catch(err) {
        console.log(err)
        res.status(400).send({ errors: [ err ]});
    }
});

export default router;