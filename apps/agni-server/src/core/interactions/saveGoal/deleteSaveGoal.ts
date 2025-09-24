import { RecordType, SAVING_CATEGORY_ID, TransactionStatus, TransactionType } from "@core/domains/constants";
import { Transaction } from "@core/domains/entities/transaction";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { GetUID } from "@core/adapters/libs";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { Record } from "@core/domains/entities/record";
import { MomentDateService } from "@core/domains/entities/libs";
import Repository from "@core/adapters/repository";
import { Account } from "@core/domains/entities/account";
import { SaveGoal } from "@core/domains/entities/saveGoal";

export type RequestDeleteSaveGoal = {
    id: string
    accountDepositId: string
}

export class DeleteSaveGoalUseCase implements IUsecase<RequestDeleteSaveGoal, void> {
    private transactionRepo: Repository<Transaction>
    private accountRepo: Repository<Account>
    private savingRepo: Repository<SaveGoal>
    private recordRepo: Repository<Record>
    private unitOfWork: UnitOfWorkRepository

    constructor(
        transactionRepository: Repository<Transaction>,
        accountRepository: Repository<Account>,
        savingRepository: Repository<SaveGoal>,
        recordRepository: Repository<Record>,
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

                let newRecord = new Record(GetUID(), savingGoal.getBalance(), date, RecordType.CREDIT, "Deposit from " + savingGoal.getDescription())
                let newTransaction = new Transaction(GetUID(), accountTranfert.getId(), newRecord.getId(), SAVING_CATEGORY_ID, 
                date, TransactionType.OTHER, TransactionStatus.COMPLETE, [])

                accountTranfert.addOnBalance(savingGoal.getBalance())

                await this.accountRepo.update(accountTranfert)

                await this.recordRepo.create(newRecord)

                await this.transactionRepo.create(newTransaction)
            } 
            
            await this.savingRepo.delete(savingGoal.getId())

            await this.unitOfWork.commit()

        } catch(err: any) {
            await this.unitOfWork.rollback()
            throw err
        }
    }
}
