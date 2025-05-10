import { Request, Response } from "express";
import { AddSaveGoalUseCase, IAddSaveGoalPresenter, IAddSaveGoalUseCase, RequestAddSaveGoalUseCase } from "@core/interactions/saveGoal/addSaveGoal";
import { DecreaseSaveGoalUseCase, IdecreaseSaveGoalPresenter, IDecreaseSaveGoalUseCase, RequestDecreaseSaveGoal } from "@core/interactions/saveGoal/decreaseSaveGoal";
import { DeleteSaveGoalUseCase, IDeleteSaveGaolAdapter, IDeleteSaveGoalPresenter, IDeleteSaveGoalUseCase, RequestDeleteSaveGoal } from "@core/interactions/saveGoal/deleteSaveGoal";
import { IGetAllSaveGoalPresenter, IGetAllSaveGoalUseCase, SaveGoalsResponse, GetAllSaveGoalUseCase } from "@core/interactions/saveGoal/getAllSaveGoal";
import { GetSaveGoalUseCase, IGetSaveGoalPresenter, IGetSaveGoalUseCase, SaveGoalResponse} from "@core/interactions/saveGoal/getSaveGoal";
import { IIncreaseSaveGoalPresenter, IIncreaseSaveGoalUseCase, IncreaseSaveGoalUseCase, RequestIncreaseSaveGoal } from "@core/interactions/saveGoal/increaseSaveGoal";
import { IUpdateSaveGoalPresenter, IUpdateSaveGoalUseCase, RequestUpdateSaveGoalUseCase, UpdateSaveGoalUseCase } from "@core/interactions/saveGoal/updateSaveGoal";
import { ApiError, ApiResponse, initApiResponse } from "./type";
import { isEmpty } from "@core/domains/helpers";
import { SavingRepository } from "@core/repositories/savingRepository";
import { AccountRepository } from "@core/repositories/accountRepository";
import { TransactionRepository } from "@core/repositories/transactionRepository";
import { DateService } from "@core/adapters/libs";
import { RecordRepository } from "@core/repositories/recordRepository";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { CategoryRepository } from "@core/repositories/categoryRepository";

class CreateSaveGoalModel {
    model: RequestAddSaveGoalUseCase

    constructor(reqBody: any) {
        this.model = { 
            title: reqBody.title,
            description: reqBody.description,
            items: reqBody.items ? reqBody.items : [],
            target: reqBody.target ? reqBody.target : 0
        }
    }

    validationInput(): ApiError[] {
        let errors: ApiError[] = []

        if (isEmpty(this.model.title))
            errors.push({field: "title", message: "Title field is empty"})

        if (this.model.target <= 0)
            errors.push({field: "target", message: "Target of save goal must be greater than 0"})


        return errors
    }

    getModelRequest(): RequestAddSaveGoalUseCase {
        return this.model
    }
}


export class ApiCreateSavingGoalController implements IAddSaveGoalPresenter {
    modelView: ApiResponse = initApiResponse()
    usecase: IAddSaveGoalUseCase

    constructor(savingGoalRepo: SavingRepository) {
        this.usecase = new AddSaveGoalUseCase(savingGoalRepo, this)
    }

