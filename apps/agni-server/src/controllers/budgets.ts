import { Request, Response } from "express";
import { AutoUpdateBudgetUseCase, IAutoUpdateBudgetPresenter, IAutoUpdateBudgetUseCase } from "@core/interactions/budgets/autoUpdateBudgetUseCase";
import { CreationBudgetUseCase, ICreationBudgetAdapter, ICreationBudgetUseCase, ICreationBudgetUseCaseResponse, RequestCreationBudgetUseCase } from "@core/interactions/budgets/creationBudgetUseCase";
import { BudgetsOutput, GetAllBudgetUseCase, IGetAllBudgetAdapter, IGetAllBudgetUseCase, IGetAllBudgetUseCaseResponse } from "@core/interactions/budgets/getAllBudgetUseCase";
import { BudgetOutput, GetBudgetUseCase, IGetBudgetAdpater, IGetBudgetUseCase, IGetBudgetUseCaseResponse } from "@core/interactions/budgets/getBudgetUseCase";
import { IUpdateBudgetAdapter, IUpdateBudgetUseCase, IUpdateBudgetUseCasePresenter, RequestUpdateBudget, UpdateBudgetUseCase } from "@core/interactions/budgets/updateBudgetUseCase";
import { ApiError, ApiResponse, initApiResponse } from "./type";
import { isEmpty } from "@core/domains/helpers";
import { DateService } from "@core/adapters/libs";
import { BudgetRepository } from "@core/repositories/budgetRepository";
import { DeleteBudgetUseCase, IDeleteBudgetUseCase } from "@core/interactions/budgets/deleteBudgetUseCase";

class CreateBudgetModel {
    model: RequestCreationBudgetUseCase

    constructor(reqBody: any) {
        this.model = { 
            title: reqBody.title ? reqBody.title : '',
            target: reqBody.target ? reqBody.target : '',
            period: reqBody.period ? reqBody.period : '',
            periodTime: reqBody.periodTime ? reqBody.periodTime : 0,
            dateStart: reqBody.dateStart ? reqBody.dateStart : '',
            dateEnd: reqBody.dateEnd ? reqBody.dateEnd : ''
        }
    }

    validationInput(): ApiError[] {
        let errors: ApiError[] = []

        if (isEmpty(this.model.title))
            errors.push({field: "title", message: "Title field is empty"})

        if (this.model.target <= 0)
            errors.push({field: "target", message: "Target field must be greater than 0"})

        if (isEmpty(this.model.dateStart))
            errors.push({field: "date start", message: "You have to set date start"})

        return errors
    }

    getModelRequest(): RequestCreationBudgetUseCase {
        return this.model
    }
}

export class ApiCreateBudgetController implements ICreationBudgetUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: ICreationBudgetUseCase 

    constructor(budgetAdapter: ICreationBudgetAdapter, dateService: DateService) {
        this.usecase = new CreationBudgetUseCase(budgetAdapter, dateService, this)
    }
    success(newBudgetId: string): void {
        this.modelView.success = true
        this.modelView.statusCode = 200
        this.modelView.data = {
            newBudgetId: newBudgetId
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
        let model = new CreateBudgetModel(req.body)
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

export class ApiGetBudgetController implements IGetBudgetUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IGetBudgetUseCase

    constructor(budgetAdapter: IGetBudgetAdpater) {
        this.usecase = new GetBudgetUseCase(budgetAdapter, this)
    }
    success(budget: BudgetOutput): void {
        this.modelView.success = true
        this.modelView.statusCode = 200
        this.modelView.data = budget
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

export class ApiGetAllBudgetController implements IGetAllBudgetUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IGetAllBudgetUseCase

    constructor(budgetAdapter: IGetAllBudgetAdapter) {
        this.usecase = new GetAllBudgetUseCase(budgetAdapter, this)
    }
    success(budgets: Array<BudgetsOutput>): void {
        this.modelView.success = true
        this.modelView.statusCode = 200
        this.modelView.data = budgets
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

class UpdateBudgetModel {
    model: RequestUpdateBudget

    constructor(id: string, reqBody: any) {
        this.model = { 
            id: id,
            title: reqBody.title ? reqBody.title : '',
            target: reqBody.target ? reqBody.target : '',
            period: reqBody.period ? reqBody.period : '',
            periodTime: reqBody.periodTime ? reqBody.periodItem : 0,
            dateStart: reqBody.dateStart ? reqBody.dateStart : '',
            dateEnd: reqBody.dateEnd ? reqBody.dateEnd : ''
        }
    }

    validationInput(): ApiError[] {
        let errors: ApiError[] = []

        if (isEmpty(this.model.title))
            errors.push({field: "title", message: "Title field is empty"})

        if (this.model.target <= 0)
            errors.push({field: "target", message: "Target field must be greater than 0"})

        return errors
    }

    getModelRequest(): RequestUpdateBudget {
        return this.model
    }
}

export class ApiUpdateBudgetController implements IUpdateBudgetUseCasePresenter {
    modelView: ApiResponse = initApiResponse()
    usecase: IUpdateBudgetUseCase

    constructor(budgetAdapter: IUpdateBudgetAdapter) {
        this.usecase = new UpdateBudgetUseCase(budgetAdapter, this)
    }
    success(success: boolean): void {
        this.modelView.success = success
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
        let model = new UpdateBudgetModel(req.params.id, req.body)
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

export class ApiDeleteBudgetController implements IUpdateBudgetUseCasePresenter {
    modelView: ApiResponse = initApiResponse()
    usecase: IDeleteBudgetUseCase

    constructor(budgetRepo: BudgetRepository) {
        this.usecase = new DeleteBudgetUseCase(budgetRepo, this)
    }
    success(success: boolean): void {
        this.modelView.success = success
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

        await this.usecase.execute(req.params.id)

        res.status(this.modelView.statusCode).send(this.modelView)

        return
    }
}


export class ApiAutoBudgetController implements IAutoUpdateBudgetPresenter {
    modelView: ApiResponse = initApiResponse()
    usecase: IAutoUpdateBudgetUseCase

    constructor(budgetRepo: BudgetRepository, dateService: DateService) {
        this.usecase = new AutoUpdateBudgetUseCase(this, budgetRepo, dateService)
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