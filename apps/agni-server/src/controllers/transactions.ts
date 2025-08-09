import { Request, Response, Router } from "express";
import { RequestNewFreezeBalance } from "@core/interactions/freezerBalance/addFreezeBalanceUseCase";
import { RequestAddTransactionUseCase } from "@core/interactions/transaction/addTransactionUseCase";
import { RequestGetBalanceBy } from "@core/interactions/transaction/getBalanceByUseCase";
import { GetAllTransactionDto, RequestGetPagination } from "@core/interactions/transaction/getPaginationTransactionUseCase";
import { GetTransactionDto } from "@core/interactions/transaction/getTransactionUseCase";
import { RequestTransfertTransactionUseCase } from "@core/interactions/transaction/transfertTransactionUseCase";
import { RequestUpdateTransactionUseCase } from "@core/interactions/transaction/updateTransactionUseCase";
import { ApiController } from "./base";
import { IUsecase } from "@core/interactions/interfaces";
import { CreatedDto, ListDto } from "@core/dto/base";
import { body, matchedData, query, validationResult } from "express-validator";
import { RequestCompleteTransactionUsecase } from "@core/interactions/transaction/CompleteTransactionUseCase";

export default class TransactionController implements ApiController {
    private route = Router();

    private createTransaction: IUsecase<RequestAddTransactionUseCase, CreatedDto>;
    private completeTransaction: IUsecase<RequestCompleteTransactionUsecase, void>;
    private updateTransaction: IUsecase<RequestUpdateTransactionUseCase, void>;
    private deleteTransaction: IUsecase<string, void>;
    private getBalanceBy: IUsecase<RequestGetBalanceBy, number>;
    private getTransaction: IUsecase<string, GetTransactionDto>;
    private getTransactionByPagination: IUsecase<RequestGetPagination, ListDto<GetAllTransactionDto>>;
    private transfertTransaction: IUsecase<RequestTransfertTransactionUseCase, void>;
    private freezeTransaction: IUsecase<RequestNewFreezeBalance, CreatedDto>;
    private autoDeleteFreezeTransaction: IUsecase<void, void>;

    constructor(
        createTransaction: IUsecase<RequestAddTransactionUseCase, CreatedDto>,
        updateTransaction: IUsecase<RequestUpdateTransactionUseCase, void>,
        deleteTransaction: IUsecase<string, void>,
        completeTransaction: IUsecase<RequestCompleteTransactionUsecase, void>,
        getBalanceBy: IUsecase<RequestGetBalanceBy, number>,
        getTransaction: IUsecase<string, GetTransactionDto>,
        getTransactionByPagination: IUsecase<RequestGetPagination, ListDto<GetAllTransactionDto>>,
        transfertTransaction: IUsecase<RequestTransfertTransactionUseCase, void>,
        freezeTransaction: IUsecase<RequestNewFreezeBalance, CreatedDto>,
        autoDeleteFreezeTransaction: IUsecase<void, void>
    ) {
        this.createTransaction = createTransaction;
        this.updateTransaction = updateTransaction;
        this.deleteTransaction = deleteTransaction;
        this.completeTransaction = completeTransaction;
        this.getBalanceBy = getBalanceBy;
        this.getTransaction = getTransaction;
        this.getTransactionByPagination = getTransactionByPagination;
        this.transfertTransaction = transfertTransaction;
        this.freezeTransaction = freezeTransaction;
        this.autoDeleteFreezeTransaction = autoDeleteFreezeTransaction;
    }

