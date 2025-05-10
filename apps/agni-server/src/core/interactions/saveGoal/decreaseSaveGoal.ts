import { DateService, GetUID } from "@core/adapters/libs";
import { SAVING_CATEGORY_ID } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { Record, TransactionType } from "@core/domains/entities/record";
import { Transaction } from "@core/domains/entities/transaction";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { AccountRepository } from "../../repositories/accountRepository";
import { RecordRepository } from "../../repositories/recordRepository";
import { SavingRepository } from "../../repositories/savingRepository";
import { TransactionRepository } from "../../repositories/transactionRepository";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { ValueError } from "@core/errors/valueError";
import { CategoryRepository } from "@core/repositories/categoryRepository";
import { Category } from "@core/domains/entities/category";


export type RequestDecreaseSaveGoal = {
    savingGoalRef: string;
    accountRef: string;
    decreaseAmount: number;
}

export interface IDecreaseSaveGoalUseCase {
    execute(request: RequestDecreaseSaveGoal): void
}

export interface IdecreaseSaveGoalPresenter {
    success(is_save: boolean): void;
    fail(err: Error): void;
}

export class DecreaseSaveGoalUseCase implements IDecreaseSaveGoalUseCase {

    private savingRepository: SavingRepository
    private accountRepository: AccountRepository
    private transactionRepository: TransactionRepository;
    private categoryRepository: CategoryRepository;
    private recordRepository: RecordRepository;
    private dateService: DateService;
    private unitOfWork: UnitOfWorkRepository
    private presenter: IdecreaseSaveGoalPresenter;

    constructor(presenter: IdecreaseSaveGoalPresenter, categoryRepository: CategoryRepository,  accountRepository: AccountRepository, savingRepository: SavingRepository, transactionRepository: TransactionRepository,  dateService: DateService, recordRepository: RecordRepository, unitOfWork: UnitOfWorkRepository) {
        this.presenter = presenter
        this.categoryRepository = categoryRepository
        this.accountRepository = accountRepository
        this.savingRepository = savingRepository
        this.transactionRepository = transactionRepository
        this.dateService = dateService
        this.recordRepository = recordRepository
        this.unitOfWork = unitOfWork
    }

    async execute(request: RequestDecreaseSaveGoal): Promise<void> {
        try {
            await this.unitOfWork.start()

            let savingGoal = await this.savingRepository.get(request.savingGoalRef)

            let account = await this.accountRepository.get(request.accountRef)

            let decreaseBalance = new Money(request.decreaseAmount)

            if (savingGoal.getBalance().getAmount() < decreaseBalance.getAmount()) {
                throw new ValueError('Price must be smaller than save account')
            }

            if (!(await this.categoryRepository.isCategoryExistById(SAVING_CATEGORY_ID))) {
                let new_category = new Category(SAVING_CATEGORY_ID, 'saving', 'saving')
                await this.categoryRepository.save(new_category)
            }
            
            savingGoal.decreaseBalance(decreaseBalance)
            account.addOnBalance(decreaseBalance)

            // transfert between account check transfert usecase
            let date = this.dateService.getTodayWithTime()

            let idRecordSaving = GetUID()
            let newRecordSaving = new Record(idRecordSaving, decreaseBalance, date, TransactionType.CREDIT)
            newRecordSaving.setDescription('Saving ' + savingGoal.getTitle())
            await this.recordRepository.save(newRecordSaving)

            let idTransTo = GetUID()
            let newTransactionTo = new Transaction(idTransTo, request.accountRef, idRecordSaving, SAVING_CATEGORY_ID, date)
            await this.transactionRepository.save(newTransactionTo)

            await this.savingRepository.update(savingGoal)

            await this.accountRepository.update(account)

            await this.unitOfWork.commit()
            
            this.presenter.success(true);
        } catch (err: any) {
            await this.unitOfWork.rollback()
            this.presenter.fail(err)
        }
    }
}