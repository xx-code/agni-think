import { RecordRepository } from "../../repositories/recordRepository";
import { AccountRepository } from "../../repositories/accountRepository";
import { TransactionRepository } from "../../repositories/transactionRepository";
import { DateService, GetUID } from "@core/adapters/libs";
import { FREEZE_CATEGORY_ID, TRANSFERT_CATEGORY_ID } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { Record, TransactionType } from "@core/domains/entities/record";
import { Transaction } from "@core/domains/entities/transaction";
import { ValueError } from "@core/errors/valueError";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { CategoryRepository } from "@core/repositories/categoryRepository";
import { Category } from "@core/domains/entities/category";


export type RequestTransfertTransactionUseCase = {
    accountRefFrom: string;
    accountRefTo: string;
    date: string;
    amount: number;
}

interface ITransfertTransactionUseCase {
    execute(request: RequestTransfertTransactionUseCase): void
}

export interface ITransfertTransactionUseCaseResponse {
    success(isTransfert: boolean): void
    fail(err: Error): void
}

export interface ITransfertTransactionAdapter {
    transactionRepository: TransactionRepository
    recordRepository: RecordRepository
    accountRepository: AccountRepository
    categoryRepository: CategoryRepository
    dateService: DateService
    unitOfWork: UnitOfWorkRepository
}

export class TransfertTransactionUseCase implements ITransfertTransactionUseCase {
    private transactionRepository: TransactionRepository
    private recordRepository: RecordRepository
    private accountRepository: AccountRepository
    private categoryRepository: CategoryRepository
    private dateService: DateService
    private unitOfWork: UnitOfWorkRepository

    private presenter: ITransfertTransactionUseCaseResponse;

    constructor(adapter: ITransfertTransactionAdapter, presenter: ITransfertTransactionUseCaseResponse) {
        this.transactionRepository = adapter.transactionRepository
        this.recordRepository = adapter.recordRepository
        this.categoryRepository = adapter.categoryRepository
        this.dateService = adapter.dateService
        this.accountRepository = adapter.accountRepository
        this.unitOfWork = adapter.unitOfWork
        this.presenter = presenter;
    }

    async execute(request: RequestTransfertTransactionUseCase): Promise<void> {
        try {
            await this.unitOfWork.start()

            let accountFrom = await this.accountRepository.get(request.accountRefFrom);
 
            let accountTo = await this.accountRepository.get(request.accountRefTo);

            let amount = new Money(request.amount)

            if (!(await this.categoryRepository.isCategoryExistById(FREEZE_CATEGORY_ID))) {
                let new_category = new Category(FREEZE_CATEGORY_ID, 'Freeze', 'freeze')
                await this.categoryRepository.save(new_category)
            }

            if (accountFrom.getBalance() < amount.getAmount())
                throw new ValueError('Price must be less than balance from 0')

            accountFrom.substractBalance(amount)
            accountTo.addOnBalance(amount)

            await this.accountRepository.update(accountFrom)
            await this.accountRepository.update(accountTo)
            
            let fromRecord: Record = new Record(GetUID(), amount, "date moment", TransactionType.DEBIT)
            fromRecord.setDescription(`Transfert du compte ${accountFrom.getTitle()}`) 

            let toRecord: Record = new Record(GetUID(), amount, "date amout", TransactionType.CREDIT)
            toRecord.setDescription(`Transfert au compte ${accountTo.getTitle()}`)

            await this.recordRepository.save(fromRecord);

            await this.recordRepository.save(toRecord);

            let date = this.dateService.formatDateWithtime(request.date)

            let transFrom = new Transaction(GetUID(), accountFrom.getId(), fromRecord.getId(), TRANSFERT_CATEGORY_ID, date)
            await this.transactionRepository.save(transFrom)

            let transTo = new Transaction(GetUID(), accountTo.getId(), toRecord.getId(), TRANSFERT_CATEGORY_ID, date)
            await this.transactionRepository.save(transTo);

            await this.unitOfWork.commit()

            this.presenter.success(true);
        } catch (err) {
            await this.unitOfWork.rollback()

            this.presenter.fail(err as Error);
        }
    }
}