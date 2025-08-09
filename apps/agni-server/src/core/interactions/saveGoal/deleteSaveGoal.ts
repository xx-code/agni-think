import { RecordType, SAVING_CATEGORY_ID, TransactionStatus, TransactionType } from "@core/domains/constants";
import { Transaction } from "@core/domains/entities/transaction";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { AccountRepository } from "../../repositories/accountRepository";
import { SavingRepository } from "../../repositories/savingRepository";
import { TransactionRepository } from "../../repositories/transactionRepository";
import { GetUID } from "@core/adapters/libs";
import { RecordRepository } from "@core/repositories/recordRepository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { Record } from "@core/domains/entities/record";
import { MomentDateService } from "@core/domains/entities/libs";

export type RequestDeleteSaveGoal = {
    id: string
    accountDepositId: string
}

export class DeleteSaveGoalUseCase implements IUsecase<RequestDeleteSaveGoal, void> {
    private transactionRepo: TransactionRepository
    private accountRepo: AccountRepository
    private savingRepo: SavingRepository
    private recordRepo: RecordRepository
    private unitOfWork: UnitOfWorkRepository

    constructor(transactionRepository: TransactionRepository,
        accountRepository: AccountRepository,
        savingRepository: SavingRepository,
        recordRepository: RecordRepository,
        unitOfWorkRepository: UnitOfWorkRepository
    ) {
        this.transactionRepo = transactionRepository
        this.recordRepo = recordRepository
        this.accountRepo = accountRepository
        this.unitOfWork = unitOfWorkRepository
        this.savingRepo = savingRepository
    }

    async execute(request: RequestDeleteSaveGoal): Promise<void> {
        try {
            await this.unitOfWork.start()

            let savingGoal = await this.savingRepo.get(request.id)
            if (savingGoal == null)
                throw new ResourceNotFoundError("SAVING_GOAL_NOT_FOUND")

            let accountTranfert = await this.accountRepo.get(request.accountDepositId)
            if (accountTranfert == null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

            if (savingGoal.getBalance().getAmount() > 0) {
                let date = MomentDateService.getTodayWithTime()

                let newRecord = new Record(GetUID(), savingGoal.getBalance(), date.toLocaleString(), RecordType.CREDIT, "Deposit from " + savingGoal.getDescription())
                let newTransaction = new Transaction(GetUID(), accountTranfert.getId(), newRecord.getId(), SAVING_CATEGORY_ID, 
                date.toLocaleString(), TransactionType.OTHER, TransactionStatus.COMPLETE, [])

                accountTranfert.addOnBalance(savingGoal.getBalance())

                await this.accountRepo.update(accountTranfert)

                await this.recordRepo.save(newRecord)

                await this.transactionRepo.save(newTransaction)
            } 
            
            await this.savingRepo.delete(savingGoal.getId())

            await this.unitOfWork.commit()

        } catch(err: any) {
            await this.unitOfWork.rollback()
            throw err
        }
    }
}
