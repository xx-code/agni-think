import { Request, Response, Router } from "express";
import { GetAccountDto } from "@core/interactions/account/getAccountUseCase";
import { GetAllAccountDto } from "@core/interactions/account/getAllAccountUseCase";
import { RequestUpdateAccountUseCase } from "@core/interactions/account/updateAccountUseCase";
import { IUsecase } from "@core/interactions/interfaces";
import { CreatedDto, ListDto } from "@core/dto/base";
import { RequestCreationAccountUseCase } from "@core/interactions/account/creationAccountUseCase";
import { ApiController } from "./base";
import { body, matchedData, validationResult } from "express-validator";

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
        this.route.post('/v1/accounts', 
            body('title').notEmpty(), 
            body('type').notEmpty(),
            this.handleCreateAccount);

        this.route.put('/v1/accounts/:id', 
            body('title').isEmpty(), 
            body('type').isEmpty(),
            this.handleUpdateAccount);

        this.route.get('/v1/accounts/:id', this.handleGetAccount)

        this.route.get('/v1/accounts', this.handleGetAllAccount)

        this.route.delete('/v1/accounts/:id', this.handleDeleteAccount)
    }

    private async handleCreateAccount(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestCreationAccountUseCase = matchedData(req);
            var ucRes = await this.createAccount.execute(data);

            res.status(200).json(ucRes);
        }

        res.send({ errors: result.array() });
    }

    private async handleUpdateAccount(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestUpdateAccountUseCase = matchedData(req);
            data.id = req.params.id;
            await this.updateAccount.execute(data);

            res.status(201);
        }
        
        res.send({ errors: result.array() });
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