import { RequestCreateHoldingDto } from "@core/interactions/holding/createHoldingUseCase";
import { Router, Request, Response } from "express";
import { body, matchedData, query, validationResult } from "express-validator";
import container from '../di_contenair';
import { RequestUpdateHoldingDto } from "@core/interactions/holding/updateHoldingUseCase";
import { HoldingTransactionQueryFilter } from "@core/interactions/holdingTransactions/getAllHoldingTransaction";
import { AddHoldingTransactionUseCase, RequestAddHoldingTransactionDto } from "@core/interactions/holdingTransactions/addHoldingTransaction";
import { HoldingQueryFilter } from "@core/interactions/holding/getAllHoldingUseCase";
import { QueryFilter } from "@core/dto/base";
import { RequestUpdateHoldingTransactionDto } from "@core/interactions/holdingTransactions/updateHoldingTransaction";

const router = Router();


router.post(
    '/v1/holdings',
    body("accountId").isString(),
    body("title").isString(),
    body("code").isString(),
    body("type").isString(),
    body("bookCost").isNumeric(),
    body("quantity").isNumeric(),
    body("currentPrice").isNumeric(),
    async (req, res) => {
    try {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestCreateHoldingDto = matchedData(req);
            var created = await container.holdingUseCase?.createHolding.execute(data);

            res.status(200).json(created)
            return;
        }

        res.status(400).send({errors: result.array()});
    } catch(err) {
        console.log(err)
        res.status(400).send({errors: [err]});
    }
});


router.put(
    '/v1/holdings/:id', 
    body('accountId').optional().isString(),
    body('title').optional().isString(),
    body('code').optional().isString(),
    body('type').optional().isString(),
    body('currentPrice').optional().isNumeric(),
    async (req: Request, res: Response) => {
    try {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestUpdateHoldingDto = matchedData(req);
            data.id = req.params.id
            var created = await container.holdingUseCase?.updateHolding.execute(data);

            res.status(200).json(created)
            return;
        }

        res.status(400).send({errors: result.array()});
    } catch(err) {
        res.status(400).send({errors: [err]});
    }
});

router.get(
    '/v1/holdings', 
    query('limit').isNumeric().toInt(),
    query('offset').isNumeric().toInt(),
    query('accountId').optional().isString(),
    query('queryAll').optional().isBoolean().toBoolean(),
    async (req, res) => {
    try {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const query = matchedData(req)
            const data: HoldingQueryFilter = {
                accountId: query.accountId,
                query: query as QueryFilter,
            }
            var results = await container.holdingUseCase?.getAllHolding.execute(data);

            res.status(200).json(results)
            return;
        }

        console.log(result.array())

        res.status(400).send({errors: result.array()});
    } catch(err) {
        console.log(err)
        res.status(400).send({errors: [err]});
    }
});

router.post(
    '/v1/holding-transactions', 
    body('holdingId').isString(),
    body('fees').isNumeric(),
    body('date').isISO8601().toDate(),
    body('quantity').isNumeric(),
    body('cost').isNumeric(),
    body('type').isString(),
    async (req, res) => {
    try {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestAddHoldingTransactionDto = matchedData(req);
            var results = await container.holdingUseCase?.addHoldingTransaction.execute(data);

            res.status(200).json(results)
            return;
        }

        res.status(400).send({errors: result.array()});
    } catch(err) {
        res.status(400).send({errors: [err]});
    }
});

router.put(
    '/v1/holding-transactions/:id', 
    body('type').optional().isString(),
    body('cost').optional().isNumeric(),
    body('quantity').optional().isNumeric(),
    body('fees').optional().isNumeric(),
    body('date').optional().isISO8601().toDate(),
    async (req: Request, res: Response) => {
    try {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestUpdateHoldingTransactionDto = matchedData(req);
            data.id = req.params.id

            var results = await container.holdingUseCase?.updateHoldingTransaction.execute(data);

            res.status(200).json(results)
            return;
        }

        res.status(400).send({errors: result.array()});
    } catch(err) {
        res.status(400).send({errors: [err]});
    }
});


router.get(
    '/v1/holding-transactions/:id', 
    async (req: Request, res: Response) => {
    try {
        const result = await container.holdingUseCase?.getHoldingTransaction.execute(req.params.id)
        
        res.status(200).json(result)
    } catch(err) {
        res.status(400).send({errors: [err]});
    }
});

router.get(
    '/v1/holding-transactions', 
    query('holdingId').isString(),
    query('limit').isNumeric().toInt(),
    query('offset').isNumeric().toInt(),
    query('queryAll').optional().isBoolean().toBoolean(),
    async (req, res) => {
    try {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const query = matchedData(req)
            const data: HoldingTransactionQueryFilter = {
                holdingId: query.holdingId,
                query: query as QueryFilter,
            }

            var results = await container.holdingUseCase?.getAllHoldingTransaction.execute(data);

            res.status(200).json(results)
            return;
        }

        res.status(400).send({errors: result.array()});
    } catch(err) {
        res.status(400).send({errors: [err]});
    }
});

router.delete(
    '/v1/holdings/:id', 
    async (req: Request, res: Response) => {
    try {
        const result = await container.holdingUseCase?.deleteHolding.execute(req.params.id)
        
        res.status(200).json(result)
    } catch(err) {
        res.status(400).send({errors: [err]});
    }
});

router.delete(
    '/v1/holding-transactions/:id', 
    async (req: Request, res: Response) => {
    try {
        const result = await container.holdingUseCase?.deleteHoldingTransaction.execute(req.params.id)
        
        res.status(200).json(result)
    } catch(err) {
        res.status(400).send({errors: [err]});
    }
});

export default router;