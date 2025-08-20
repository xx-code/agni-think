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
            const scheduleTransactions = await this.scheduleTransactionRepo.getAll()

            for(let i = 0; i < scheduleTransactions.length; i++) {
                let scheduleTrans = scheduleTransactions[i]

                if (scheduleTrans.getSchedule().isDue(true) && !scheduleTrans.getIsPause()) {
                    if (scheduleTrans.getIsPay() === false) {
                        const record = new Record(
                            GetUID(),
                            scheduleTrans.getAmount(),
                            scheduleTrans.getSchedule().getUpdatedDate().toISOString(),
                            scheduleTrans.getTransactionType() === TransactionType.INCOME ? RecordType.CREDIT : RecordType.DEBIT,
                            scheduleTrans.getName()
                        )
                        await this.recordRepo.save(record)

                        let date = scheduleTrans.getSchedule().getUpdatedDate().toISOString() 
                        if (scheduleTrans.getIsFreeze())
                            date = MomentDateService.getUTCDateAddition(
                                scheduleTrans.getSchedule().getUpdatedDate(), 
                                scheduleTrans.getSchedule().getPeriod(),
                                scheduleTrans.getSchedule().getPeriodTime() || 1).toISOString()

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