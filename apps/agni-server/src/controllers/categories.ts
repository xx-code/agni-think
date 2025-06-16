import { Request, Response } from "express";
import { CreationCategoryUseCase, ICreationCategoryUseCase, ICreationCategoryUseCaseResponse, RequestCreationCategoryUseCase } from "@core/interactions/category/creationCategoryUseCase";
import { DeleteCategoryUseCase, IDeleteCategoryUseCase, IDeleteCategoryUseCaseResponse } from "@core/interactions/category/deleteCategoryUseCase";
import { CategoriesResponse, GetAllCategoryUseCase, IGetAllCategoryUseCase, IGetAllCategoryUseCaseResponse } from "@core/interactions/category/getAllCategoryUseCase";
import { CategoryResponse, GetCategoryUseCase, IGetCategoryUseCase, IGetCategoryUseCaseResponse } from "@core/interactions/category/getCategoryUseCase";
import { IUpdateCategoryUseCase, IUpdateCategoryUseCaseResponse, RequestUpdateCategoryUseCase, UpdateCategoryUseCase } from "@core/interactions/category/updateCategoryUseCase";
import { ApiError, ApiResponse, initApiResponse } from "./type";
import { isEmpty } from "@core/domains/helpers";
import { CategoryRepository } from "@core/repositories/categoryRepository";

class CreateCategoryModel {
    model: RequestCreationCategoryUseCase

    constructor(reqBody: any) {
        this.model = { 
            title: reqBody.title,
            color: reqBody.color,
            icon: reqBody.icon        
        }
    }

    validationInput(): ApiError[] {
        let errors: ApiError[] = []

        if (isEmpty(this.model.title))
            errors.push({field: "title", message: "Title field is empty"})

        if (isEmpty(this.model.icon))
            errors.push({field: "icon", message: "you have to set icon value"})

        return errors
    }

    getModelRequest(): RequestCreationCategoryUseCase {
        return this.model
    }
}


export class ApiCreateCategoryController implements ICreationCategoryUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: ICreationCategoryUseCase

    constructor(categoryRepo: CategoryRepository) {
        this.usecase = new CreationCategoryUseCase(categoryRepo, this)
    }

    success(newCategoryId: string): void {
        this.modelView.success = true
        this.modelView.statusCode = 200
        this.modelView.data = {
            newCategoryId: newCategoryId
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
        let model = new CreateCategoryModel(req.body)
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

export class ApiGetCategoryController implements IGetCategoryUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IGetCategoryUseCase
    constructor(categoryRepo: CategoryRepository) {
        this.usecase = new GetCategoryUseCase(categoryRepo, this)
    }

    success(category: CategoryResponse): void {
        this.modelView.statusCode = 200
        this.modelView.success = true 
        this.modelView.data = category
    }

    fail(err: Error): void {
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

export class ApiGetAllCategoriesController implements IGetAllCategoryUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IGetAllCategoryUseCase

    constructor(categoryRepo: CategoryRepository) {
        this.usecase = new GetAllCategoryUseCase(categoryRepo, this)
    }

    success(categories: CategoriesResponse[]): void {
        this.modelView.statusCode = 200
        this.modelView.success = true 
        this.modelView.data = categories
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

class UpdateCategoryModel {
    model: RequestUpdateCategoryUseCase

    constructor(id: string, reqBody: any) {
        this.model = { 
            id: id,
            title: reqBody.title,
            color: reqBody.color,
            icon: reqBody.icon        
        }
    }

    validationInput(): ApiError[] {
        let errors: ApiError[] = []

        if (isEmpty(this.model.title))
            errors.push({field: "title", message: "Title field is empty"})

        if (isEmpty(this.model.icon))
            errors.push({field: "target", message: "you have to set icon value"})

        return errors
    }

    getModelRequest(): RequestUpdateCategoryUseCase {
        return this.model
    }
}

export class ApiUpdateCategoryController implements IUpdateCategoryUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IUpdateCategoryUseCase

    constructor(categoryRepo: CategoryRepository) {
        this.usecase = new UpdateCategoryUseCase(categoryRepo, this)
    }
    success(isUpdated: boolean): void {
        this.modelView.success = isUpdated
        this.modelView.statusCode = 201
    }
    fail(err: Error): void {
        this.modelView.success = false 
        this.modelView.error = {
            field: "", message: err.message
        }
    }

    async execute(req: Request, res: Response): Promise<void> {
        this.modelView = initApiResponse()
        let model = new UpdateCategoryModel(req.params.id, req.body)
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

export class ApiDeleteCategoryController implements IDeleteCategoryUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IDeleteCategoryUseCase

    constructor(categoryRepo: CategoryRepository) {
        this.usecase = new DeleteCategoryUseCase(categoryRepo, this)
    }

    success(isDeleted: boolean): void {
        this.modelView.success = isDeleted
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

        await this.usecase.execute(req.params.id)
        
        res.status(this.modelView.statusCode).send(this.modelView)

        return 
    }
}