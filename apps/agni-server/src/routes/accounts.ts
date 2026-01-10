import { Router, Request, Response } from 'express';
import container from '../di_contenair'
import { body, matchedData, query, validationResult } from 'express-validator';
import { RequestCreationAccountUseCase } from '@core/interactions/account/creationAccountUseCase';
import { RequestUpdateAccountUseCase } from '@core/interactions/account/updateAccountUseCase';
import { RequestGetAllAccountPastBalanceUseCase } from '@core/interactions/account/getAllAccountWithPatBalanceUseCase';
import { QueryFilter } from '@core/dto/base';

const router = Router();


router.post('/v1/accounts', 
    body('title').isString().notEmpty(), 
    body('type').isString().notEmpty(),
    body('currencyId').isString().notEmpty(),
    body('creditLimit').optional().isNumeric(),
    body('contributionType').optional().isString(),
    body('managementType').optional().isString(),
    async (req, res) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestCreationAccountUseCase = matchedData(req);
                var ucRes = await container.accountUseCase?.createAccount.execute(data);

                res.status(200).json(ucRes);

                return;
            }

            console.log(result.array())

            res.status(400).send({ errors: result.array() });
        } catch(err) {
            console.log(err)
            res.status(400).send({ errors: [err] });
        }
    });

router.put('/v1/accounts/:id', 
    body('title').optional().isString(),
    body('currencyId').optional().isString(),
    body('creditLimit').optional().isNumeric(),
    body('contributionType').optional().isString(),
    body('managementType').optional().isString(),
    async (req: Request, res: Response) => {
        try {
            const result = validationResult(req);
            
            if (!result.isEmpty()) {
                res.send({ errors: result.array() });
                return;
            }

            const data: RequestUpdateAccountUseCase = matchedData(req);
            data.id = req.params.id as string;
            await container.accountUseCase?.updateAccount.execute(data);

            res.sendStatus(200);
        } catch(err) {
            res.status(400).send({ errors: [err] });
        }
        
    });

router.get('/v1/accounts/:id', 
    async (req: Request, res: Response) => {
    try {
        const id = req.params.id as string
        const account = await container.accountUseCase?.getAccount.execute(id)
        res.status(200).json(account)
    } catch(err) {
        res.status(400).send({ errors: [err] });
    }
    
});

router.get('/v1/accounts-with-detail/:id', 
    async (req: Request, res: Response) => {
    try {
        const id = req.params.id as string
        const account = await container.accountUseCase?.getAccountWithDetail.execute(id)
        res.status(200).json(account)
        return 
    } catch(err) {
        res.status(400).send({ errors: [err] });
    }
    
});

router.get(
    '/v1/accounts',
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
        var ucRes = await container.accountUseCase?.getAllAccount.execute(request)
        res.status(200).json(ucRes)
    } catch(err) {
        res.status(400).send({ errors: [err] });
    }
});

router.get(
    '/v1/accounts-with-detail',
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
        const account = await container.accountUseCase?.getAllAccountWithDetail.execute(request)
        res.status(200).json(account)
    } catch(err) {
        res.status(400).send({ errors: [err] });
    }
});

router.get('/v1/accounts-with-past-balance',
    query('limit').isNumeric().toInt(),
    query('offset').isNumeric().toInt(),
    query('queryAll').optional().isBoolean().toBoolean(),
    query('period').isString().notEmpty(),
    query('periodTime').isNumeric().notEmpty(), async (req, res) => {
    try {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestGetAllAccountPastBalanceUseCase = matchedData(req);
            var ucRes = await container.accountUseCase?.getAllAccountWithBalance.execute(data);

            res.status(200).json(ucRes);
            return;
        }
        
        res.status(400).send({ errors: result.array() });
    } catch(err) {
        res.status(400).send({ errors: [err] });
    }
});

router.delete('/v1/accounts/:id', async (req, res) => {
    try {
        await container.accountUseCase?.deleteAccount.execute(req.params.id)
        res.sendStatus(200);
    } catch(err) {
        res.status(400).send({ errors: [err] });
    }
});


export default router;