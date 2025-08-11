import { Router, Request, Response } from "express";
import { ApiController } from "./base";
import { IUsecase } from "@core/interactions/interfaces";
import { RequestCreateScheduleTransaction } from "@core/interactions/scheduleTransaction/createScheduleTransaction";
import { CreatedDto, ListDto } from "@core/dto/base";
import { GetScheduleTransactionDto } from "@core/interactions/scheduleTransaction/getScheduleTransaction";
import { GetAllScheduleTransactionDto } from "@core/interactions/scheduleTransaction/getAllScheduleTransaction";
import { RequestUpdateScheduleTransaction } from "@core/interactions/scheduleTransaction/updateScheduleTransaction";
import { body, matchedData, validationResult } from "express-validator";

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
        this.router.post('/v1/schedule-transactions/apply-schedule', 
            this.handleApplyScheduleTransaction);

        this.router.post('/v1/schedule-transactions', 
            body('accountId').notEmpty(),
            body('amount').notEmpty().isNumeric(),
            body('categoryId').notEmpty(),
            body('description').notEmpty(),
            body('name').notEmpty(),
            body('schedule.period').notEmpty(),
            body('schedule.periodTime').isEmpty().isNumeric(),
            body('schedule.dateStart').notEmpty().isDate(),
            body('schedule.dateEnd').isEmpty().isDate(),
            body('tagIds').isArray(),
            body('type').notEmpty(),
            this.handleCreateScheduleTransaction);

        this.router.get('/v1/schedule-transactions/:id', 
            this.handleGetScheduleTransaction);

        this.router.get('/v1/schedule-transactions', 
            this.handleGetAllScheduleTransaction);

        this.router.delete('/v1/schedule-transactions/:id', 
            this.handleDeleteScheduleTransaction);

        this.router.put('/v1/schedule-transactions/:id', 
            body('accountId').isEmpty(),
            body('amount').isEmpty().isNumeric(),
            body('categoryId').isEmpty(),
            body('description').isEmpty(),
            body('isPause').isEmpty().isBoolean(),
            body('name').isEmpty(),
            body('schedule.period').isEmpty(),
            body('schedule.periodTime').isEmpty().isNumeric(),
            body('schedule.dateStart').isEmpty().isDate(),
            body('schedule.dateEnd').isEmpty().isDate(),
            body('tagIds').isArray(),
            body('type').isEmpty(),
            this.handleUpdateScheduleTransaction);
    };
    
    getRoute(){
        return this.router;
    };

    async handleApplyScheduleTransaction(req: Request, res: Response) {
        await this.applyScheduleTransaction.execute();
        res.status(201);
    }

    async handleCreateScheduleTransaction(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestCreateScheduleTransaction = matchedData(req);
            var created = await this.createScheduleTransaction.execute(data);

            res.status(200).send(created);
        }
        res.send({ errors: result.array() }); 
    }

    async handleUpdateScheduleTransaction(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestUpdateScheduleTransaction = matchedData(req);
            data.id = req.params.id;
            await this.updateScheduleTransaction.execute(data);

            res.status(201);
        } 

        res.send({ errors: result.array() }); 
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