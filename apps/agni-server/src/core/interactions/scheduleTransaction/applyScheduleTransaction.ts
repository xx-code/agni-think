import { IUsecase } from "../interfaces";
import { Transaction } from "@core/domains/entities/transaction";
import { GetUID } from "@core/adapters/libs";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { Record } from "@core/domains/entities/record";
import { RecordType, TransactionStatus, TransactionType } from "@core/domains/constants";
import { MomentDateService } from "@core/domains/entities/libs";
import Repository from "@core/adapters/repository";
import { ScheduleTransaction } from "@core/domains/entities/scheduleTransaction";
import { IEventRegister } from "@core/adapters/event";

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
            await this.unitOfWork.start()
            const scheduleTransactions = await this.scheduleTransactionRepo.getAll({
                limit: 0, offset: 0,
                queryAll: true
            })

            for(let i = 0; i < scheduleTransactions.items.length; i++) {
                let scheduleTrans = scheduleTransactions.items[i]

                if (scheduleTrans.getSchedule().isDue(true) && !scheduleTrans.getIsPause()) {
                    if (scheduleTrans.getIsPay() === false) {
                        const record = new Record(
                            GetUID(),
                            scheduleTrans.getAmount(),
                            scheduleTrans.getSchedule().getUpdatedDate(true),
                            scheduleTrans.getTransactionType() === TransactionType.INCOME ? RecordType.CREDIT : RecordType.DEBIT,
                            scheduleTrans.getName()
                        )
                        await this.recordRepo.create(record)

                        let date = scheduleTrans.getSchedule().getUpdatedDate(true) 

                        if (scheduleTrans.getIsFreeze())
                            date = MomentDateService.getUTCDateAddition(
                                scheduleTrans.getSchedule().getUpdatedDate(true), 
                                scheduleTrans.getSchedule().getPeriod(),
                                scheduleTrans.getSchedule().getPeriodTime() || 1)

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

                        scheduleTrans.setIsPay(true);
                        await this.scheduleTransactionRepo.update(scheduleTrans);
                        await this.transactionRepo.create(transaction)
                        this.eventManager.notify('notification', {
                            title: 'Schedule Transaction',
                            content:`la transaction ${scheduleTrans.getName()} de ${record.getMoney().getAmount()} est en pending`
                        })
                    } 
                } else {
                    scheduleTrans.setIsPay(false);
                    await this.scheduleTransactionRepo.update(scheduleTrans);
                }
            }

            await this.unitOfWork.commit()
        }
        catch (err: any){
            await this.unitOfWork.rollback()
            throw err
        }   
    }
}