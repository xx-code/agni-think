import { QueryFilter } from '@core/dto/base';
import { RequestCreateScheduleTransaction } from '@core/interactions/scheduleTransaction/createScheduleTransaction';
import { RequestUpdateScheduleTransaction } from '@core/interactions/scheduleTransaction/updateScheduleTransaction';
import { Router, Request, Response } from 'express';
import { body, matchedData, query, validationResult } from 'express-validator';
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
    body('categoryId').optional().isString(),
    body('isFreeze').isBoolean().default(false),
    body('description').notEmpty().isString(),
    body('name').notEmpty().isString(),
    body('schedule').notEmpty().isObject(),
    body('schedule.repeater').optional().isObject(),
    body('schedule.repeater.period').notEmpty().isString(),
    body('schedule.repeater.interval').notEmpty().isNumeric(),
    body('schedule.dueDate').notEmpty().isISO8601().toDate(),
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
            res.status(400).send({ errors: result.array() });
        } catch(err) {
            console.log(err)
            res.status(400).send({ errors: [err] });
        }         
    });

router.get(
    '/v1/schedule-transactions', 
    query('limit').isNumeric().toInt(),
    query('offset').isNumeric().toInt(),
    query('queryAll').optional().isBoolean().toBoolean(),
    async (req, res) => {
    try {
        const result = validationResult(req);

        if (!result.isEmpty()) {
            res.send({ errors: result.array() });
            return;
        }

        const request: QueryFilter = matchedData(req)

        var schedules = await container.scheduleTransactionUseCase?.getAllScheduleTransaction.execute(request);
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
    body('schedule.repeater').optional().isObject(),
    body('schedule.repeater.period').notEmpty().isString(),
    body('schedule.repeater.interval').notEmpty().isNumeric(),
    body('schedule.dueDate').notEmpty().isISO8601().toDate(),
    body('tagIds').optional().isArray(),
    body('type').optional().isString(),
    async (req: Request, res: Response) => {
        try {
            const result = validationResult(req);
            if (!result.isEmpty()) {
                console.log(result)
                res.status(400).send({ errors: result.array() });
                return;
            } 

            const data: RequestUpdateScheduleTransaction = matchedData(req);
            data.id = req.params.id as string;
            await container.scheduleTransactionUseCase?.updateScheduleTransaction.execute(data);

            res.sendStatus(200);
        } catch(err) {
            console.log(err)
            res.status(400).send({ errors: [err] })
        } 
    });


export default router;