import { RequestEstimationLeftAmount } from "@core/interactions/analystic/estimationleftAmount";
import { RequestSuggestPlanningSaveGoal } from "@core/interactions/analystic/suggestPlanningSaveGoalUseCase";
import { Router } from "express";
import { body, matchedData, query, validationResult } from "express-validator";
import container from 'src/di_contenair';

const router = Router();

router.get('/v1/analytics/estimation-left-amount', 
    query('startDate').isDate().notEmpty(),
    query('endDate').isDate().notEmpty()
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
    body('wishSpends').isArray().optional(),
    body('wishSpends.*.amount').isNumeric(),
    body('wishSpends.*.description').isString(),
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