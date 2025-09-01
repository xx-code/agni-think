import { ScheduleTransactionRepository } from "@core/repositories/scheduleTransactionRepository";
import { IUsecase } from "../interfaces";
import { TransactionRepository } from "@core/repositories/transactionRepository";
import { Transaction } from "@core/domains/entities/transaction";
import { GetUID } from "@core/adapters/libs";
import { RecordRepository } from "@core/repositories/recordRepository";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { Record } from "@core/domains/entities/record";
import { RecordType, TransactionStatus, TransactionType } from "@core/domains/constants";
import { MomentDateService } from "@core/domains/entities/libs";

export class ApplyScheduleTransactionUsecase implements IUsecase<void, void> {
    private scheduleTransactionRepo: ScheduleTransactionRepository 
    private recordRepo: RecordRepository
    private transactionRepo: TransactionRepository
    private unitOfWork: UnitOfWorkRepository

    constructor(
        scheduleTransactionRepo: ScheduleTransactionRepository,
        transactionRepo: TransactionRepository,
        recordRepo: RecordRepository,
        unitOfWork: UnitOfWorkRepository
    ) {
        this.scheduleTransactionRepo = scheduleTransactionRepo
        this.transactionRepo = transactionRepo
        this.recordRepo = recordRepo
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
                        await this.recordRepo.save(record)

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
                        await this.transactionRepo.save(transaction)
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