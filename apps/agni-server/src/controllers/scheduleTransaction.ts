import { Router, Request, Response } from "express";
import { ApiController } from "./base";
import { IUsecase } from "@core/interactions/interfaces";
import { RequestCreateScheduleTransaction, RequestCreateScheduleTransactionScheduler } from "@core/interactions/scheduleTransaction/createScheduleTransaction";
import { CreatedDto, ListDto } from "@core/dto/base";
import { GetScheduleTransactionDto } from "@core/interactions/scheduleTransaction/getScheduleTransaction";
import { GetAllScheduleTransactionDto } from "@core/interactions/scheduleTransaction/getAllScheduleTransaction";
import { RequestUpdateScheduleTransaction, RequestUpdateScheduleTransactionScheduler } from "@core/interactions/scheduleTransaction/updateScheduleTransaction";

export default class ScheduleTransationController implements ApiController {
    private router = Router();

    private applyScheduleTransaction: IUsecase<void, void>;
    private createScheduleTransaction: IUsecase<RequestCreateScheduleTransaction, CreatedDto>;
    private deleteScheduleTransaction: IUsecase<string, void>;
    private getAllScheduleTransaction: IUsecase<void, ListDto<GetAllScheduleTransactionDto>>;
    private getScheduleTransaction: IUsecase<string, GetScheduleTransactionDto>;
    private updateScheduleTransaction: IUsecase<RequestUpdateScheduleTransaction, void>;

    constructor(
        applyScheduleTransaction: IUsecase<void, void>,
        createScheduleTransaction: IUsecase<RequestCreateScheduleTransaction, CreatedDto>,
        deleteScheduleTransaction: IUsecase<string, void>,
        getAllScheduleTransaction: IUsecase<void, ListDto<GetAllScheduleTransactionDto>>,
        getScheduleTransaction: IUsecase<string, GetScheduleTransactionDto>,
        updateScheduleTransaction: IUsecase<RequestUpdateScheduleTransaction, void>
    ){
        this.applyScheduleTransaction = applyScheduleTransaction;
        this.createScheduleTransaction = createScheduleTransaction;
        this.deleteScheduleTransaction = deleteScheduleTransaction;
        this.getAllScheduleTransaction = getAllScheduleTransaction;
        this.getScheduleTransaction = getScheduleTransaction;
        this.updateScheduleTransaction = updateScheduleTransaction;
    }

    setupRoutes() {
        this.router.post('/v1/schedule-transactions/apply-schedule', this.handleApplyScheduleTransaction);
        this.router.post('/v1/schedule-transactions', this.handleCreateScheduleTransaction);
        this.router.get('/v1/schedule-transactions/:id', this.handleGetScheduleTransaction);
        this.router.get('/v1/schedule-transactions', this.handleGetAllScheduleTransaction);
        this.router.delete('/v1/schedule-transactions/:id', this.handleDeleteScheduleTransaction);
        this.router.put('/v1/schedule-transactions/:id', this.handleUpdateScheduleTransaction);
    };
    
    getRoute(){
        return this.router;
    };

    async handleApplyScheduleTransaction(req: Request, res: Response) {
        await this.applyScheduleTransaction.execute();
        res.status(201);
    }

    async handleCreateScheduleTransaction(req: Request, res: Response) {
        var created = await this.createScheduleTransaction.execute({
            accountRef: req.body.accountId,
            amount: req.body.amount,
            categoryRef: req.body.categoryId,
            description: req.body.description,
            name: req.body.name,
            schedule: req.body.schedule as RequestCreateScheduleTransactionScheduler,
            tagRefs: req.body.tags,
            type: req.body.type 
        });

        res.status(200).send(created);
    }

    async handleUpdateScheduleTransaction(req: Request, res: Response) {
        await this.updateScheduleTransaction.execute({
            id: req.params.id,
            accountRef: req.body.accountId,
            amount: req.body.amount,
            categoryRef: req.body.categoryId,
            isPause: req.body.isPause,
            tagRefs: (Array.isArray(req.query.tagFilter) ? req.query.tagFilter : [req.query.tagFilter]).filter((v): v is string => typeof v === 'string'),
            type: req.body.type,
            name: req.body.name,
            schedule: req.body.schedule as RequestUpdateScheduleTransactionScheduler,
        });

        res.status(201);
    }

    async handleDeleteScheduleTransaction(req: Request, res: Response) {
        await this.deleteScheduleTransaction.execute(req.params.id);

        res.status(201);
    }
 
    async handleGetScheduleTransaction(req: Request, res: Response) {
        var schedule = await this.getScheduleTransaction.execute(req.params.id);

        res.status(200).send(schedule);
    }

    async handleGetAllScheduleTransaction(req: Request, res: Response) {
        var schedules = await this.getAllScheduleTransaction.execute();

        res.status(200).send(schedules);
    }
}