    setupRoutes(){
        this.route.post("/v1/transactions", 
            body('accountId').notEmpty(),
            body('amount').notEmpty().isNumeric(),
            body('budgetIds').isArray(),
            body('categoryIds').isArray(),
            body('date').notEmpty().isDate(),
            body('description').notEmpty(),
            body('tagIds').isArray(),
            body('type').notEmpty(),
            this.handleCreateTransaction);

        this.route.put("/v1/transactions/:id", 
            body('accountId').isEmpty(),
            body('amount').isEmpty().isNumeric(),
            body('budgetIds').isArray(),
            body('categoryIds').isArray(),
            body('date').isEmpty().isDate(),
            body('description').isEmpty(),
            body('tagIds').isArray(),
            body('type').isEmpty(),
            this.handleUpdateTransaction);
        
        this.route.put("/v1/transactions/:id/complete", this.handleCompleteTransaction);

        this.route.delete("/v1/transactions", 
            this.handleDeleteTransaction);

        this.route.get("/v1/transactions-balance",
            query('accountFilterIds').isArray(),
            query('categoryFilterIds').isArray(),
            query('budgetFilterIds').isArray(),
            query('tagFilterIds').isArray(),
            query('dateStart').isEmpty().isDate(),
            query('dateEnd').isEmpty().isDate(),
            query('types').isArray(),
            query('minPrice').isEmpty().isNumeric(),
            query('maxPrice').isEmpty().isNumeric(),
            this.handleGetBalanceBy);

        this.route.get("/v1/transactions/:id", 
            this.handleGetTransaction); 

        this.route.get("/v1/transactions", 
            query('offset').isNumeric().default(0),
            query('limit').isNumeric().default(25),
            query('sortBy').isEmpty(),
            query('sortSense').isEmpty(),
            query('accountFilterIds').isArray(),
            query('categoryFilterIds').isArray(),
            query('budgetFilterIds').isArray(),
            query('tagFilterIds').isArray(),
            query('dateStart').isEmpty().isDate(),
            query('dateEnd').isEmpty().isDate(),
            query('types').isArray(),
            query('minPrice').isEmpty().isNumeric(),
            query('maxPrice').isEmpty().isNumeric(),
            this.handleGetAllTransaction);

        this.route.post("/v1/transfert-transaction", 
            body('accountIdFrom').notEmpty(),
            body('accountIdTo').notEmpty(),
            body('amount').notEmpty().isNumeric(),
            this.handleTransfertTransaction);

        this.route.post("/v1/freeze-transaction", 
            body('accountId').notEmpty(),
            body('amount').notEmpty().isNumeric(),
            body('endDate').notEmpty().isDate(),
            this.handleFreezeTransaction);

        this.route.post("/v1/freeze-transaction/auto-delete-verification", 
            this.handleAutoFreezeTransaction);
    }

    getRoute(){
        return this.route;
    }

    async handleCreateTransaction(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestAddTransactionUseCase = matchedData(req);
            const created = await this.createTransaction.execute(data);

            res.status(200).send(created);
        } 
        
        res.send({ errors: result.array() });
    }

    async handleUpdateTransaction(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestUpdateTransactionUseCase = matchedData(req);
            data.id = req.params.id;
            await this.updateTransaction.execute(data);

            res.status(201);
        }
        
        res.send({ errors: result.array() });
    }

    async handleDeleteTransaction(req: Request, res: Response) {
        await this.deleteTransaction.execute(req.params.id);
    }

    async handleGetTransaction(req: Request, res: Response) {
        const transaction = await this.getTransaction.execute(req.params.id);

        res.status(200).send(transaction);
    }

    async handleGetAllTransaction(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestGetPagination = matchedData(req);
            const transactions = await this.getTransactionByPagination.execute(data); 

            res.status(200).send(transactions);
        }

        res.send({ errors: result.array() });
    }

    async handleGetBalanceBy(req: Request, res: Response) {
        const result = validationResult(req); 
        if (result.isEmpty()) {
            const data: RequestGetBalanceBy = matchedData(req);
            const balance = await this.getBalanceBy.execute(data);

            res.status(200).send(balance);
        }
        
        res.send({ errors: result.array() });
    }

    async handleTransfertTransaction(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestTransfertTransactionUseCase = matchedData(req);
            await this.transfertTransaction.execute(data);

            res.status(201);
        }
        
        res.send({ errors: result.array() });
    }

    async handleFreezeTransaction(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestNewFreezeBalance = matchedData(req);
            await this.freezeTransaction.execute(data);

            res.status(201);
        }
        
        res.send({ errors: result.array() });
    }

    async handleCompleteTransaction(req: Request, res: Response) {
        await this.completeTransaction.execute({transactionId: req.params.id});
        res.status(201);
    } 

    async handleAutoFreezeTransaction(req: Request, res: Response) {
        await this.autoDeleteFreezeTransaction.execute();

        res.status(201);
    }
}