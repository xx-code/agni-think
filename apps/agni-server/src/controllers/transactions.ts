import { Request, Response } from "express";
import { AddFreezeBalanceUseCase, IAddFreezeBalancePresenter, IAddFreezeBalanceUseCase, RequestNewFreezeBalance } from "@core/interactions/freezerBalance/addFreezeBalanceUseCase";
import { AutoDeleteFreezeBalanceUseCase, IAutoDeleteFreezeBalancePresenter, IAutoDeleteFreezeBalanceUseCase } from "@core/interactions/freezerBalance/autoDeleteFreezeBalanceUseCase";
import { AddTransactionUseCase, IAddTransactionAdapter, IAddTransactionUseCase, IAddTransactionUseCaseResponse, RequestAddTransactionUseCase } from "@core/interactions/transaction/addTransactionUseCase";
import { DeleteTransactionUseCase, IDeleteTransactionUseCase, IDeleteTransactoinUseCaseResponse } from "@core/interactions/transaction/deleteTransactionUseCase";
import { GetBalanceByUseCase, IGetBalanceByUseCase, IGetBalanceByUseCaseResponse, RequestGetBalanceBy } from "@core/interactions/transaction/getBalanceByUseCase";
import { GetPaginationTransaction, IGetPaginationTransaction, IGetPaginationTransactionAdapter, IGetPaginationTransactionResponse, RequestGetPagination, TransactionPaginationResponse } from "@core/interactions/transaction/getPaginationTransactionUseCase";
import { GetTransactionUseCase, IGetTransactionAdapter, IGetTransactionUseCase, IGetTransactionUseCaseResponse, TransactionResponse } from "@core/interactions/transaction/getTransactionUseCase";
import { ITransfertTransactionAdapter, ITransfertTransactionUseCaseResponse, RequestTransfertTransactionUseCase, TransfertTransactionUseCase } from "@core/interactions/transaction/transfertTransactionUseCase";
import { IUpdateTransactionAdapter, IUpdateTransactionUseCase, IUpdateTransactionUseCaseResponse, RequestUpdateTransactionUseCase, UpdateTransactionUseCase } from "@core/interactions/transaction/updateTransactionUseCase";
import { ApiError, ApiResponse, initApiResponse } from "./type";
import { isEmpty } from "@core/domains/helpers";
import { TransactionRepository } from "@core/repositories/transactionRepository";
import { RecordRepository } from "@core/repositories/recordRepository";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { CategoryRepository } from "@core/repositories/categoryRepository";
import { AccountRepository } from "@core/repositories/accountRepository";
import { DateService } from "@core/adapters/libs";

class CreateTransactionModel { 
    private model: RequestAddTransactionUseCase

    constructor(reqBody: any) {
        this.model = {
            accountRef: reqBody.accountId,
            categoryRef: reqBody.categoryId,
            amount: reqBody.amount ? reqBody.amount : 0,
            description: reqBody.description,
            tagRefs: reqBody.tagIds,
            mainCategory: reqBody.mainCategory,
            type: reqBody.typeTransaction,
            date: reqBody.date
        }
    }

    validateInput(): ApiError[] {
        let errors: ApiError[] = []

        if (isEmpty(this.model.accountRef))
            errors.push({field: "accountId", message: "account field is empty"})

        if (isEmpty(this.model.categoryRef))
            errors.push({field: "categoryId", message: "category field is empty"})

        if (this.model.amount <= 0)
            errors.push({field: "amount", message: "amout must be greater than 0"})

        if (isEmpty(this.model.type)) 
            errors.push({field: "type", message: "you have to choose type of transaction 'credit' or 'debit' "})

        if (isEmpty(this.model.mainCategory)) 
            errors.push({field: "type", message: "you have to choose main category transactions"})

        if (isEmpty(this.model.date))
            errors.push({field: "date", message: "date field is"})

        return errors
    }

    getModelRequest(): RequestAddTransactionUseCase {
        return this.model
    }
}

export class ApiCreateTransactionController implements IAddTransactionUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IAddTransactionUseCase

    constructor(transactionAdapter: IAddTransactionAdapter) {
        this.usecase = new AddTransactionUseCase(transactionAdapter, this)
    }
    success(transactionId: string): void {
        this.modelView.success = true
        this.modelView.statusCode = 200
        this.modelView.data = {
            transactionId: transactionId
        }
    }
    fail(err: Error): void {
        this.modelView.success = false
        this.modelView.statusCode = 400 
        this.modelView.error = {
            field: "", message: err.message
        }
    }

    async execute(req: Request, res: Response): Promise<void> {
        this.modelView = initApiResponse()
        let model = new CreateTransactionModel(req.body)
        let errors = model.validateInput()

        if (errors.length > 0) {
            if (req.query.validate_all)
                this.modelView.errors = errors
            else 
                this.modelView.error = errors[0]

            this.modelView.statusCode = 400

            res.status(this.modelView.statusCode).send(this.modelView)
            return
        }

        await this.usecase.execute(model.getModelRequest())
        
        res.status(this.modelView.statusCode).send(this.modelView)

        return 
    }
}

