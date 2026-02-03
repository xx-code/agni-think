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
import UnExpectedError from "@core/errors/unExpectedError";

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
            const trx = await this.unitOfWork.start()

            let savingGoal = await this.savingRepo.get(request.id)
            if (savingGoal == null)
                throw new ResourceNotFoundError("SAVING_GOAL_NOT_FOUND")

            let accountTranfert = await this.accountRepo.get(request.accountDepositId)
            if (accountTranfert == null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

            if (savingGoal.getAccountId())
                if (savingGoal.getAccountId() != request.accountDepositId)
                    throw new UnExpectedError("ACCOUNT_ID_DIFF_OF_SAVING_GOAL_ACCOUNT_ID")

            if (savingGoal.getBalance().getAmount() > 0) {
                let date = MomentDateService.getTodayWithTime()

                let newTransaction = new Transaction(GetUID(),  accountTranfert.getId(), date, TransactionType.OTHER, RecordType.CREDIT, TransactionStatus.COMPLETE)
                let newRecord = new Record(GetUID(), newTransaction.getId(), savingGoal.getBalance(), SAVING_CATEGORY_ID, "Deposit from " + savingGoal.getDescription())

                accountTranfert.addOnBalance(savingGoal.getBalance())

                await this.accountRepo.update(accountTranfert, trx)
                await this.recordRepo.create(newRecord, trx)
                await this.transactionRepo.create(newTransaction, trx)
            } 
            
            await this.savingRepo.delete(savingGoal.getId(), trx)

            await this.unitOfWork.commit()

        } catch(err: any) {
            await this.unitOfWork.rollback()
            throw err
        }
    }
}
