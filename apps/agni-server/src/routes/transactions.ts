import { RequestNewFreezeBalance } from '@core/interactions/freezerBalance/addFreezeBalanceUseCase';
import { RequestAddTransactionUseCase } from '@core/interactions/transaction/addTransactionUseCase';
import { RequestGetBalanceBy } from '@core/interactions/transaction/getBalanceByUseCase';
import { RequestGetPagination } from '@core/interactions/transaction/getPaginationTransactionUseCase';
import { RequestTransfertTransactionUseCase } from '@core/interactions/transaction/transfertTransactionUseCase';
import { RequestUpdateTransactionUseCase } from '@core/interactions/transaction/updateTransactionUseCase';
import { Router, Request, Response } from 'express';
import { body, matchedData, query, validationResult } from 'express-validator';
import container from 'src/di_contenair';

const router = Router();

router.post("/v1/transactions", 
    body('accountId').notEmpty().isString(),
    body('amount').notEmpty().isNumeric(),
    body('budgetIds').isArray(),
    body('categoryId').notEmpty().isString(),
    body('date').notEmpty().isDate(),
    body('description').notEmpty().isString(),
    body('tagIds').isArray(),
    body('type').notEmpty().isString(),
    async (req, res) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestAddTransactionUseCase = matchedData(req);
                const created = await container.transactionUseCase?.createTransaction.execute(data);

                res.status(200).send(created);
                return;
            } 
            
            res.send({ errors: result.array() });
        } catch(err) {
            res.status(400).send({ errors: [err] });
        }
    });

router.put("/v1/transactions/:id", 
    body('accountId').optional().isString(),
    body('amount').optional().isNumeric(),
    body('budgetIds').optional().isArray(),
    body('categoryId').optional().isString(),
    body('date').optional().isDate(),
    body('description').optional(),
    body('tagIds').optional().isArray(),
    body('type').optional().isString(),
    async (req: Request, res: Response) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestUpdateTransactionUseCase = matchedData(req);
                data.id = req.params.id;
                await container.transactionUseCase?.updateTransaction.execute(data);

                res.sendStatus(200);
                return;
            }
            
            res.send({ errors: result.array() });
        } catch(err) {
            res.status(400).send({ errors: [err] });
        }
    });

router.put("/v1/transactions/:id/complete", async (req, res) => {
    try {
        await container.transactionUseCase?.completeTransaction.execute({transactionId: req.params.id});
        res.sendStatus(200);
    } catch(err) {
        console.log(err)
        res.status(400).send({ errors: [err] });
    }
});

router.delete("/v1/transactions/:id", async (req, res) => {
    try {
        await container.transactionUseCase?.deleteTransaction.execute(req.params.id);
        res.sendStatus(200);
    } catch(err) {
        res.status(400).send({ errors: [err] });
    }
});

router.get("/v1/transactions-balance",
    query('accountFilterIds').optional().isArray(),
    query('categoryFilterIds').optional().isArray(),
    query('budgetFilterIds').optional().isArray(),
    query('tagFilterIds').optional().isArray(),
    query('dateStart').optional().isDate(),
    query('dateEnd').optional().isDate(),
    query('types').optional().isArray(),
    query('minPrice').optional().isNumeric(),
    query('maxPrice').optional().isNumeric(),
    async (req, res) => {
        try {
            const result = validationResult(req); 
            if (result.isEmpty()) {
                const data: RequestGetBalanceBy = matchedData(req);
                const balance = await container.transactionUseCase?.getBalanceBy.execute(data);

                res.status(200).json({balance: balance});
                return;
            }
            
            res.send({ errors: result.array() });
        } catch(err) {
            res.status(400).send({ errors: [err] });
        }
    });

router.get("/v1/transactions/:id", async (req, res) => {
    try {
        const transaction = await container.transactionUseCase?.getTransaction.execute(req.params.id);

        res.status(200).send(transaction);
    } catch(err) {
        res.status(400).send({ errors: [err] });
    }
}); 

router.get("/v1/transactions", 
    query('offset').isNumeric().default(0),
    query('limit').isNumeric().default(25),
    query('sortBy').optional(),
    query('sortSense').optional(),
    query('accountFilterIds').optional().isArray(),
    query('categoryFilterIds').optional().isArray(),
    query('budgetFilterIds').optional().isArray(),
    query('tagFilterIds').optional().isArray(),
    query('dateStart').optional().isDate(),
    query('dateEnd').optional().isDate(),
    query('types').optional().isArray(),
    query('minPrice').optional().isNumeric(),
    query('maxPrice').optional().isNumeric(),
    query('isFreeze').optional().isBoolean(),
    async (req, res) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestGetPagination = matchedData(req);
                const transactions = await container.transactionUseCase?.getPaginition.execute(data); 

                res.status(200).send(transactions);
                return;
            }

            res.send({ errors: result.array() });
        } catch(err) {
            console.log(err)
            res.status(400).send({ errors: [err] });
        }
    });

router.post("/v1/transfert-transaction", 
    body('accountIdFrom').notEmpty(),
    body('accountIdTo').notEmpty(),
    body('amount').notEmpty().isNumeric(),
    async (req, res) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestTransfertTransactionUseCase = matchedData(req);
                await container.transactionUseCase?.transfertTransaction.execute(data);

                res.sendStatus(200);
                return;
            }
            
            res.send({ errors: result.array() });
        } catch(err) {
            res.status(400).send({ errors: [err] });
        }
    });

router.post("/v1/freeze-transaction", 
    body('accountId').notEmpty(),
    body('amount').notEmpty().isNumeric(),
    body('endDate').notEmpty().isDate(),
    async (req, res) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestNewFreezeBalance = matchedData(req);
                await container.transactionUseCase?.freezeTransaction.execute(data);

                res.sendStatus(200);
                return;
            }
            
            res.send({ errors: result.array() });
        } catch(err) {
            res.status(400).send({ errors: [err] });
        }
    });

router.post("/v1/freeze-transaction/auto-delete-verification", async (req, res) => {
    try {
        await container.transactionUseCase?.autoFreezeTransaction.execute();
        res.sendStatus(200);
    } catch(err) {
        res.status(400).send({ errors: [err] });
    }
});

export default router;