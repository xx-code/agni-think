import { GetUID } from "@core/adapters/libs";
import { RecordType, SAVING_CATEGORY_ID, TransactionStatus, TransactionType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { Record } from "@core/domains/entities/record";
import { Transaction } from "@core/domains/entities/transaction";
import ValidationError from "@core/errors/validationError";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { AccountRepository } from "../../repositories/accountRepository";
import { RecordRepository } from "../../repositories/recordRepository";
import { SavingRepository } from "../../repositories/savingRepository";
import { TransactionRepository } from "../../repositories/transactionRepository";
import { ValueError } from "@core/errors/valueError";
import { CategoryRepository } from "@core/repositories/categoryRepository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { MomentDateService } from "@core/domains/entities/libs";


export type RequestIncreaseSaveGoal = {
    id: string;
    accountId: string;
    increaseAmount: number;
}

export class IncreaseSaveGoalUseCase implements IUsecase<RequestIncreaseSaveGoal, void> {

    private savingRepository: SavingRepository
    private accountRepository: AccountRepository
    private transactionRepository: TransactionRepository;
    private recordRepository: RecordRepository;
    private unitOfWork: UnitOfWorkRepository

    constructor(accountRepository: AccountRepository, savingRepository: SavingRepository, transactionRepository: TransactionRepository,  recordRepository: RecordRepository, unitOfWork: UnitOfWorkRepository) {
        this.accountRepository = accountRepository
        this.savingRepository = savingRepository
        this.transactionRepository = transactionRepository
        this.recordRepository = recordRepository
        this.unitOfWork = unitOfWork
    }

    async execute(request: RequestIncreaseSaveGoal): Promise<void> {
        try {
            await this.unitOfWork.start()

            let savingGoal = await this.savingRepository.get(request.id)
            if (savingGoal === null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

            let account = await this.accountRepository.get(request.accountId)
            if (account === null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

            let increaseAmount = new Money(request.increaseAmount)

            if (account.getBalance() < increaseAmount.getAmount()) {
                throw new ValidationError('Price must be smaller than account')
            }

            if (increaseAmount.add(savingGoal.getBalance()).getAmount() > savingGoal.getTarget().getAmount())  
                throw new ValueError("You cant't set more than target")


            account.substractBalance(increaseAmount)
            savingGoal.increaseBalance(increaseAmount)

            // transfert between account check transfert usecase
            let date = MomentDateService.getTodayWithTime()

            let idRecordFrom = GetUID()
            let newRecordFrom = new Record(idRecordFrom, increaseAmount, date, RecordType.DEBIT)
            newRecordFrom.setDescription('Saving ' + savingGoal.getTitle()) 
            await this.recordRepository.save(newRecordFrom)
       
            let idTransFrom = GetUID()
            let newTransactionFrom = new Transaction(idTransFrom, request.accountId, idRecordFrom, SAVING_CATEGORY_ID, date,
                TransactionType.OTHER, TransactionStatus.COMPLETE,
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