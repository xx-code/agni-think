import { RequestAddSaveGoalUseCase } from '@core/interactions/saveGoal/addSaveGoal';
import { RequestDecreaseSaveGoal } from '@core/interactions/saveGoal/decreaseSaveGoal';
import { RequestDeleteSaveGoal } from '@core/interactions/saveGoal/deleteSaveGoal';
import { RequestIncreaseSaveGoal } from '@core/interactions/saveGoal/increaseSaveGoal';
import { RequestUpdateSaveGoalUseCase } from '@core/interactions/saveGoal/updateSaveGoal';
import { Router, Request, Response } from 'express';
import { body, matchedData, query, validationResult } from 'express-validator';
import container from 'src/di_contenair';

const router = Router();

router.post('/v1/save-goals', 
    body('title').notEmpty().isString(),           
    body('description').notEmpty().isString(),           
    body('target').notEmpty().isNumeric(),           
    body('items').isArray(),
    body('desirValue').isNumeric(),
    body('importance').isNumeric(),
    body('wishDueDate').optional().isISO8601().toDate(),
    async (req, res) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestAddSaveGoalUseCase = matchedData(req);
                var created = await container.saveGoalUseCase?.addSaveGoal.execute(data);

                res.status(200).send(created);
                return;
            }
            
            res.status(400).send({ errors: result.array() });
        } catch(err) {
            res.status(400).send({ errors: [err] });
        }
    });

router.put('/v1/save-goals/:id', 
    body('title').optional().isString(),           
    body('description').optional().isString(),           
    body('target').optional().isNumeric(),           
    body('items').optional().isArray(),
    body('desirValue').optional().isNumeric(),
    body('importance').optional().isNumeric(),
    body('wishDueDate').optional().isISO8601().toDate(),
    async (req: Request, res: Response) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestUpdateSaveGoalUseCase = matchedData(req); 
                data.id = req.params.id;
                await container.saveGoalUseCase?.updateSaveGoal.execute(data);

                res.sendStatus(200);
                return;
            }
            
            res.status(400).send({ errors: result.array() });
        } catch(err) {
            res.status(400).send({ errors: [err] });
        } 
    });

router.get('/v1/save-goals/:id', async (req, res) => {
    try {
        var saveGoal = await container.saveGoalUseCase?.getSaveGoal.execute(req.params.id);
        res.status(200).send(saveGoal);
    } catch(err) {
        res.status(400).send({ errors: [err] })
    }
});

router.get('/v1/save-goals', async (req, res) => {
    try {
        var saveGoals = await container.saveGoalUseCase?.getAllSaveGoal.execute();
        res.status(200).send(saveGoals);
    } catch(err) {
        res.status(400).send({ errors: [err] })
    }
});

router.delete('/v1/save-goals/:id', body('accountDepositId').isString().notEmpty(), async (req: Request, res: Response) => {
    try {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestDeleteSaveGoal = matchedData(req);
            data.id = req.params.id;
            await container.saveGoalUseCase?.deleteSaveGoal.execute(data);
            
            res.sendStatus(200);
            return;
        }
        
        res.send({ errors: result.array() });
    } catch(err) {
        res.status(400).send({ errors: [err] })
    } 
});

router.patch('/v1/save-goals/:id/increase-balance', 
    body('accountId').notEmpty(),
    body('increaseAmount').notEmpty(),
    async (req: Request, res: Response) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestIncreaseSaveGoal = matchedData(req);
                data.id = req.params.id
                await container.saveGoalUseCase?.increaseSaveGoal.execute(data);

                res.sendStatus(200);
                return;
            }
            
            res.status(400).send({ errors: result.array() });
        } catch(err) {
            res.status(400).send({ errors: [err] })
        }
    });

router.patch('/v1/save-goals/:id/decrease-balance',
    body('accountId').notEmpty().isString(),
    body('decreaseAmount').notEmpty().isNumeric(),
    async (req: Request, res: Response) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestDecreaseSaveGoal = matchedData(req);
                data.id = req.params.id;
                await container.saveGoalUseCase?.decreaseSaveGoal.execute(data);

                res.sendStatus(200);
                return;
            }
            
            res.status(400).send({ errors: result.array() });
        } catch(err) {
            res.status(400).send({ errors: [err]});
        }
    });


export default router;