import { SAVING_CATEGORY_ID, TransactionMainCategory } from "@core/domains/constants";
import { Transaction } from "@core/domains/entities/transaction";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { AccountRepository } from "../../repositories/accountRepository";
import { SavingRepository } from "../../repositories/savingRepository";
import { TransactionRepository } from "../../repositories/transactionRepository";
import { DateService, GetUID } from "@core/adapters/libs";
import { RecordRepository } from "@core/repositories/recordRepository";
import { Record, TransactionType } from "@core/domains/entities/record";

export type RequestDeleteSaveGoal = {
    saveGoalRef: string
    accountTranfertRef: string
}

export interface IDeleteSaveGoalUseCase {
    execute(request: RequestDeleteSaveGoal): void
}

export interface IDeleteSaveGoalPresenter {
    success(isDelete: boolean): void;
    fail(err: Error): void;
}

export interface IDeleteSaveGaolAdapter {
    transactionRepository: TransactionRepository
    accountRepository: AccountRepository
    savingRepository: SavingRepository
    dateService: DateService
    recordRepository: RecordRepository
    unitOfWorkRepository: UnitOfWorkRepository
}

export class DeleteSaveGoalUseCase implements IDeleteSaveGoalUseCase {
    private transactionRepo: TransactionRepository
    private accountRepo: AccountRepository
    private savingRepo: SavingRepository
    private recordRepo: RecordRepository
    private unitOfWork: UnitOfWorkRepository
    private dateService: DateService
    private presenter: IDeleteSaveGoalPresenter

    constructor(adapter: IDeleteSaveGaolAdapter, presenter: IDeleteSaveGoalPresenter) {
        this.presenter = presenter
        this.transactionRepo = adapter.transactionRepository
        this.dateService = adapter.dateService
        this.recordRepo = adapter.recordRepository
        this.accountRepo = adapter.accountRepository
        this.unitOfWork = adapter.unitOfWorkRepository
        this.savingRepo = adapter.savingRepository
    }

    async execute(request: RequestDeleteSaveGoal): Promise<void> {
        try {
            await this.unitOfWork.start()

            let savingGoal = await this.savingRepo.get(request.saveGoalRef)

            let accountTranfert = await this.accountRepo.get(request.accountTranfertRef)

            let date = this.dateService.getTodayWithTime()

            let newRecord = new Record(GetUID(), savingGoal.getBalance(), date, TransactionType.CREDIT, "Deposit from " + savingGoal.getDescription())
            let newTransaction = new Transaction(GetUID(), accountTranfert.getId(), newRecord.getId(), SAVING_CATEGORY_ID, 
            date, TransactionMainCategory.OTHER, [])

            accountTranfert.addOnBalance(savingGoal.getBalance())

            await this.accountRepo.update(accountTranfert)

            await this.recordRepo.save(newRecord)

            await this.transactionRepo.save(newTransaction)
            
            await this.savingRepo.delete(savingGoal.getId())

            await this.unitOfWork.commit()

            this.presenter.success(true)
        } catch(err: any) {
            await this.unitOfWork.rollback()
            this.presenter.fail(err)
        }
    }
}