export class ApiGetTransactionController implements IGetTransactionUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IGetTransactionUseCase

    constructor(transactionAdapter: IGetTransactionAdapter) {
        this.usecase = new GetTransactionUseCase(transactionAdapter, this)
    }

    success(transaction: TransactionResponse): void {
        this.modelView.success = true
        this.modelView.statusCode = 200
        this.modelView.data = transaction
    }

    fail(err: Error): void {
        this.modelView.success = false
        this.modelView.statusCode = 400
        this.modelView.error = {
            field: "", message: err.message
        }
    }

    async execute(req: Request, res: Response): Promise<void> {
        this.modelView = initApiResponse()

        await this.usecase.execute(req.params.id)

        res.status(this.modelView.statusCode).send(this.modelView)

        return 
    }
}

class PaginationTransactionModel { 
    private model: RequestGetPagination

    constructor(reqQuery: any) {
        this.model = {
            page: reqQuery.page ? reqQuery.page : 0,
            limit: reqQuery.limit ? reqQuery.limit : 0,
            sortBy: reqQuery.sortBy ? reqQuery.sortBy : '',
            sortSense: reqQuery.sortSense ? reqQuery.sortSense : '',
            accountFilter: reqQuery.accountFilter ? reqQuery.accountFilter : [],
            categoryFilter: reqQuery.categoryFilter ? reqQuery.categoryFilter : [],
            tagFilter: reqQuery.tagFilter ? reqQuery.tagFilter : [],
            dateStart: reqQuery.dateStart ? reqQuery.dateStart : '',
            dateEnd: reqQuery.dateEnd ? reqQuery.dateEnd : '',
            type: reqQuery.type ? reqQuery.type : '',
            mainCategory: reqQuery.type ? reqQuery.type : '', 
            minPrice: reqQuery.minPrice ? reqQuery.minPrice : undefined,
            maxPrice: reqQuery.maxPrice ? reqQuery.maxPrice : undefined
        }
    }

    validateInput(): ApiError[] {
        let errors: ApiError[] = []

        return errors
    }

    getModelRequest(): RequestGetPagination {
        return this.model
    }
}

export class ApiPaginationTransactionController implements IGetPaginationTransactionResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IGetPaginationTransaction

    constructor(transactionAdapter: IGetPaginationTransactionAdapter) {
        this.usecase = new GetPaginationTransaction(transactionAdapter, this)
    }

    success(response: TransactionPaginationResponse): void {
        this.modelView.success = true
        this.modelView.statusCode = 200
        this.modelView.data = response
    }

    fail(err: Error): void {
        this.modelView.success = false
        this.modelView.statusCode = 400
        this.modelView.error = {
            field: "", message: err.message
        }
    }

    async execute(req: Request, res: Response): Promise<void> {
        this.modelView = initApiResponse()
        let model = new PaginationTransactionModel(req.query)

        await this.usecase.execute(model.getModelRequest())

        res.status(this.modelView.statusCode).send(this.modelView)

        return 
    }
}

class TransfertTransactionModel { 
    private model: RequestTransfertTransactionUseCase

    constructor(reqBody: any) {
        this.model = {
            accountRefTo: reqBody.accountFromTo,
            accountRefFrom: reqBody.accountFromTo,
            amount: reqBody.amount,
            date: reqBody.date
        }
    }

    validateInput(): ApiError[] {
        let errors: ApiError[] = []

        if (isEmpty(this.model.accountRefTo))
            errors.push({field: "accountId", message: "account To field is empty"})

        if (isEmpty(this.model.accountRefFrom))
            errors.push({field: "categoryId", message: "account from field is empty"})

        if (this.model.amount <= 0) 
            errors.push({field: "amount", message: "Transaction is amount must be greater than 0"})

        if (isEmpty(this.model.date))
            errors.push({field: "date", message: "Transaction is date is empty"})

        return errors
    }

    getModelRequest(): RequestTransfertTransactionUseCase {
        return this.model
    }
}


