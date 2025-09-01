import { RequestCreatePatrimony } from "@core/interactions/patrimony/createPatrimony";
import { Router, Request, Response } from "express";
import { body, matchedData, query, validationResult } from "express-validator";
import container from '../di_contenair';
import { RequestUpdatePatrimony } from "@core/interactions/patrimony/updatePatrimony";
import { RequestAddSnapshotPatrimony } from "@core/interactions/patrimony/addSnapshotToPatrimony";
import { RequestUpdateSnapshotPatrimony } from "@core/interactions/patrimony/updateSnapshotPatrimony";
import { RequestAllSnapshotPatrimony } from "@core/interactions/patrimony/getAllSnapshotOfPatrimony";
import { RequestGetPatrimony } from "@core/interactions/patrimony/getPatrimony";
import { RequestGetAllPatrimony } from "@core/interactions/patrimony/getAllPatrimony";

const router = Router();

router.post('/v1/patrimonies', 
    body('title').notEmpty().isString(),
    body('amount').notEmpty().isNumeric(),
    body('type').notEmpty().isString(),
    body('accountIds').optional().isArray(),
async (req, res) => {
    try {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestCreatePatrimony = matchedData(req);
            var created = await container.patrimonyUseCase?.createPatrimony.execute(data);

            res.status(200).json(created)
            return;
        }

        console.log(result.array())
        res.status(400).send({errors: result.array()});
    } catch(err) {
        console.log(err)
        res.status(400).send({errors: [err]});
    }
});
 
router.put('/v1/patrimonies/:id', 
    body('title').optional().isString(),
    body('type').optional().isString(),
    body('amount').optional().isNumeric(),
    body('accountIds').optional().isArray(),
    async (req: Request, res: Response) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestUpdatePatrimony = matchedData(req);
                data.patrimonyId = req.params.id
                await container.patrimonyUseCase?.updatePatrimony.execute(data);

                res.sendStatus(200)
                return;
            }

            console.log(result.array())
            res.status(400).send({errors: result.array()});
        } catch(err) {
            console.log(err)
            res.status(400).send({errors: [err]});
        } 
    }
)

router.get('/v1/patrimonies/:id', 
    query('period').isString(),
    query('periodTime').isNumeric(),
    async (req:Request, res: Response) => {
        try {
            const data: RequestGetPatrimony = matchedData(req);
            data.patrimonyId = req.params.id
            const patrimony = await container.patrimonyUseCase?.getPatrimony.execute(data)

            res.status(200).json(patrimony)
        } catch(err) {
            res.status(400).send({errors: [err]});
        }
    }
)

router.delete('/v1/patrimonies/:id', 
    async (req, res) => {
        try {
            await container.patrimonyUseCase?.deletePatrimony.execute(req.params.id)
            res.sendStatus(200)
        } catch(err) {
            console.log(err)
            res.status(400).send({errors: [err]});
        }
    }
)

router.get('/v1/patrimonies', 
    query('period').isString(),
    query('periodTime').isNumeric(),
    query('limit').isNumeric().toInt(),
    query('offset').isNumeric().toInt(),
    query('queryAll').optional().isBoolean().toBoolean(),
    async (req, res) => {
        try {
            const data: RequestGetAllPatrimony = matchedData(req);
            const patrimonies = await container.patrimonyUseCase?.getAllPatrimony.execute(data)

            res.status(200).json(patrimonies)
        } catch(err) {
            res.status(400).send({errors: [err]});
        }
    }
)

router.post('/v1/patrimonies/:id/add-snapshot', 
    body('balance').isNumeric(),
    body('date').isISO8601().toDate(),
    body('status').isString(),
    async (req: Request, res: Response) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestAddSnapshotPatrimony = matchedData(req);
                data.patrimonyId = req.params.id
                var created = await container.patrimonyUseCase?.addSnapshotPatrimony.execute(data);

                res.status(200).json(created)
                return;
            }

            res.status(400).send({errors: result.array()});
        } catch(err) {
            console.log(err)
            res.status(400).send({errors: [err]});
        }
    }
)

router.get('/v1/patrimonies/:id/snapshots', 
    query('period').isString(),
    query('periodTime').isNumeric(),
    query('limit').isNumeric().toInt(),
    query('offset').isNumeric().toInt(),
    query('queryAll').optional().isBoolean().toBoolean(),
    async (req: Request, res: Response) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestAllSnapshotPatrimony = matchedData(req);
                data.patrimonyId = req.params.id
                var snapshots = await container.patrimonyUseCase?.getSnapshotPatrimony.execute(data);

                res.status(200).json(snapshots)
                return;
            }

            console.log(result.array())

            res.status(400).send({errors: result.array()});
        } catch(err) {
            console.log(err)
            res.status(400).send({errors: [err]});
        }
    }
)

router.delete('/v1/patrimony-remove-snapshot/:id', 
    async (req, res) => {
        try {
            await container.patrimonyUseCase?.removeSnapshotPatrimony.execute(req.params.id);
            res.sendStatus(200)
        } catch(err) {
            res.status(400).send({errors: [err]});
        }
    }
)

router.put('/v1/patrimonies/:id/update-snapshot/:id_snapshot', 
    body('balance').optional().isNumeric(),
    body('date').optional().isISO8601().toDate(),
    body('status').optional().isString(),
    async (req: Request, res: Response) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestUpdateSnapshotPatrimony = matchedData(req);
                data.patrimonyId = req.params.id
                data.snapshotId = req.params.id_snapshot
                await container.patrimonyUseCase?.updateSnapshotPatrimony.execute(data);

                res.sendStatus(200)
                return;
            }

            res.status(400).send({errors: result.array()});
        } catch(err) {
            console.log(err)
            res.status(400).send({errors: [err]});
        }
    }
)

export default router