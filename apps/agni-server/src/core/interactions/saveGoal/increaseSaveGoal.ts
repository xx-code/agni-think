import { DateService, GetUID } from "@core/adapters/libs";
import { SAVING_CATEGORY_ID, TransactionType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { Record, TransactionType } from "@core/domains/entities/record";
import { Transaction } from "@core/domains/entities/transaction";
import ValidationError from "@core/errors/validationError";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { AccountRepository } from "../../repositories/accountRepository";
import { RecordRepository } from "../../repositories/recordRepository";
import { SavingRepository } from "../../repositories/savingRepository";
import { TransactionRepository } from "../../repositories/transactionRepository";
import { ValueError } from "@core/errors/valueError";
import { CategoryRepository } from "@core/repositories/categoryRepository";
import { Category } from "@core/domains/entities/category";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";


export type RequestIncreaseSaveGoal = {
    savingGoalRef: string;
    accountRef: string;
    increaseAmount: number;
}

export class IncreaseSaveGoalUseCase implements IUsecase<RequestIncreaseSaveGoal, void> {

    private savingRepository: SavingRepository
    private accountRepository: AccountRepository
    private categoryRepository: CategoryRepository
    private transactionRepository: TransactionRepository;
    private recordRepository: RecordRepository;
    private dateService: DateService;
    private unitOfWork: UnitOfWorkRepository

    constructor(categoryRepository: CategoryRepository, accountRepository: AccountRepository, savingRepository: SavingRepository, transactionRepository: TransactionRepository,  dateService: DateService, recordRepository: RecordRepository, unitOfWork: UnitOfWorkRepository) {
        this.accountRepository = accountRepository
        this.categoryRepository = categoryRepository
        this.savingRepository = savingRepository
        this.transactionRepository = transactionRepository
        this.dateService = dateService
        this.recordRepository = recordRepository
        this.unitOfWork = unitOfWork
    }

    async execute(request: RequestIncreaseSaveGoal): Promise<void> {
        try {
            await this.unitOfWork.start()

            let savingGoal = await this.savingRepository.get(request.savingGoalRef)
            if (savingGoal === null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

            let account = await this.accountRepository.get(request.accountRef)
            if (account === null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

            let increaseAmount = new Money(request.increaseAmount)

            if (account.getBalance() < increaseAmount.getAmount()) {
                throw new ValidationError('Price must be smaller than account')
            }

            if (increaseAmount.add(savingGoal.getBalance()).getAmount() > savingGoal.getTarget().getAmount())  
                throw new ValueError("You cant't set more than target")

            if (!(await this.categoryRepository.isCategoryExistById(SAVING_CATEGORY_ID))) {
                let new_category = new Category(SAVING_CATEGORY_ID, 'saving', 'saving')
                await this.categoryRepository.save(new_category)
            }

            account.substractBalance(increaseAmount)
            savingGoal.increaseBalance(increaseAmount)

            // transfert between account check transfert usecase
            let date = this.dateService.getTodayWithTime()

            let idRecordFrom = GetUID()
            let newRecordFrom = new Record(idRecordFrom, increaseAmount, date, TransactionType.DEBIT)
            newRecordFrom.setDescription('Saving ' + savingGoal.getTitle()) 
            await this.recordRepository.save(newRecordFrom)
       
            let idTransFrom = GetUID()
            let newTransactionFrom = new Transaction(idTransFrom, request.accountRef, idRecordFrom, SAVING_CATEGORY_ID, date,
                TransactionType.OTHER
            )
            await this.transactionRepository.save(newTransactionFrom);
            
            await this.savingRepository.update(savingGoal)

            await this.accountRepository.update(account)

            await this.unitOfWork.commit()
        } catch (err: any) {
            await this.unitOfWork.rollback()
            throw err
        }
    }
}