export class ApiTransfertTransactionController implements ITransfertTransactionUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: TransfertTransactionUseCase

    constructor(transactionAdapter: ITransfertTransactionAdapter) {
        this.usecase = new TransfertTransactionUseCase(transactionAdapter, this)
    }

    fail(err: Error): void {
        this.modelView.success = false
        this.modelView.statusCode = 400
        this.modelView.error = {
            field: "", message: err.message
        }
    }

    success(isTransfert: boolean): void {
        this.modelView.success = isTransfert
        this.modelView.statusCode = 200
    }

    async execute(req: Request, res: Response): Promise<void> {
        this.modelView = initApiResponse()
        let model = new TransfertTransactionModel(req)
        let errors = model.validateInput()
        if (errors.length > 0) {
            if (req.query.validate_all)
                this.modelView.errors = errors
            else 
                this.modelView.error = errors[0]
            this.modelView.statusCode = 400
            res.status(this.modelView.statusCode).send(this.modelView)
        }

        await this.usecase.execute(model.getModelRequest())

        res.status(this.modelView.statusCode).send(this.modelView)
        return
    }
}

class GetBalanceModel { 
    private model: RequestGetBalanceBy

    constructor(reqQuery: any) {
        this.model = {
            accountsIds:reqQuery.accountIds ? reqQuery.accountIds.split(';') :[],
            tagsIds:reqQuery.tagIds ? reqQuery.tagIds.split(';') : [],
            categoriesIds:reqQuery.categoriesIds ? reqQuery.categoriesIds.split(';'): [],
            dateStart: reqQuery.dateStart ? reqQuery.dateStart : '',
            dateEnd: reqQuery.dateEnd ? reqQuery.dateEnd : '',
            type: reqQuery.type ? reqQuery.type : '' ,
            mainCategory: reqQuery.mainCategory ? reqQuery.mainCategory : '' ,
            minPrice: reqQuery.minPrice ? reqQuery.minPrice : '',
            maxPrice: reqQuery.maxPrice ? reqQuery.maxPrice : '' 
        }
    }

    validateInput(): ApiError[] {
        let errors: ApiError[] = []

        return errors
    }

    getModelRequest(): RequestGetBalanceBy {
        return this.model
    }
}

export class ApiGetBalanceController implements IGetBalanceByUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IGetBalanceByUseCase

    constructor(transRepo: TransactionRepository, recordRepository: RecordRepository) {
        this.usecase = new GetBalanceByUseCase(transRepo, recordRepository, this)
    }
    success(balance: number): void {
        this.modelView.success = true
        this.modelView.statusCode = 200
        this.modelView.data = {
            balance: balance
        }
    }

    fail(err: Error): void {
        this.modelView.statusCode = 400
        this.modelView.success = false
        this.modelView.error = {
            field: "", message: err.message
        }
    }

    async execute(req: Request, res: Response): Promise<void> {
        this.modelView = initApiResponse()
        let model = new GetBalanceModel(req.query)
        
        await this.usecase.execute(model.getModelRequest())

        res.status(this.modelView.statusCode).send(this.modelView)
        return 
    }
}

class UpdateTransactionModel { 
    private model: RequestUpdateTransactionUseCase

    constructor(id: string, reqBody: any) {
        this.model = {
            id: id,
            accountRef: reqBody.accountId ? reqBody.accountId : '',
            categoryRef: reqBody.categoryId ? reqBody.categoryId : '',
            amount: reqBody.amount ? reqBody.amount : '',
            description: reqBody.description ? reqBody.description : '',
            tagsRef: reqBody.tagIds ? reqBody.tagIds : [],
            type: reqBody.typeTransaction ? reqBody.typeTransaction : '',
            mainCategory: reqBody.mainCategory ? reqBody.mainCategory : '' ,
            date: reqBody.date ? reqBody.date : ''
        }
    }

    validateInput(): ApiError[] {
        let errors: ApiError[] = []

        if (isEmpty(this.model.accountRef))
            errors.push({field: "accountId", message: "account field is empty"})

        if (isEmpty(this.model.categoryRef))
            errors.push({field: "categoryId", message: "category field is empty"})

        if (this.model.amount <= 0)
            errors.push({field: "amount", message: "amout must be greater than 0"})

        if (isEmpty(this.model.type)) 
            errors.push({field: "type", message: "you have to choose type of transaction 'credit' or 'debit' "})

        if (isEmpty(this.model.date))
            errors.push({field: "date", message: "date field is"})

        return errors
    }

    getModelRequest(): RequestUpdateTransactionUseCase {
        return this.model
    }
}

export class ApiUpdateTransactionController implements IUpdateTransactionUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IUpdateTransactionUseCase

    constructor(transactionAdapter: IUpdateTransactionAdapter) {
        this.usecase = new UpdateTransactionUseCase(transactionAdapter, this)
    }
    
    success(newTransactionId: string): void {
        this.modelView.statusCode = 200
        this.modelView.success = true
        this.modelView.data = {
            newTransactionId: newTransactionId
        }
    }

    fail(err: Error): void {
        this.modelView.success = false
        this.modelView.statusCode = 400
        this.modelView.error = {
            field: "", message: err.message
        }
    }

    async execute(req: Request, res: Response): Promise<void> {
        this.modelView = initApiResponse()
        let model = new UpdateTransactionModel(req.params.id, req.body)

        let errors = model.validateInput()

        if (errors.length > 0) {
            if (req.query.validate_all)
                this.modelView.errors = errors
            else 
                this.modelView.error = errors[0]

            this.modelView.statusCode = 400
            res.status(this.modelView.statusCode).send(this.modelView)
            return
        }

        await this.usecase.execute(model.getModelRequest())

        res.status(this.modelView.statusCode).send(this.modelView)

        return
    }
}