    success(newSavingId: string): void {
        this.modelView.success = true
        this.modelView.statusCode = 200
        this.modelView.data = {
            newSavingId: newSavingId
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
        let model = new CreateSaveGoalModel(req.body)
        let errors = model.validationInput()

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

export class ApiGetSavingGoalController implements IGetSaveGoalPresenter {
    modelView: ApiResponse = initApiResponse()
    usecase: IGetSaveGoalUseCase
    constructor(saveRepo: SavingRepository) {
        this.usecase = new GetSaveGoalUseCase(this, saveRepo)
    }

    success(saveGoal: SaveGoalResponse): void {
        this.modelView.success = true
        this.modelView.statusCode = 200
        this.modelView.data = saveGoal
    }

    fail(err: Error): void {
        this.modelView.success = true
        this.modelView.statusCode = 200
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

export class ApiGetAllSavingGoalController implements IGetAllSaveGoalPresenter {
    modelView: ApiResponse = initApiResponse()
    usecase: IGetAllSaveGoalUseCase

    constructor(savingRepo: SavingRepository) {
        this.usecase = new GetAllSaveGoalUseCase(this, savingRepo)
    }

    success(response: SaveGoalsResponse[]): void {
        this.modelView.statusCode = 200
        this.modelView.success = true 
        this.modelView.data = response
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
        
        await this.usecase.execute()

        res.status(this.modelView.statusCode).send(this.modelView)
        return 
    }
}

class UpdateSaveGoalModel {
    model: RequestUpdateSaveGoalUseCase

    constructor(id: string, reqBody: any) {
        this.model = { 
            savingGoalRef: id,
            title: reqBody.title ? reqBody.title : '',
            description: reqBody.description ? reqBody.description : '',
            items: reqBody.items ? reqBody.items : [],
            target: reqBody.target ? reqBody.target : 0
        }
    }

    validationInput(): ApiError[] {
        let errors: ApiError[] = []

        if (isEmpty(this.model.title))
            errors.push({field: "title", message: "Title field is empty"})

        if (this.model.target <= 0)
            errors.push({field: "target", message: "Target of save goal must be greater than 0"})


        return errors
    }

    getModelRequest(): RequestUpdateSaveGoalUseCase {
        return this.model
    }
}

export class ApiUpdateSavingGoalController implements IUpdateSaveGoalPresenter {
    modelView: ApiResponse = initApiResponse()
    usecase: IUpdateSaveGoalUseCase

    constructor(savingGoalRepo: SavingRepository) {
        this.usecase = new UpdateSaveGoalUseCase(this, savingGoalRepo)
    }

    success(isSave: boolean): void {
        this.modelView.success = isSave 
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
        let model = new UpdateSaveGoalModel(req.params.id, req.body)
        let errors = model.validationInput() 

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

class IncreaseSaveGoalModel {
    model: RequestIncreaseSaveGoal

    constructor(id: string, reqBody: any) {
        this.model = { 
            savingGoalRef: id,
            accountRef: reqBody.accountFromId,
            increaseAmount: reqBody.amount ? reqBody.amount :  0
        }
    }

    validationInput(): ApiError[] {
        let errors: ApiError[] = []

        if (isEmpty(this.model.accountRef))
            errors.push({field: "accountFromId", message: "You must choose account from"})

        if (this.model.increaseAmount <= 0)
            errors.push({field: "target", message: "Target of save goal must be greater than 0"})


        return errors
    }

    getModelRequest(): RequestIncreaseSaveGoal {
        return this.model
    }
}

export class ApiIncreaseSavingGoalController implements IIncreaseSaveGoalPresenter {
    modelView: ApiResponse = initApiResponse()
    usecase: IIncreaseSaveGoalUseCase

    constructor(categoryRepo: CategoryRepository, savingRepo: SavingRepository, accountRepo: AccountRepository, transRepo: TransactionRepository, 
        dateService: DateService, recordRepo: RecordRepository, unitOfWork: UnitOfWorkRepository) {

        this.usecase = new IncreaseSaveGoalUseCase(this, categoryRepo, accountRepo, savingRepo, transRepo, dateService, recordRepo, unitOfWork)
    }

    success(isSave: boolean): void {
        this.modelView.success = isSave
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
        let model = new IncreaseSaveGoalModel(req.params.id, req.body)
        let errors = model.validationInput()
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

class DecreaseSavingGoalModel {
    model: RequestDecreaseSaveGoal

    constructor(id: string, reqBody: any) {
        this.model = { 
            savingGoalRef: id,
            accountRef: reqBody.accountToId,
            decreaseAmount: reqBody.amount ? reqBody.amount :  0
        }
    }

    validationInput(): ApiError[] {
        let errors: ApiError[] = []

        if (isEmpty(this.model.accountRef))
            errors.push({field: "accountToId", message: "You must choose account from"})

        if (this.model.decreaseAmount <= 0)
            errors.push({field: "target", message: "Target of save goal must be greater than 0"})


        return errors
    }

    getModelRequest(): RequestDecreaseSaveGoal {
        return this.model
    }
}

export class ApiDecreaseSavingGoalController implements IdecreaseSaveGoalPresenter {
    modelView: ApiResponse = initApiResponse()
    usecase: IDecreaseSaveGoalUseCase

    constructor(categoryRepo: CategoryRepository,  savingRepo: SavingRepository, accountRepo: AccountRepository, transRepo: TransactionRepository, 
        dateService: DateService, recordRepo: RecordRepository, unitOfWork: UnitOfWorkRepository) {
        this.usecase = new DecreaseSaveGoalUseCase(this,  categoryRepo,  accountRepo, savingRepo, transRepo, dateService, recordRepo, unitOfWork)
    }

    success(isSave: boolean): void {
        this.modelView.success = isSave
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
        let model = new DecreaseSavingGoalModel(req.params.id, req.body)
        let errors = model.validationInput()
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

class DeleteSavingGoalModel {
    model: RequestDeleteSaveGoal

    constructor(id:string, reqBody: any) {
        this.model = { 
            saveGoalRef: id,
            accountTranfertRef: reqBody.accountDepositId
        }
    }

    validationInput(): ApiError[] {
        let errors: ApiError[] = []

        if (isEmpty(this.model.accountTranfertRef))
            errors.push({field: "accountDepositId", message: "Account deposit field is empty"})

        return errors
    }

    getModelRequest(): RequestDeleteSaveGoal {
        return this.model
    }
}

export class ApiDeleteSavingGoalController implements IDeleteSaveGoalPresenter {
    modelView: ApiResponse = initApiResponse()
    usecase: IDeleteSaveGoalUseCase

    constructor(savingAdpter: IDeleteSaveGaolAdapter) {
        this.usecase = new DeleteSaveGoalUseCase(savingAdpter, this)
    }
    success(isDelete: boolean): void {
        this.modelView.success = isDelete
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
        let model = new DeleteSavingGoalModel(req.params.id, req.body)
        let errors = model.validationInput()

        if (errors.length > 0) {
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

