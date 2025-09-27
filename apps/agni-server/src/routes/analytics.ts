import { RequestCashFlow } from "@core/interactions/analystic/cashflowAnalyse";
import { RequestEstimationLeftAmount } from "@core/interactions/analystic/estimationleftAmount";
import { RequestIncomAnalystic } from "@core/interactions/analystic/income";
import { RequestSavingAnalystic } from "@core/interactions/analystic/savings";
import { RequestSpendAnalystic } from "@core/interactions/analystic/spends";
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
    body('amountToAllocate').isNumeric().toFloat(),
    body('futureAmountToAllocate').isNumeric().toFloat(),
    body('estimationPeriodStart').notEmpty().isISO8601().toDate(),
    body('estimationPeriodEnd').notEmpty().isISO8601().toDate(),
    body('wishSpends').optional().isArray(),
    // body('wishSpends.*.amount').exists().isNumeric(),
    // body('wishSpends.*.description').exists().isString(),
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

router.get('/v1/analytics/cashflow', 
    query('period').isString(),
    query('periodTime').isNumeric(),
    query('showNumber').isNumeric(),
    async (req, res ) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestCashFlow = matchedData(req);
                const result = await container.analyticUseCase?.cashflowAnalyse.execute(data);
                res.status(200).send(result);
                return;
            }

            res.status(400).send({ errors: result.array() });
        } catch(err) {
            console.log(err)
            res.status(400).send({ errors: [ err ]});
        }
    }
)

router.get('/v1/analytics/budget-rules', 
    query('period').isString(),
    query('periodTime').isNumeric(),
    query('showNumber').isNumeric(),
    async (req, res) => {
        try {
            const result = validationResult(req);
            if (!result.isEmpty()) { 
                res.status(400).send({ errors: result.array() });
                return;
            }

            const data: RequestCashFlow = matchedData(req);
            const budgets = await container.analyticUseCase?.analyseBudgetRule.execute(data);

            res.status(200).send(budgets);
        } catch(err) {
            console.log(err)
            res.status(400).send({ errors: [ err ]});
        }
    }
)

router.get('/v1/analytics/incomes', 
    query('period').isString(),
    query('periodTime').isNumeric(),
    query('showNumber').isNumeric(),
    async (req, res) => {
        try {
            const result = validationResult(req);
            if (!result.isEmpty()) { 
                res.status(400).send({ errors: result.array() });
                return;
            }

            const data: RequestIncomAnalystic = matchedData(req);
            const incomes = await container.analyticUseCase?.incomeAnalystic.execute(data);

            res.status(200).send(incomes);
        } catch(err) {
            console.log(err)
            res.status(400).send({ errors: [ err ]});
        }
    }
)

router.get('/v1/analytics/savings', 
    query('period').isString(),
    query('periodTime').isNumeric(),
    query('showNumber').isNumeric(),
    async (req, res) => {
        try {
            const result = validationResult(req);
            if (!result.isEmpty()) { 
                res.status(400).send({ errors: result.array() });
                return;
            }

            const data: RequestSavingAnalystic = matchedData(req);
            const savings = await container.analyticUseCase?.savingAnalystic.execute(data);

            res.status(200).send(savings);
        } catch(err) {
            console.log(err)
            res.status(400).send({ errors: [ err ]});
        }
    }
)

router.get('/v1/analytics/spends', 
    query('period').isString(),
    query('periodTime').isNumeric(),
    query('showNumber').isNumeric(),
    async (req, res) => {
        try {
            const result = validationResult(req);
            if (!result.isEmpty()) { 
                res.status(400).send({ errors: result.array() });
                return;
            }

            const data: RequestSpendAnalystic = matchedData(req);
            const spends = await container.analyticUseCase?.spendAnalystic.execute(data);

            res.status(200).send(spends);
        } catch(err) {
            console.log(err)
            res.status(400).send({ errors: [ err ]});
        }
    }
)

export default router;