import { Router, Request, Response } from 'express';
import container from '../di_contenair'
import { body, matchedData, validationResult } from 'express-validator';
import { RequestCreationBudgetUseCase } from '@core/interactions/budgets/creationBudgetUseCase';
import { RequestUpdateBudget } from '@core/interactions/budgets/updateBudgetUseCase';

const router = Router();

router.post('/v1/budgets', 
    body('title').notEmpty().isString(),
    body('target').notEmpty().isNumeric(),
    body('schedule').notEmpty().isObject(),
    body('schedule.period').notEmpty().isString(),
    body('schedule.periodTime').optional().isNumeric(),
    body('schedule.dateStart').notEmpty().isDate(),
    body('schedule.dateEnd').optional().isDate(),
    async (req, res) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestCreationBudgetUseCase = matchedData(req);
                var created = await container.budgetUseCase?.createBudget.execute(data);
                
                res.status(200).send(created);
                return;
            }

            res.send({ errors: result.array() });
        } catch(err) {
            res.status(400).send({ errors: [ err ]});
        }
    });

router.put('/v1/budgets/:id', 
    body('title').optional().isString(),
    body('target').optional().isNumeric(),
    body('schedule').optional().isObject(),
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
            
            res.send({ errors: result.array() });
        } catch(err) {
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

router.get('/v1/budgets', async (req, res) => {
    try {
        var budgets = await container.budgetUseCase?.getAllBudgets.execute();
        res.status(200).send(budgets);
    } catch(err) {
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
