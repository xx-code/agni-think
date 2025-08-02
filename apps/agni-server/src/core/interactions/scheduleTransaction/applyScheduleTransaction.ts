import { ScheduleTransactionRepository } from "@core/repositories/scheduleTransactionRepository";
import { IUsecase } from "../interfaces";
import { TransactionRepository } from "@core/repositories/transactionRepository";
import { Transaction } from "@core/domains/entities/transaction";
import { GetUID } from "@core/adapters/libs";
import { RecordRepository } from "@core/repositories/recordRepository";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { Record } from "@core/domains/entities/record";
import { RecordType, TransactionStatus, TransactionType } from "@core/domains/constants";

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

                if (scheduleTrans.getSchedule().isDue() && !scheduleTrans.getIsPause()) {
                    const record = new Record(
                        GetUID(),
                        scheduleTrans.getAmount(),
                        scheduleTrans.getSchedule().getUpdatedDate().toLocaleString(),
                        scheduleTrans.getTransactionType() === TransactionType.INCOME ? RecordType.CREDIT : RecordType.DEBIT,
                        scheduleTrans.getName()
                    )
                    await this.recordRepo.save(record)

                    const transaction = new Transaction(
                        GetUID(),
                        scheduleTrans.getAccountRef(),
                        record.getId(),
                        scheduleTrans.getCategoryRef(),
                        scheduleTrans.getSchedule().getUpdatedDate().toLocaleString(),
                        scheduleTrans.getTransactionType(),
                        TransactionStatus.PENDING,
                        scheduleTrans.getTags() 
                    )

                    await this.transactionRepo.save(transaction)
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