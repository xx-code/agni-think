import { Request, Response } from 'express';
import { RequestCreateBudgetSchedule, RequestCreationBudgetUseCase } from "@core/interactions/budgets/creationBudgetUseCase";
import { GetAllBudgetDto } from "@core/interactions/budgets/getAllBudgetUseCase";
import { GetBudgetDto } from "@core/interactions/budgets/getBudgetUseCase";
import { RequestUpdateBudget } from "@core/interactions/budgets/updateBudgetUseCase";
import { ApiController } from "./base";
import { IUsecase } from "@core/interactions/interfaces";
import { CreatedDto, ListDto } from "@core/dto/base";
import { Router } from "express";

export default class BudgetController implements ApiController {
    private router = Router();

    private createBudget: IUsecase<RequestCreationBudgetUseCase, CreatedDto>;
    private updateBudget: IUsecase<RequestUpdateBudget, void>;
    private getBudget: IUsecase<string, GetBudgetDto>;
    private getAllBudget: IUsecase<void, ListDto<GetAllBudgetDto>>;
    private deleteBudget: IUsecase<string, void>;

    constructor(
        createBudget: IUsecase<RequestCreationBudgetUseCase, CreatedDto>,
        updateBudget: IUsecase<RequestUpdateBudget, void>,
        getBudget: IUsecase<string, GetBudgetDto>,
        getAllBudget: IUsecase<void, ListDto<GetAllBudgetDto>>,
        deleteBudget: IUsecase<string, void>
    ) {
        this.createBudget = createBudget;
        this.updateBudget = updateBudget;
        this.getBudget = getBudget;
        this.getAllBudget = getAllBudget; 
        this.deleteBudget = deleteBudget;
    }

    setupRoutes(){
        this.router.get('/v1/budgets', this.handleGetAllBudget);
        this.router.put('/v1/budgets/:id', this.handleUpdateBudget);
        this.router.post('/v1/budgets', this.handleCreateBudget);
        this.router.get('/v1/budgets/:id', this.handleGetBudget);
        this.router.delete('/v1/budgets/:id', this.handleDeleteBudget)
    };

    getRoute() {
        return this.router;
    };

    async handleCreateBudget(req: Request, res: Response) {
        var created = await this.createBudget.execute({
            title: req.body.title,
            target: req.body.target,
            schedule: req.body.schedule as RequestCreateBudgetSchedule
        });
        
        res.status(200).send(created);
    }

    async handleUpdateBudget(req: Request, res: Response) {
        await this.updateBudget.execute({
            id: req.params.id,
            target: req.body.target,
            title: req.body.title,
            schedule: req.body.schedule as RequestCreateBudgetSchedule
        });

        res.status(201);
    }

    async handleGetBudget(req: Request, res: Response) {
        var budget = await this.getBudget.execute(req.params.id);        

        res.status(200).send(budget);
    }

    async handleGetAllBudget(req: Request, res: Response) {
        var budgets = await this.getAllBudget.execute();

        res.status(200).send(budgets);
    }

    async handleDeleteBudget(req: Request, res: Response) {
        await this.deleteBudget.execute(req.params.id);

        res.status(201);
    }
}
