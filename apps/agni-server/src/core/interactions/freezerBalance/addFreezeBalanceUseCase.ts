import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { AccountRepository } from "../../repositories/accountRepository";
import { CategoryRepository } from "../../repositories/categoryRepository";
import { RecordRepository } from "../../repositories/recordRepository";
import { TagRepository } from "../../repositories/tagRepository";
import { TransactionRepository } from "../../repositories/transactionRepository";
import { DateService, GetUID } from "@core/adapters/libs";
import { FREEZE_CATEGORY_ID, TransactionMainCategory } from "@core/domains/constants";
import { Category } from "@core/domains/entities/category";
import { Money } from "@core/domains/entities/money";
import { Record, TransactionType } from "@core/domains/entities/record";
import { Transaction } from "@core/domains/entities/transaction";
import ValidationError from "@core/errors/validationError";


export type RequestNewFreezeBalance = {
    accountRef: string;
    endDate: string;
    amount: number;
}

export interface IAddFreezeBalanceUseCase {
    execute(request: RequestNewFreezeBalance): void
}

export interface IAddFreezeBalancePresenter {
    success(success: boolean): void;
    fail(err: Error): void;
}


export class AddFreezeBalanceUseCase implements IAddFreezeBalanceUseCase {
    private transactionRepository: TransactionRepository;
    private recordRepository: RecordRepository;
    private categoryRepository: CategoryRepository;
    private accountRepository: AccountRepository;
    private unitOfWork: UnitOfWorkRepository;
    private dateService: DateService

    private presenter: IAddFreezeBalancePresenter;

    constructor(dateService: DateService, transactionRepo: TransactionRepository, presenter: IAddFreezeBalancePresenter, accountRepo: AccountRepository, categoryRepo: CategoryRepository, recordRepo: RecordRepository, unitOfWork: UnitOfWorkRepository) {
        this.transactionRepository = transactionRepo;
        this.recordRepository = recordRepo;
        this.dateService = dateService
        this.categoryRepository = categoryRepo;
        this.accountRepository = accountRepo;
        this.unitOfWork = unitOfWork
        this.presenter = presenter;
    }

    async execute(request: RequestNewFreezeBalance): Promise<void> {
        try {
            this.unitOfWork.start()

            let fetchedAccount = await this.accountRepository.get(request.accountRef)
            if (!fetchedAccount) {
                throw new ValidationError(`Account ${fetchedAccount} don\'t existe`);
            }

            let amount = new Money(request.amount)

            const endDate = this.dateService.formatDate(request.endDate)

            let newRecord = new Record(GetUID(), amount, endDate, TransactionType.DEBIT)
            newRecord.setDescription("Freeze")

            fetchedAccount.substractBalance(amount)          

            if (!(await this.categoryRepository.isCategoryExistById(FREEZE_CATEGORY_ID))) {
                let new_category = new Category(FREEZE_CATEGORY_ID, 'Freeze', 'freeze')
                await this.categoryRepository.save(new_category)
            }

            await this.recordRepository.save(newRecord);

            await this.accountRepository.update(fetchedAccount)

            let newTransaction = new Transaction(GetUID(), request.accountRef, newRecord.getId(), FREEZE_CATEGORY_ID, endDate, TransactionMainCategory.OTHER)
            newTransaction.setIsFreeze()
            
            await this.transactionRepository.save(newTransaction)

            this.unitOfWork.commit()
            this.presenter.success(true);
        } catch (err) {
            this.unitOfWork.rollback()
            this.presenter.fail(err as Error);
        }
    }
}