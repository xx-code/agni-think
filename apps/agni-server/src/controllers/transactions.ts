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

export default class TransactionController implements ApiController {
    private route = Router();

    private createTransaction: IUsecase<RequestAddTransactionUseCase, CreatedDto>;
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
        this.getBalanceBy = getBalanceBy;
        this.getTransaction = getTransaction;
        this.getTransactionByPagination = getTransactionByPagination;
        this.transfertTransaction = transfertTransaction;
        this.freezeTransaction = freezeTransaction;
        this.autoDeleteFreezeTransaction = autoDeleteFreezeTransaction;
    }

    setupRoutes(){
        this.route.post("/v1/transactions", this.handleCreateTransaction);
        this.route.put("/v1/transactions", this.handleUpdateTransaction);
        this.route.delete("/v1/transactions", this.handleDeleteTransaction);
        this.route.get("/v1/transactions-balance", this.handleGetBalanceBy);
        this.route.get("/v1/transactions/:id", this.handleGetTransaction); this.route.get("/v1/transactions", this.handleGetAllTransaction);
        this.route.post("/v1/transfert-transaction", this.handleTransfertTransaction);
        this.route.post("/v1/freeze-transaction", this.handleFreezeTransaction);
        this.route.post("/v1/freeze-transaction/auto-delete-verification", this.handleAutoFreezeTransaction);
    }

    getRoute(){
        return this.route;
    }

    async handleCreateTransaction(req: Request, res: Response) {
        const created = await this.createTransaction.execute({
            accountRef: req.body.accountRef,
            amount: req.body.amount,
            budgetRefs: req.body.budgetIds,
            categoryRef: req.body.categoryId,
            date: req.body.date,
            description: req.body.description,
            tagRefs: req.body.tagIds,
            type: req.body.type 
        });

        res.status(200).send(created);
    }

    async handleUpdateTransaction(req: Request, res: Response) {
        await this.updateTransaction.execute({
            id: req.params.id,
            accountRef: req.body.accountRef,
            amount: req.body.amount,
            budgetRefs: req.body.budgetIds,
            categoryRef: req.body.categoryId,
            date: req.body.date,
            description: req.body.description,
            tagRefs: req.body.tagIds,
            type: req.body.type
        });
        
        res.status(201);
    }

    async handleDeleteTransaction(req: Request, res: Response) {
        await this.deleteTransaction.execute(req.params.id);
    }

    async handleGetTransaction(req: Request, res: Response) {
        const transaction = await this.getTransaction.execute(req.params.id);

        res.status(200).send(transaction);
    }

    async handleGetAllTransaction(req: Request, res: Response) {
        const transactions = await this.getTransactionByPagination.execute({
            page: Number(req.query.page) || 0,
            limit: Number(req.query.limit) ||25,
            sortBy: req.query.sortBy?.toString(),
            sortSense: req.query.sortSense?.toString(),
            accountFilter: (Array.isArray(req.query.accountFilter) ? req.query.accountFilter : [req.query.accountFilter]).filter((v): v is string => typeof v === 'string'),
            categoryFilter: (Array.isArray(req.query.categoryFilter) ? req.query.categoryFilter : [req.query.categoryFilter]).filter((v): v is string => typeof v === 'string'),
            budgetFilter: (Array.isArray(req.query.budgetFilter) ? req.query.budgetFilter : [req.query.budgetFilter]).filter((v): v is string => typeof v === 'string'),
            tagFilter: (Array.isArray(req.query.tagFilter) ? req.query.tagFilter : [req.query.tagFilter]).filter((v): v is string => typeof v === 'string'),
            dateStart: req.query.dateStart?.toString(),
            dateEnd: req.query.dateEnd?.toString(),
            types: (Array.isArray(req.query.types) ? req.query.types : [req.query.types]).filter((v): v is string => typeof v === 'string'),
            minPrice: req.query.minPrice ? Number(req.query.minPrice) : undefined,
            maxPrice: req.query.maxPrice ? Number(req.query.maxPrice) : undefined
        }); 

        res.status(200).send(transactions);
    }

    async handleGetBalanceBy(req: Request, res: Response) {
        const balance = await this.getBalanceBy.execute({
            accountsIds: (Array.isArray(req.query.accountFilter) ? req.query.accountFilter : [req.query.accountFilter]).filter((v): v is string => typeof v === 'string'),
            categoriesIds: (Array.isArray(req.query.categoryFilter) ? req.query.categoryFilter : [req.query.categoryFilter]).filter((v): v is string => typeof v === 'string'),
            budgetIds: (Array.isArray(req.query.budgetFilter) ? req.query.budgetFilter : [req.query.budgetFilter]).filter((v): v is string => typeof v === 'string'),
            tagsIds: (Array.isArray(req.query.tagFilter) ? req.query.tagFilter : [req.query.tagFilter]).filter((v): v is string => typeof v === 'string'),
            types: (Array.isArray(req.query.types) ? req.query.types : [req.query.types]).filter((v): v is string => typeof v === 'string'),
            minPrice: req.query.minPrice ? Number(req.query.minPrice) : undefined,
            maxPrice: req.query.maxPrice ? Number(req.query.maxPrice) : undefined
        });

        res.status(200).send(balance);
    }

    async handleTransfertTransaction(req: Request, res: Response) {
        await this.transfertTransaction.execute({
            accountRefFrom: req.body.accountRefFrom,
            accountRefTo: req.body.accountRefTo,
            amount: req.body.amount,
            date: req.body.date
        });

        res.status(201);
    }

    async handleFreezeTransaction(req: Request, res: Response) {
        await this.freezeTransaction.execute({
            accountRef: req.body.accountId,
            amount: req.body.amount,
            endDate: req.body.endDate
        });

        res.status(201);
    }

    async handleAutoFreezeTransaction(req: Request, res: Response) {
        await this.autoDeleteFreezeTransaction.execute();

        res.status(201);
    }
}