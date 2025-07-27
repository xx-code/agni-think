import { Request, Response, Router } from "express";
import { RequestAddItemSaveGoalUseCase, RequestAddSaveGoalUseCase } from "@core/interactions/saveGoal/addSaveGoal";
import { RequestDecreaseSaveGoal } from "@core/interactions/saveGoal/decreaseSaveGoal";
import { RequestDeleteSaveGoal } from "@core/interactions/saveGoal/deleteSaveGoal";
import { GetAllSaveGoalDto } from "@core/interactions/saveGoal/getAllSaveGoal";
import { GetSaveGoalDto } from "@core/interactions/saveGoal/getSaveGoal";
import { RequestIncreaseSaveGoal } from "@core/interactions/saveGoal/increaseSaveGoal";
import { RequestUpdateItemSaveGoalUseCase, RequestUpdateSaveGoalUseCase } from "@core/interactions/saveGoal/updateSaveGoal";
import { ApiController } from "./base";
import { IUsecase } from "@core/interactions/interfaces";
import { CreatedDto, ListDto } from "@core/dto/base";

export default class SaveGoalController implements ApiController {
    private route = Router();

    private addSaveGoal: IUsecase<RequestAddSaveGoalUseCase, CreatedDto>;
    private increaseSaveGoal: IUsecase<RequestIncreaseSaveGoal, void>;
    private decreaseSaveGoal: IUsecase<RequestDecreaseSaveGoal, void>;
    private getSaveGoal: IUsecase<string, GetSaveGoalDto>;
    private getAllSaveGoal: IUsecase<void, ListDto<GetAllSaveGoalDto>>;
    private updateSaveGoal: IUsecase<RequestUpdateSaveGoalUseCase, void>;
    private deleteSaveGoal: IUsecase<RequestDeleteSaveGoal, void>;

    constructor(
        addSaveGoal: IUsecase<RequestAddSaveGoalUseCase, CreatedDto>,
        increaseSaveGoal: IUsecase<RequestIncreaseSaveGoal, void>,
        decreaseSaveGoal: IUsecase<RequestDecreaseSaveGoal, void>,
        getSaveGoal: IUsecase<string, GetSaveGoalDto>,
        getAllSaveGoal: IUsecase<void, ListDto<GetAllSaveGoalDto>>,
        updateSaveGoal: IUsecase<RequestUpdateSaveGoalUseCase, void>,
        deleteSaveGoal: IUsecase<RequestDeleteSaveGoal, void>
    ) {
        this.addSaveGoal = addSaveGoal;
        this.increaseSaveGoal = increaseSaveGoal;
        this.decreaseSaveGoal = decreaseSaveGoal;
        this.getSaveGoal = getSaveGoal;
        this.getAllSaveGoal = getAllSaveGoal;
        this.updateSaveGoal = updateSaveGoal;
        this.deleteSaveGoal = deleteSaveGoal;
    }

    setupRoutes() {
        this.route.post('/v1/save-goals', this.handleAddSaveGoalUseCase);
        this.route.put('/v1/save-goals/:id', this.handleUpdateSaveGoal);
        this.route.get('/v1/save-goals:id', this.handleGetSaveGoal);
        this.route.get('/v1/save-goals', this.handleGetAllSaveGoal);
        this.route.delete('/v1/save-goals/:id', this.handleDeleteSaveGoal);
        this.route.patch('/v1/save-goals/:id/increase-balance', this.handleIncreaseSaveGoal);
        this.route.patch('/v1/save-goals/:id/decrease-balance', this.handleDecreaseSaveGoal);
    };

    getRoute() {
        return this.route;
    };

    async handleAddSaveGoalUseCase(req: Request, res: Response) {
        var created = await this.addSaveGoal.execute({
            title: req.body.title,
            description: req.body.description,
            items: req.body.items as RequestAddItemSaveGoalUseCase[],
            target: req.body.target
        });

        res.status(200).send(created)
    }

    async handleIncreaseSaveGoal(req: Request, res: Response) {
        await this.increaseSaveGoal.execute({
            accountRef: req.body.accountId,
            increaseAmount: req.body.amount,
            savingGoalRef: req.body.saveGoalId
        });

        res.status(201);
    }

    async handleDecreaseSaveGoal(req: Request, res: Response) {
        await this.decreaseSaveGoal.execute({
            accountRef: req.body.accountId,
            decreaseAmount: req.body.amount,
            savingGoalRef: req.body.saveGoalId
        });

        res.status(201);
    }

    async handleGetSaveGoal(req: Request, res: Response) {
       var saveGoal = await this.getSaveGoal.execute(req.params.id);

       res.status(200).send(saveGoal); 
    }

    async handleGetAllSaveGoal(req: Request, res: Response) {
        var saveGoals = await this.getAllSaveGoal.execute();

        res.status(200).send(saveGoals);
    }

    async handleUpdateSaveGoal(req: Request, res: Response) {
        await this.updateSaveGoal.execute({
            savingGoalRef: req.params.id,
            description: req.body.description,
            target: req.body.target,
            title: req.body.title,
            items: req.body.items as RequestUpdateItemSaveGoalUseCase[],
        });

        res.status(201);
    }

    async handleDeleteSaveGoal(req: Request, res: Response) {
        await this.deleteSaveGoal.execute({
            saveGoalRef: req.params.id,
            accountTranfertRef: req.query?.accountTransfertId?.toString() || ''
        });
        
        res.status(201);
    }
}

