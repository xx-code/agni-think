import { SAVING_CATEGORY_ID, TransactionType } from "@core/domains/constants";
import { Transaction } from "@core/domains/entities/transaction";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { AccountRepository } from "../../repositories/accountRepository";
import { SavingRepository } from "../../repositories/savingRepository";
import { TransactionRepository } from "../../repositories/transactionRepository";
import { DateService, GetUID } from "@core/adapters/libs";
import { RecordRepository } from "@core/repositories/recordRepository";
import { Record, TransactionType } from "@core/domains/entities/record";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export type RequestDeleteSaveGoal = {
    saveGoalRef: string
    accountTranfertRef: string
}


export interface IDeleteSaveGaolAdapter {
    
}

export class DeleteSaveGoalUseCase implements IUsecase<RequestDeleteSaveGoal, void> {
    private transactionRepo: TransactionRepository
    private accountRepo: AccountRepository
    private savingRepo: SavingRepository
    private recordRepo: RecordRepository
    private unitOfWork: UnitOfWorkRepository
    private dateService: DateService

    constructor(transactionRepository: TransactionRepository,
        accountRepository: AccountRepository,
        savingRepository: SavingRepository,
        dateService: DateService,
        recordRepository: RecordRepository,
        unitOfWorkRepository: UnitOfWorkRepository
    ) {
        this.transactionRepo = transactionRepository
        this.dateService = dateService
        this.recordRepo = recordRepository
        this.accountRepo = accountRepository
        this.unitOfWork = unitOfWorkRepository
        this.savingRepo = savingRepository
    }

    async execute(request: RequestDeleteSaveGoal): Promise<void> {
        try {
            await this.unitOfWork.start()

            let savingGoal = await this.savingRepo.get(request.saveGoalRef)
            if (savingGoal == null)
                throw new ResourceNotFoundError("SAVING_GOAL_NOT_FOUND")

            let accountTranfert = await this.accountRepo.get(request.accountTranfertRef)
            if (accountTranfert == null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

            let date = this.dateService.getTodayWithTime()

            let newRecord = new Record(GetUID(), savingGoal.getBalance(), date, TransactionType.CREDIT, "Deposit from " + savingGoal.getDescription())
            let newTransaction = new Transaction(GetUID(), accountTranfert.getId(), newRecord.getId(), SAVING_CATEGORY_ID, 
            date, TransactionType.OTHER, [])

            accountTranfert.addOnBalance(savingGoal.getBalance())

            await this.accountRepo.update(accountTranfert)

            await this.recordRepo.save(newRecord)

            await this.transactionRepo.save(newTransaction)
            
            await this.savingRepo.delete(savingGoal.getId())

            await this.unitOfWork.commit()

        } catch(err: any) {
            await this.unitOfWork.rollback()
            throw err
        }
    }
}
