import { Router, Request, Response } from 'express';
import container from '../di_contenair'
import { body, matchedData, query, validationResult } from 'express-validator';
import { RequestCreationBudgetUseCase } from '@core/interactions/budgets/creationBudgetUseCase';
import { RequestUpdateBudget } from '@core/interactions/budgets/updateBudgetUseCase';
import { QueryFilter } from '@core/dto/base';

const router = Router();

router.post('/v1/budgets', 
    body('title').notEmpty().isString(),
    body('target').notEmpty().isNumeric(),
    body('saveGoalIds').toArray(),
    body('schedule').notEmpty().isObject(),
    body('schedule.repeater').optional().isObject(),
    body('schedule.repeater.period').notEmpty().isString(),
    body('schedule.repeater.period.interval').notEmpty().isNumeric(),
    body('schedule.dueDate').notEmpty().isISO8601().toDate(),
    async (req, res) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestCreationBudgetUseCase = matchedData(req);
                var created = await container.budgetUseCase?.createBudget.execute(data);
                
                res.status(200).send(created);
                return;
            }

            res.status(400).send({ errors: result.array() });
        } catch(err) {
            res.status(400).send({ errors: [ err ]});
        }
    });

router.put('/v1/budgets/:id', 
    body('title').optional().isString(),
    body('target').optional().isNumeric(),
    body('saveGoalIds').optional().toArray(),
    body('schedule').optional().isObject(),
    body('schedule.repeater').optional().isObject(),
    body('schedule.repeater.period').notEmpty().isString(),
    body('schedule.repeater.period.interval').notEmpty().isNumeric(),
    body('schedule.dueDate').notEmpty().isISO8601().toDate(),
    async (req: Request, res: Response) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestUpdateBudget = matchedData(req);
                data.id = req.params.id;
                await container.budgetUseCase?.updateBudget.execute(data);

                res.sendStatus(200);
                return;
            }
            
            res.status(400).send({ errors: result.array() });
        } catch(err) {
            console.log(err)
            res.status(400).send({ errors: [ err ]});
        }
    });

router.get('/v1/budgets/:id', async (req, res) => {
    try {
        var budget = await container.budgetUseCase?.getBudget.execute(req.params.id);        

        res.status(200).send(budget);
    } catch(err) {
        res.status(400).send({ errors: [ err ]});
    }
});

router.get(
    '/v1/budgets', 
    query('limit').isNumeric().toInt(),
    query('offset').isNumeric().toInt(),
    query('queryAll').optional().isBoolean().toBoolean(),
    query('sortBy').optional().isString(),
    query('sortSense').optional().isString(),
    async (req, res) => {
    try {
        const result = validationResult(req);
        
        if (!result.isEmpty()) {
            res.send({ errors: result.array() });
            return;
        }

        const request: QueryFilter = matchedData(req)

        var budgets = await container.budgetUseCase?.getAllBudgets.execute(request);
        res.status(200).send(budgets);
    } catch(err) {
        console.log(err)
        res.status(400).send({ errors: [ err ]});
    }
});

router.delete('/v1/budgets/:id', async (req, res) => {
    try {
        await container.budgetUseCase?.deleteBudget.execute(req.params.id);
        res.sendStatus(200);
    } catch(err) {
        res.status(400).send({ errors: [ err ]});
    }
});

export default router;