export class ApiDeleteTransactionController implements IDeleteTransactoinUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IDeleteTransactionUseCase
    constructor(transactionRepo: TransactionRepository, recordRepo: RecordRepository, unitOfWork: UnitOfWorkRepository, accountRepo: AccountRepository) {
        this.usecase = new DeleteTransactionUseCase(transactionRepo, recordRepo, unitOfWork, accountRepo, this)
    }
    success(isDeleted: boolean): void {
        this.modelView.statusCode = 201
        this.modelView.success = isDeleted
    }
    fail(err: Error): void {
        this.modelView.statusCode = 400
        this.modelView.success = false
        this.modelView.error = {
            field: "", message: err.message
        }
    }

    async execute(req: Request, res: Response): Promise<void> {
        this.modelView = initApiResponse()
        
        await this.usecase.execute(req.params.id)

        res.status(this.modelView.statusCode).send(this.modelView)
        return
    }
}

class FreezeTransactionModel { 
    private model: RequestNewFreezeBalance

    constructor(reqBody: any) {
        this.model = {
            accountRef: reqBody.accountId,
            amount: reqBody.amount,
            endDate: reqBody.endDate
        }
    }

    validateInput(): ApiError[] {
        let errors: ApiError[] = []

        if (isEmpty(this.model.accountRef))
            errors.push({field: "accountId", message: "account field is empty"})

        if (isEmpty(this.model.amount)) 
            errors.push({field: "type", message: "you have to choose type of transaction 'credit' or 'debit' "})

        if (isEmpty(this.model.endDate))
            errors.push({field: "date", message: "date field is"})

        return errors
    }

    getModelRequest(): RequestNewFreezeBalance {
        return this.model
    }
}

export class ApiCreateFreezeTransactionController implements IAddFreezeBalancePresenter {
    modelView: ApiResponse = initApiResponse()
    usecase: IAddFreezeBalanceUseCase 

    constructor(dateService: DateService, transactionRepo: TransactionRepository, accountRepo: AccountRepository, 
        categoryRepo: CategoryRepository, recordRepo: RecordRepository, unitOfWork: UnitOfWorkRepository) {
        this.usecase = new AddFreezeBalanceUseCase(dateService, transactionRepo, this, accountRepo, categoryRepo, recordRepo, unitOfWork)
    }
    success(success: boolean): void {
        this.modelView.success = success
        this.modelView.statusCode = 200
    }
    fail(err: Error): void {
        this.modelView.statusCode = 400
        this.modelView.success = false
        this.modelView.error = {
            field: "", message: err.message
        }
    }

    async execute(req: Request, res: Response): Promise<void> {
        this.modelView = initApiResponse()
        let model = new FreezeTransactionModel(req.body)

        let errors = model.validateInput()

        if (errors.length > 0) {
            if (req.query.validate_all)
                this.modelView.errors = errors
            else 
                this.modelView.error = errors[0]

            this.modelView.statusCode = 400
            res.status(this.modelView.statusCode).send(this.modelView)
            return
        }

        await this.usecase.execute(model.getModelRequest())

        res.status(this.modelView.statusCode).send(this.modelView)

        return
    }
}

export class ApiAutoDeleteFreezeTransactionController implements IAutoDeleteFreezeBalancePresenter {
    modelView: ApiResponse = initApiResponse()
    usecase: IAutoDeleteFreezeBalanceUseCase

    constructor(accountRepository: AccountRepository, transactionRepository: TransactionRepository, recordRepository: RecordRepository, unitOfWork: UnitOfWorkRepository, 
        dateService: DateService) {
        this.usecase = new AutoDeleteFreezeBalanceUseCase(accountRepository, transactionRepository, recordRepository, unitOfWork, dateService, this)
    }

    success(message: string): void {
        this.modelView.success = true
        this.modelView.statusCode = 201
    }

    fail(err: Error): void {
        this.modelView.success = false
        this.modelView.statusCode = 400
        this.modelView.error = {
            field: "", message: err.message
        }
    }
    
    async execute(req: Request, res: Response): Promise<void> {
        this.modelView = initApiResponse()

        await this.usecase.execute()

        res.status(this.modelView.statusCode).send(this.modelView)
        return 
    }
}