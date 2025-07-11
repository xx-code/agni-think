import { Request, Response, Router } from "express";
import { GetAccountDto } from "@core/interactions/account/getAccountUseCase";
import { GetAllAccountDto } from "@core/interactions/account/getAllAccountUseCase";
import { RequestUpdateAccountUseCase } from "@core/interactions/account/updateAccountUseCase";
import { IUsecase } from "@core/interactions/interfaces";
import { CreatedDto, ListDto } from "@core/dto/base";
import { RequestCreationAccountUseCase } from "@core/interactions/account/creationAccountUseCase";
import { ApiController } from "./base";

export default class AccountController implements ApiController {
    private route = Router();

    private createAccount: IUsecase<RequestCreationAccountUseCase, CreatedDto>
    private updateAccount: IUsecase<RequestUpdateAccountUseCase, void>
    private getAccount: IUsecase<string, GetAccountDto>
    private getAllAccount: IUsecase<void, ListDto<GetAllAccountDto>>
    private deleteAccount: IUsecase<string, void>

    constructor(
        createAccount: IUsecase<RequestCreationAccountUseCase, CreatedDto>,
        updateAccount: IUsecase<RequestUpdateAccountUseCase, void>,
        getAccount: IUsecase<string, GetAccountDto>,
        getAllAccount: IUsecase<void, ListDto<GetAllAccountDto>>,
        deleteAccount: IUsecase<string, void>
    ) {
        this.createAccount = createAccount;
        this.updateAccount = updateAccount;
        this.getAccount = getAccount;
        this.getAllAccount = getAllAccount;
        this.deleteAccount = deleteAccount;

        this.setupRoutes();
    }

    public setupRoutes() {
        this.route.post('/accounts', this.handleCreateAccount)
        this.route.put('/accounts/:id', this.handleUpdateAccount)
        this.route.get('/accounts/:id', this.handleGetAccount)
        this.route.get('/accounts', this.handleGetAllAccount)
        this.route.delete('/accounts/:id', this.handleDeleteAccount)
    }

    private async handleCreateAccount(req: Request, res: Response) {
        // body verification
        var ucRes = await this.createAccount.execute({ 
            title: req.body.title, 
            type: req.body.type
        })

        res.status(200).json(ucRes);
    }

    private async handleUpdateAccount(req: Request, res: Response) {
        await this.updateAccount.execute({
            id: req.params.id, 
            title: req.body.title,
            type: req.body.type
        })

       res.status(201)
    }

    private async handleGetAccount(req: Request, res: Response) {
        var ucRes = await this.getAccount.execute(req.params.id)
        res.status(200).json(ucRes)
    }

    private async handleGetAllAccount(req: Request, res: Response) {
        var ucRes = await this.getAllAccount.execute()
        res.status(200).json(ucRes)
    }

    private async handleDeleteAccount(req: Request, res: Response) {
        await this.deleteAccount.execute(req.params.id)
        res.status(201)
    }

    public getRoute(): Router {
        return this.route
    }
}