import { IUsecase } from "../interfaces";
import { Transaction } from "@core/domains/entities/transaction";
import { GetUID } from "@core/adapters/libs";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { Record } from "@core/domains/entities/record";
import { RecordType, TransactionStatus, TransactionType } from "@core/domains/constants";
import { MomentDateService } from "@core/domains/entities/libs";
import Repository, { ScheduleTransactionFilter } from "@core/adapters/repository";
import { ScheduleTransaction } from "@core/domains/entities/scheduleTransaction";
import { IEventRegister } from "@core/adapters/event";
import { Scheduler } from "@core/domains/valueObjects/scheduleInfo";

export class ApplyScheduleTransactionUsecase implements IUsecase<void, void> {
    private scheduleTransactionRepo: Repository<ScheduleTransaction> 
    private recordRepo: Repository<Record>
    private transactionRepo: Repository<Transaction>
    private unitOfWork: UnitOfWorkRepository
    private eventManager: IEventRegister

    constructor(
        scheduleTransactionRepo: Repository<ScheduleTransaction>,
        transactionRepo: Repository<Transaction>,
        recordRepo: Repository<Record>,
        unitOfWork: UnitOfWorkRepository,
        eventManager: IEventRegister
    ) {
        this.scheduleTransactionRepo = scheduleTransactionRepo
        this.transactionRepo = transactionRepo
        this.recordRepo = recordRepo
        this.eventManager = eventManager
        this.unitOfWork = unitOfWork
    }

    async execute(): Promise<void> {
        try {
            // const trx = await this.unitOfWork.start() 
            const filterExtend = new ScheduleTransactionFilter()
            filterExtend.schedulerDueDate = { date: new Date(Date.now()), comparator: "<="} 

            const notifications = []
            const scheduleTransactions = await this.scheduleTransactionRepo.getAll({
                limit: 0, offset: 0,
                queryAll: true
            }, filterExtend)

            const shedules = scheduleTransactions.items.filter(i => !i.getIsPause())
            for(let i = 0; i < shedules.length; i++) {
                let scheduleTrans = shedules[i]

                const record = new Record(
                    GetUID(),
                    scheduleTrans.getAmount(),
                    scheduleTrans.getSchedule().dueDate,
                    scheduleTrans.getTransactionType() === TransactionType.INCOME ? RecordType.CREDIT : RecordType.DEBIT,
                    scheduleTrans.getName()
                )
                await this.recordRepo.create(record)

                let date = scheduleTrans.getSchedule().dueDate 

                if (scheduleTrans.getIsFreeze() && scheduleTrans.getSchedule().repeater !== undefined)
                    date = MomentDateService.getUTCDateAddition(
                        scheduleTrans.getSchedule().dueDate, 
                        scheduleTrans.getSchedule().repeater!.period,
                        scheduleTrans.getSchedule().repeater!.interval)

                const transaction = new Transaction(
                    GetUID(),
                    scheduleTrans.getAccountRef(),
                    record.getId(),
                    scheduleTrans.getCategoryRef(),
                    date,
                    scheduleTrans.getTransactionType(),
                    TransactionStatus.PENDING,
                    scheduleTrans.getTags() 
                )
                if (scheduleTrans.getIsFreeze())
                    transaction.setIsFreeze()

                if (scheduleTrans.getSchedule().repeater === undefined) {
                    await this.scheduleTransactionRepo.delete(scheduleTrans.getId())
                } else {
                    const scheduler = scheduleTrans.getSchedule()
                    const dueDate = MomentDateService.getUTCDateAddition(scheduler.dueDate, scheduler.repeater!.period, scheduler.repeater!.interval)
                    scheduleTrans.reSchedule(new Scheduler(dueDate, { period: scheduler.repeater!.period, interval: scheduler.repeater!.interval}))
                    await this.scheduleTransactionRepo.update(scheduleTrans);
                }
                await this.transactionRepo.create(transaction)

                notifications.push(`la transaction ${scheduleTrans.getName()} de ${record.getMoney().getAmount()} est en pending`)
                this.eventManager.notify('notification', {
                    title: 'Schedule Transaction',
                    content:`la transaction ${scheduleTrans.getName()} de ${record.getMoney().getAmount()} est en pending`
                })
            }
            //this.unitOfWork.commit()
            // notifications.forEach(n => {
            //     this.eventManager.notify('notification', {
            //         title: 'Schedule Transaction',
            //         content: n
            //     })
            // })
        }
        catch (err: any){
            //this.unitOfWork.rollback()
            throw err
        }   
    }
}