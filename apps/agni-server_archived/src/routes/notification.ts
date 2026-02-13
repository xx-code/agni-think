import { RequestPushNotification } from "@core/interactions/notification/pushNotification";
import { Router } from "express";
import { body, matchedData, query, validationResult } from "express-validator";
import container from '../di_contenair';
import { QueryFilter } from "@core/dto/base";
import { NotificationQueryFilter } from "@core/interactions/notification/getAllNotication";

const router = Router();

router.post(
    '/v1/push-notification', 
    body('title').isString(),
    body('content').isString(),
    body('dateTitle').notEmpty().isISO8601().toDate(),
    async (req, res) => {
        try {
            const result = validationResult(req);
            if (!result.isEmpty()) {
                res.status(400).send({errors: result.array()});
                return;
            }

            const data: RequestPushNotification = matchedData(req);
            var created = await container.notificationUseCase?.pushNotification.execute(data);

            res.status(200).json(created)
        } catch(err) {
            res.status(400).send({errors: [err]});
        }
    }
)

router.get(
    '/v1/notifications', 
    query('limit').isNumeric().toInt(),
    query('offset').isNumeric().toInt(),
    query('queryAll').optional().isBoolean().toBoolean(),
    query('isRead').optional().isBoolean().toBoolean(),
    async (req, res) => {
        try {
            const data: NotificationQueryFilter = matchedData(req);
            const result = await container.notificationUseCase?.getAllNotification.execute(data)
            res.status(200).json(result)
        } catch(err) {
            console.log(err)
            res.status(400).send({errors: [err]});
        }
    }
)

router.delete(
    `/v1/notifications/:id`,
    async (req, res) => {
        try {
            await container.notificationUseCase?.deleteNotification.execute(req.params.id)
            res.sendStatus(200)
        } catch(err) {
            res.status(400).send({errors: [err]});
        }
    }
)

router.put(
    '/v1/notifications/:id/toggle-read',
    async (req, res) => {
        try {
            await container.notificationUseCase?.toggleReadNotification.execute(req.params.id)
            res.sendStatus(200)
        } catch(err) {
            console.log(err)
            res.status(400).send({errors: [err]});
        }
    }
)

export default router