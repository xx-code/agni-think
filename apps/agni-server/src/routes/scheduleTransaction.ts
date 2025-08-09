import { RequestCreateScheduleTransaction } from '@core/interactions/scheduleTransaction/createScheduleTransaction';
import { RequestUpdateScheduleTransaction } from '@core/interactions/scheduleTransaction/updateScheduleTransaction';
import { Router, Request, Response } from 'express';
import { body, matchedData, validationResult } from 'express-validator';
import container from 'src/di_contenair';

const router = Router();

router.post('/v1/schedule-transactions/apply-schedule', async (req, res) => {
    try {
        await container.scheduleTransactionUseCase?.applyScheduleTransaction.execute();
        res.sendStatus(200);
    } catch(err) {
        res.status(400).send({ errors: [err] }); 
    }
});

router.post('/v1/schedule-transactions', 
    body('accountId').notEmpty().isString(),
    body('amount').notEmpty().isNumeric(),
    body('categoryId').notEmpty().isString(),
    body('description').notEmpty().isString(),
    body('name').notEmpty().isString(),
    body('schedule').notEmpty().isObject(),
    body('schedule.period').notEmpty().isString(),
    body('schedule.periodTime').optional().isNumeric(),
    body('schedule.dateStart').notEmpty().isDate(),
    body('schedule.dateEnd').optional().isDate(),
    body('tagIds').isArray(),
    body('type').notEmpty(),
    async (req, res) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestCreateScheduleTransaction = matchedData(req);
                var created = await container.scheduleTransactionUseCase?.createScheduleTransaction.execute(data);

                res.status(200).send(created);
                return;
            }
            res.send({ errors: result.array() });
        } catch(err) {
            res.status(400).send({ errors: [err] });
        }         
    });

router.get('/v1/schedule-transactions', async (req, res) => {
    try {
        var schedules = await container.scheduleTransactionUseCase?.getAllScheduleTransaction.execute();
        res.status(200).send(schedules);
    } catch(err) {
        res.status(400).send({ errors: [err] })
    }
});

router.get('/v1/schedule-transactions/:id', async (req, res) => {
    try {
        var schedule = await container.scheduleTransactionUseCase?.getScheduleTransaction.execute(req.params.id);
        res.status(200).send(schedule);
    } catch(err) {
        res.status(400).send({ errors: [err] })
    }
});

router.delete('/v1/schedule-transactions/:id', async (req, res) => {
    try {
        await container.scheduleTransactionUseCase?.deleteScheduleTransaction.execute(req.params.id);
        res.sendStatus(200);
    } catch(err) {
        res.status(400).send({ errors: [err] })
    }
});

router.put('/v1/schedule-transactions/:id', 
    body('accountId').optional().isString(),
    body('amount').optional().isNumeric(),
    body('categoryId').optional().isString(),
    body('description').optional().isString(),
    body('isPause').optional().isBoolean(),
    body('name').optional().isString(),
    body('schedule').optional().isObject(),
    // body('schedule.period').notEmpty().isString(),
    // body('schedule.periodTime').optional().isNumeric(),
    // body('schedule.dateStart').optional().isDate(),
    // body('schedule.dateEnd').notEmpty().isDate(),
    body('tagIds').optional().isArray(),
    body('type').optional().isString(),
    async (req: Request, res: Response) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestUpdateScheduleTransaction = matchedData(req);
                data.id = req.params.id;
                await container.scheduleTransactionUseCase?.updateScheduleTransaction.execute(data);

                res.sendStatus(200);
                return;
            } 

            res.send({ errors: result.array() });
        } catch(err) {
            res.status(400).send({ errors: [err] })
        } 
    });


export default router;