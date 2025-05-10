import { Request, Response } from "express";
import { CreationAccountUseCase, ICreationAccountUseCaseResponse, ICreationAccountUseCase, RequestCreationAccountUseCase } from "@core/interactions/account/creationAccountUseCase";
import { AccountRepository } from "@core/repositories/accountRepository";
import { ApiError, ApiResponse, initApiResponse } from "./type";
import { isEmpty } from "@core/domains/helpers";
import { AccountResponse, GetAccountUseCase, IGetAccountUseCase, IGetAccountUseCaseResponse } from "@core/interactions/account/getAccountUseCase";
import { GetAllAccountUseCase, IGetAllAccountUseCase, IGetAllAccountUseCaseResponse } from "@core/interactions/account/getAllAccountUseCase";
import { IUpdateAccountUseCase, IUpdateAccountUseCaseResponse, RequestUpdateAccountUseCase, UpdateAccountUseCase } from "@core/interactions/account/updateAccountUseCase";
import { DeleteAccountUseCase, IDeleteAccountUseCase, IDeleteAccountUseCaseResponse } from "@core/interactions/account/deleteAccountUseCase";

class CreateAccountModel {
    private model: RequestCreationAccountUseCase
    
    constructor (reqBody: any) {
        this.model = {
            title: reqBody.title,
            type: reqBody.type ? reqBody.type : ''
        }
    }

    validateRequestInput(): ApiError[] {
        let errors: ApiError[] = []

        if (isEmpty(this.model.title))
            errors.push({field: 'title', message: 'message field is empty'})

        if (isEmpty(this.model.type))
            errors.push({field: 'type', message: 'type field is empty'})

        return errors
    }  

    getUseCaseRequest(): RequestCreationAccountUseCase{
        return this.model
    }
}

export class ApiCreateAccountControler implements ICreationAccountUseCaseResponse {
    usecase: ICreationAccountUseCase
    modelView: ApiResponse = initApiResponse()

    constructor( accountRepo: AccountRepository) {
        this.usecase = new CreationAccountUseCase(accountRepo, this)
    }

    async execute(req: Request, res: Response): Promise<void> {
        this.modelView = initApiResponse()
        let model = new CreateAccountModel(req.body)
        let errors = model.validateRequestInput()
        
        if (errors.length > 0) {
            if (req.query.validate_all)
                this.modelView.errors = errors
            else 
                this.modelView.error = errors[0]

            this.modelView.statusCode = 400

            res.status(400)
            res.send(this.modelView)
            return
        }

        await this.usecase.execute(model.getUseCaseRequest())

        res.status(this.modelView.statusCode)
        res.send(this.modelView)
        return
    }

    success(newAccountId: string): void {
        this.modelView.success = true
        this.modelView.data = {
            accountId: newAccountId
        }
        this.modelView.statusCode = 201
    }

    fail(err: Error): void {
        this.modelView.success = false
        this.modelView.statusCode = 400
        this.modelView.error = {
            field: "",
            message: err.message
        }
    }
}


export class ApiGetAccountAccountController implements IGetAccountUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IGetAccountUseCase

    constructor(accountRepo: AccountRepository) {
        this.usecase = new GetAccountUseCase(accountRepo, this)
    }

    async execute(req: Request, res: Response): Promise<void> {
        this.modelView = initApiResponse()
        
        await this.usecase.execute(req.params.id)

        res.status(this.modelView.statusCode).send(this.modelView)

        return 
    }

    success(account: AccountResponse): void {
        this.modelView.data = account
        this.modelView.success = true
        this.modelView.statusCode = 200
    }

    fail(error: Error): void {
        this.modelView.statusCode = 400
        this.modelView.success = false
        
        this.modelView.error = {
            field: "",
            message: error.message
        }
    }

}

export class ApiGetAllAccountController implements IGetAllAccountUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IGetAllAccountUseCase 

    constructor(accountRepo: AccountRepository) {
        this.usecase = new GetAllAccountUseCase(accountRepo, this)
    }

    async execute(req: Request, res: Response): Promise<void> {
        await this.usecase.execute()

        res.status(this.modelView.statusCode).send(this.modelView)
    }

    success(allAccount: Array<AccountResponse>): void {
        this.modelView.statusCode = 200
        this.modelView.data = allAccount
    }

    fail(err: Error): void {
        this.modelView.statusCode = 400
        this.modelView.error = {
            field: '',
            message: err.message
        }
    }
}

class UpdateAccountModel {
    private model: RequestUpdateAccountUseCase

    constructor(id: string, reqBody: any) {
        this.model = {
            id: id,
            title: reqBody.title ? reqBody.title : '',
            type: reqBody.type ? reqBody.type : ''
        }
    }

    validateRequestInput(): ApiError[] {
        let errors: ApiError[] = []

        if (isEmpty(this.model.title))
            errors.push({field: 'title', message: 'message field is empty'})

        if (isEmpty(this.model.type))
            errors.push({field: 'type', message: 'message field is empty'})

        return errors
    }  

    getModelRequest(): RequestUpdateAccountUseCase {
        return this.model
    }
}


export class ApiUpdateAccountController implements IUpdateAccountUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IUpdateAccountUseCase 

    constructor(accountRepo: AccountRepository) {
        this.usecase = new UpdateAccountUseCase(accountRepo, this)
    }

    async execute(req: Request, res: Response): Promise<void> {
        let model = new UpdateAccountModel(req.params.id, req.body)
        let errors = model.validateRequestInput()
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

        res.status(this.modelView.statusCode)
        res.send(this.modelView)

        return
    }

    success(accountUpdated: boolean): void {
        this.modelView.success = accountUpdated 
        this.modelView.statusCode = 200
    }

    fail(err: Error): void {
        this.modelView.success = false
        this.modelView.statusCode = 400
        this.modelView.error = {
            field: "",
            message: err.message
        }
    }
}

export class ApiDeleteAccountController implements IDeleteAccountUseCaseResponse {
    modelView: ApiResponse = initApiResponse()
    usecase: IDeleteAccountUseCase

    constructor(accountRepo: AccountRepository) {
        this.usecase = new DeleteAccountUseCase(accountRepo, this)
    }

    async execute(req: Request, res: Response): Promise<void> {
        this.modelView = initApiResponse()

        await this.usecase.execute(req.params.id)

        res.status(this.modelView.statusCode)
        res.json(this.modelView)
    }

    success(isDeleted: boolean): void {
        this.modelView.statusCode = 201
        this.modelView.success = isDeleted
    }

    fail(err: Error): void {
        this.modelView.statusCode = 400 
        this.modelView.success = false
        this.modelView.error = {
            field: "",
            message: err.message
        }
    }
}


