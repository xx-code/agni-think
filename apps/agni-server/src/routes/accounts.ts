import { Router, Request, Response } from 'express';
import container from '../di_contenair'
import { body, matchedData, query, validationResult } from 'express-validator';
import { RequestCreationAccountUseCase } from '@core/interactions/account/creationAccountUseCase';
import { RequestUpdateAccountUseCase } from '@core/interactions/account/updateAccountUseCase';
import { RequestGetAllAccountPastBalanceUseCase } from '@core/interactions/account/getAllAccountWithPatBalanceUseCase';

const router = Router();

router.post('/v1/accounts', 
    body('title').isString().notEmpty(), 
    body('type').isString().notEmpty(),
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
            res.status(400).send({ errors: [err] });
        }
    });

router.put('/v1/accounts/:id', 
    body('title').optional().isString(),
    body('type').optional().isString(),
    async (req: Request, res: Response) => {
        try {
            const result = validationResult(req);
            
            if (!result.isEmpty()) {
                res.send({ errors: result.array() });
                return;
            }

            const data: RequestUpdateAccountUseCase = matchedData(req);
            data.id = req.params.id;
            await container.accountUseCase?.updateAccount.execute(data);

            res.sendStatus(200);
        } catch(err) {
            res.status(400).send({ errors: [err] });
        }
        
    });

router.get('/v1/accounts/:id', async (req, res) => {
    try {
        var ucRes = await container.accountUseCase?.getAccount.execute(req.params.id)
        res.status(200).json(ucRes)
    } catch(err) {
        res.status(400).send({ errors: [err] });
    }
    
});

router.get('/v1/accounts', async (req, res) => {
    try {
        var ucRes = await container.accountUseCase?.getAllAccount.execute()
        res.status(200).json(ucRes)
    } catch(err) {
        res.status(400).send({ errors: [err] });
    }
});

router.get('/v1/accounts-with-past-balance',
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