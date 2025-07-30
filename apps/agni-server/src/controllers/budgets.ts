import { Request, Response } from 'express';
import { RequestCreationBudgetUseCase } from "@core/interactions/budgets/creationBudgetUseCase";
import { GetAllBudgetDto } from "@core/interactions/budgets/getAllBudgetUseCase";
import { GetBudgetDto } from "@core/interactions/budgets/getBudgetUseCase";
import { RequestUpdateBudget } from "@core/interactions/budgets/updateBudgetUseCase";
import { ApiController } from "./base";
import { IUsecase } from "@core/interactions/interfaces";
import { CreatedDto, ListDto } from "@core/dto/base";
import { Router } from "express";
import { body, matchedData, validationResult } from 'express-validator';

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
        this.router.get('/v1/budgets', 
            this.handleGetAllBudget);

        this.router.put('/v1/budgets/:id', 
            body('title').isEmpty(),
            body('target').isEmpty().isNumeric(),
            body('schedule.period').isEmpty(),
            body('schedule.periodTime').isEmpty().isNumeric(),
            body('schedule.dateStart').isEmpty().isDate(),
            body('schedule.dateEnd').isEmpty().isDate(),
            this.handleUpdateBudget);

        this.router.post('/v1/budgets', 
            body('title').notEmpty(),
            body('target').notEmpty().isNumeric(),
            body('schedule.period').notEmpty(),
            body('schedule.periodTime').isEmpty().isNumeric(),
            body('schedule.dateStart').notEmpty().isDate(),
            body('schedule.dateEnd').isEmpty().isDate(),
            this.handleCreateBudget);

        this.router.get('/v1/budgets/:id', 
            this.handleGetBudget);

        this.router.delete('/v1/budgets/:id', 
            this.handleDeleteBudget);
    };

    getRoute() {
        return this.router;
    };

    async handleCreateBudget(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestCreationBudgetUseCase = matchedData(req);
            var created = await this.createBudget.execute(data);
            
            res.status(200).send(created);
        }

        res.send({ errors: result.array() });
    }

    async handleUpdateBudget(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestUpdateBudget = matchedData(req);
            data.id = req.params.id;
            await this.updateBudget.execute(data);

            res.status(201);
        }
        
        res.send({ errors: result.array() });
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
