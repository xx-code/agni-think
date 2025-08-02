import { Request, Response, Router } from "express";
import { RequestAddSaveGoalUseCase } from "@core/interactions/saveGoal/addSaveGoal";
import { RequestDecreaseSaveGoal } from "@core/interactions/saveGoal/decreaseSaveGoal";
import { RequestDeleteSaveGoal } from "@core/interactions/saveGoal/deleteSaveGoal";
import { GetAllSaveGoalDto } from "@core/interactions/saveGoal/getAllSaveGoal";
import { GetSaveGoalDto } from "@core/interactions/saveGoal/getSaveGoal";
import { RequestIncreaseSaveGoal } from "@core/interactions/saveGoal/increaseSaveGoal";
import { RequestUpdateSaveGoalUseCase } from "@core/interactions/saveGoal/updateSaveGoal";
import { ApiController } from "./base";
import { IUsecase } from "@core/interactions/interfaces";
import { CreatedDto, ListDto } from "@core/dto/base";
import { body, matchedData, query, validationResult } from "express-validator";

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
        this.route.post('/v1/save-goals', 
            body('title').notEmpty(),           
            body('description').notEmpty(),           
            body('target').notEmpty().isNumeric(),           
            body('items').isArray(),
            this.handleAddSaveGoalUseCase);

        this.route.put('/v1/save-goals/:id', 
            body('title').isEmpty(),           
            body('description').isEmpty(),           
            body('target').isEmpty().isNumeric(),           
            body('items').isArray(),
            this.handleUpdateSaveGoal);

        this.route.get('/v1/save-goals:id', 
            this.handleGetSaveGoal);

        this.route.get('/v1/save-goals', 
            this.handleGetAllSaveGoal);

        this.route.delete('/v1/save-goals/:id', 
            query('accountTransfertId').notEmpty(),
            this.handleDeleteSaveGoal);

        this.route.patch('/v1/save-goals/:id/increase-balance', 
            body('accountId').notEmpty(),
            body('amount').notEmpty(),
            body('saveGoalId').notEmpty(),
            this.handleIncreaseSaveGoal);

        this.route.patch('/v1/save-goals/:id/decrease-balance',
            body('accountId').notEmpty(),
            body('amount').notEmpty(),
            body('saveGoalId').notEmpty(),
            this.handleDecreaseSaveGoal);
    };

    getRoute() {
        return this.route;
    };

    async handleAddSaveGoalUseCase(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestAddSaveGoalUseCase = matchedData(req);
            var created = await this.addSaveGoal.execute(data);

            res.status(200).send(created);
        }
        
        res.send({ errors: result.array() });
    }

    async handleIncreaseSaveGoal(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestIncreaseSaveGoal = matchedData(req);
            await this.increaseSaveGoal.execute(data);

            res.status(201);
        }
        
        res.send({ errors: result.array() });
    }

    async handleDecreaseSaveGoal(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestDecreaseSaveGoal = matchedData(req);
            await this.decreaseSaveGoal.execute(data);

            res.status(201);
        }
        
        res.send({ errors: result.array() });
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
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestUpdateSaveGoalUseCase = matchedData(req); 
            data.id = req.params.id;
            await this.updateSaveGoal.execute(data);

            res.status(201);
        }
        
        res.send({ errors: result.array() });
    }

    async handleDeleteSaveGoal(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestDeleteSaveGoal = matchedData(req);
            data.id = req.params.id;
            await this.deleteSaveGoal.execute(data);
            
            res.status(201);
        }
        
        res.send({ errors: result.array() });
    }
}

