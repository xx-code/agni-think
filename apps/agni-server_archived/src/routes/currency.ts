import { Router, Response, Request } from 'express';
import { body, matchedData, query, validationResult } from "express-validator";
import container from '../di_contenair';
import { RequestCreateCurrency } from "@core/interactions/currency/createCurrencyUseCase";
import { RequestUpdateCurrency } from "@core/interactions/currency/updateCurrencyUseCase";
import { QueryFilter } from "@core/dto/base";

const router = Router();


router.post(
    '/v1/currencies',
    body('name'),
    body('symbol'),
    body('locale').optional().isString(),
    body('rateToBase').optional().isBoolean(),
    body('isBase').optional().isBoolean(),
    async (req, res) => {
    try {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestCreateCurrency = matchedData(req);
            var created = await container.currencyUseCase?.createCurrency.execute(data);

            res.status(200).json(created)
            return;
        }

        res.status(400).send({errors: result.array()});
    } catch(err) {
        res.status(400).send({errors: [err]});
    }
});

router.put(
    '/v1/currencies/:id', 
    body('name').optional().isString(),
    body('symbol').optional().isString(),
    body('locale').optional().isString(),
    body('rateToBase').optional().isBoolean(),
    body('isBase').optional().isBoolean(),
    async (req: Request, res) => {
    try {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestUpdateCurrency = matchedData(req);
            data.id = req.params.id as string
            var created = await container.currencyUseCase?.updateCurrency.execute(data);

            res.status(200).json(created)
            return;
        }

        res.status(400).send({errors: result.array()});
    } catch(err) {
        res.status(400).send({errors: [err]});
    }
});

router.get('/v1/currencies/:id', async (req: Request, res: Response) => {
    try {
        const id = req.params.id as string
        var result = await container.currencyUseCase?.getCurrency.execute(id);

        res.status(200).json(result)
    } catch(err) {
        res.status(400).send({errors: [err]});
    }
});

router.get(
    '/v1/currencies', 
    query('limit').isNumeric().toInt(),
    query('offset').isNumeric().toInt(),
    query('queryAll').optional().isBoolean().toBoolean(),
    async (req, res) => {
    try {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: QueryFilter = matchedData(req);
            var results = await container.currencyUseCase?.getAllCurrency.execute(data);

            res.status(200).json(results)
            return;
        }

        res.status(400).send({errors: result.array()});
    } catch(err) {
        res.status(400).send({errors: [err]});
    }
});

export default router;