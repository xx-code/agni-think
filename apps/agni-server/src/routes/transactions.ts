import { RequestNewFreezeBalance } from '@core/interactions/freezerBalance/addFreezeBalanceUseCase';
import { RequestAddTransactionUseCase } from '@core/interactions/transaction/addTransactionUseCase';
import { RequestGetPagination } from '@core/interactions/transaction/getPaginationTransactionUseCase';
import { RequestTransfertTransactionUseCase } from '@core/interactions/transaction/transfertTransactionUseCase';
import { RequestUpdateTransactionUseCase } from '@core/interactions/transaction/updateTransactionUseCase';
import { Router, Request, Response } from 'express';
import { body, matchedData, query, validationResult } from 'express-validator';
import container from 'src/di_contenair';

const router = Router();

router.post("/v1/transactions", 
    body('accountId').notEmpty().isString(),
    body('status'),
    body('type').notEmpty().isString(),
    body('currencyId').optional(),
    body('date').notEmpty().isISO8601().toDate(),
    body('records').exists().isArray(),
    body('deductions').isArray(),
    body('records.*.amount').exists().isNumeric(),
    body('records.*.categoryId').exists().isString(),
    body('records.*.description').optional().isString(),
    body('records.*.tagIds').optional().isArray(),
    body('records.*.budgetIds').optional().isArray(),
    body('deductions').optional({ checkFalsy: true }).isArray(),
    body('deductions.*.deductionId').notEmpty().isString(),
    body('deductions.*.amount').notEmpty().isNumeric(),
    async (req, res) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestAddTransactionUseCase = matchedData(req);
                const created = await container.transactionUseCase?.createTransaction.execute(data);

                res.status(200).send(created);
                return;
            } 
            
            console.log(result.array())
            res.status(400).send({ errors: result.array() });
        } catch(err) {
            console.log(err)
            res.status(400).send({ errors: [err] });
        }
    });

router.put("/v1/transactions/:id", 
    body('accountId').optional().isString(),
    body('type').optional().isString(),
    body('currencyId').optional(),
    body('date').optional().isISO8601().toDate(),
    body('removeRecordIds').optional().isArray(),
    body('addRecords').optional().isArray(),
    body('deductions').optional().isArray(),

    body('addRecords.*.amount').exists().isNumeric(),
    body('addRecords.*.categoryId').exists().isString(),
    body('addRecords.*.description').optional().isString(),
    body('addRecords.*.tagIds').optional().isArray(),
    body('addRecords.*.budgetIds').optional().isArray(),

    body('deductions').optional({ checkFalsy: true }).isArray(),
    body('deductions.*.deductionId').notEmpty().isString(),
    body('deductions.*.amount').notEmpty().isNumeric(),
    async (req: Request, res: Response) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestUpdateTransactionUseCase = matchedData(req);
                data.id = req.params.id as string;
                await container.transactionUseCase?.updateTransaction.execute(data);

                res.sendStatus(200);
                return;
            }
            console.log(result.array())
            res.status(400).send({ errors: result.array() });
        } catch(err) {
            console.log(err)
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
    query('status').optional().isString(),
    query('accountFilterIds').optional().toArray(),
    query('categoryFilterIds').optional().toArray(),
    query('budgetFilterIds').optional().toArray(),
    query('tagFilterIds').optional().toArray(),
    query('dateStart').optional().isISO8601().toDate(),
    query('dateEnd').optional().isISO8601().toDate(),
    query('types').optional().toArray(),
    query('minPrice').optional().toFloat(),
    query('maxPrice').optional().toFloat(),
    query('isFreeze').optional().isBoolean(),
    async (req, res) => {
        try {
            const result = validationResult(req); 
            if (result.isEmpty()) {
                const data: RequestGetPagination = matchedData(req);
                const balance = await container.transactionUseCase?.getBalanceBy.execute(data);

                res.status(200).json(balance);
                return;
            }

            
            res.status(400).send({ errors: result.array() });
        } catch(err) {
            console.log(err)
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
    query('offset').isNumeric().toInt().default(0),
    query('limit').isNumeric().toInt().default(25),
    query('sortBy').optional(),
    query('sortSense').optional(),
    query('status').optional().isString(),
    query('accountFilterIds').optional().toArray(),
    query('categoryFilterIds').optional().toArray(),
    query('budgetFilterIds').optional().toArray(),
    query('tagFilterIds').optional().toArray(),
    query('dateStart').optional().isISO8601().toDate(),
    query('dateEnd').optional().isISO8601().toDate(),
    query('types').optional().toArray(),
    query('minPrice').optional().toFloat(),
    query('maxPrice').optional().toFloat(),
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

            res.status(400).send({ errors: result.array() });
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
                data.date = new Date()
                await container.transactionUseCase?.transfertTransaction.execute(data);

                res.sendStatus(200);
                return;
            }
            
            res.status(400).send({ errors: result.array() });
        } catch(err) {
            res.status(400).send({ errors: [err] });
        }
    });

router.post("/v1/freeze-transaction", 
    body('accountId').notEmpty(),
    body('title').notEmpty().isString(),
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
            
            res.status(400).send({ errors: result.array() });
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