import { Request, Response } from "express";
import { CreationTagUseCase, ICreationTagUseCase, ICreationTagUseCaseResponse, RequestCreationTagUseCase } from "@core/interactions/tag/creationTagUseCase";
import { DeleteTagUseCase, IDeleteTagUseCase, IDeleteTagUseCaseResponse } from "@core/interactions/tag/deleteTagUseCase";
import { GetAllTagUseCase, IGetAllTagUseCase, IGetAllTagUseCaseResponse, TagsOutput } from "@core/interactions/tag/getAllTagsUseCase";
import { GetTagUseCase, IGetTagUseCase, IGetTagUseCaseResponse, TagOutput } from "@core/interactions/tag/getTagUseCase";
import { ApiError, ApiResponse, initApiResponse } from "./type";
import { isEmpty } from "@core/domains/helpers";
import { IUpdateTagUseCase, IUpdateTagUseCaseResponse, RequestUpdateTagUseCase, UpdateTagUseCase } from "@core/interactions/tag/updateTagUseCase";
import { TagRepository } from "@core/repositories/tagRepository";

class CreateTagModel {
    model: RequestCreationTagUseCase

    constructor(reqBody: any) {
        this.model = { 
            value: reqBody.value,
            color: reqBody.color      
        }
    }

    validationInput(): ApiError[] {
        let errors: ApiError[] = []

        if (isEmpty(this.model.value))
            errors.push({field: "value", message: "Title field is empty"})

        if (isEmpty(this.model.color))
            errors.push({field: "color", message: "you have to set icon value"})

        return errors
    }

    getModelRequest(): RequestCreationTagUseCase {
        return this.model
    }
}


export class ApiCreateTagController implements ICreationTagUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: ICreationTagUseCase

    constructor(tagRepo: TagRepository) { 
        this.usecase = new CreationTagUseCase(tagRepo, this)
    }

    success(newTagId: string): void {
        this.modelView.success = true
        this.modelView.statusCode = 200 
        this.modelView.data = {
            newTagId: newTagId
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
        let model = new CreateTagModel(req.body)
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

export class ApiGetTagController implements IGetTagUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IGetTagUseCase

    constructor(tagRepo: TagRepository) {
        this.usecase = new GetTagUseCase(tagRepo, this)
    }
    success(tag: TagOutput): void {
        this.modelView.success = true
        this.modelView.statusCode = 200
        this.modelView.data = tag
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

        this.modelView.success = true 
        res.status(this.modelView.statusCode).send(this.modelView)
        return 
    }
}

export class ApiGetAllTagController implements IGetAllTagUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IGetAllTagUseCase

    constructor(tagRepo: TagRepository) {
        this.usecase = new GetAllTagUseCase(tagRepo, this)
    }

    success(tags: TagsOutput[]): void {
        this.modelView.success = true
        this.modelView.statusCode = 200
        this.modelView.data = tags
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

class UpdateTagModel {
    model: RequestUpdateTagUseCase

    constructor(id:string, reqBody: any) {
        this.model = { 
            id: id,
            value: reqBody.value,
            color: reqBody.color      
        }
    }

    validationInput(): ApiError[] {
        let errors: ApiError[] = []

        if (isEmpty(this.model.value))
            errors.push({field: "value", message: "Title field is empty"})

        if (isEmpty(this.model.color))
            errors.push({field: "color", message: "you have to set icon value"})

        return errors
    }

    getModelRequest(): RequestUpdateTagUseCase {
        return this.model
    }
}

export class ApiUpdateTagController implements IUpdateTagUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IUpdateTagUseCase

    constructor(tagRepo: TagRepository) {
        this.usecase = new UpdateTagUseCase(tagRepo, this)
    }

    success(isDel: boolean): void {
        this.modelView.success = isDel
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
        let model = new UpdateTagModel(req.params.id, req.body)
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

export class ApiDeleteTagController implements IDeleteTagUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IDeleteTagUseCase

    constructor(tagRepo: TagRepository) {
        this.usecase = new DeleteTagUseCase(tagRepo, this)
    }

    success(success: boolean): void {
        this.modelView.success = success
        this.modelView.statusCode = 200
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