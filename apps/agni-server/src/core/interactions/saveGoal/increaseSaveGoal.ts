import { GetUID } from "@core/adapters/libs";
import { RecordType, SAVING_CATEGORY_ID, TransactionStatus, TransactionType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { Record } from "@core/domains/entities/record";
import { Transaction } from "@core/domains/entities/transaction";
import ValidationError from "@core/errors/validationError";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { ValueError } from "@core/errors/valueError";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { MomentDateService } from "@core/domains/entities/libs";
import Repository from "@core/adapters/repository";
import { SaveGoal } from "@core/domains/entities/saveGoal";
import { Account } from "@core/domains/entities/account";
import UnExpectedError from "@core/errors/unExpectedError";


export type RequestIncreaseSaveGoal = {
    id: string;
    accountId: string;
    increaseAmount: number;
}

export class IncreaseSaveGoalUseCase implements IUsecase<RequestIncreaseSaveGoal, void> {

    private savingRepository: Repository<SaveGoal>
    private accountRepository: Repository<Account>
    private transactionRepository: Repository<Transaction>;
    private recordRepository: Repository<Record>;
    private unitOfWork: UnitOfWorkRepository

    constructor(accountRepository: Repository<Account>, 
        savingRepository: Repository<SaveGoal>, 
        transactionRepository: Repository<Transaction>,  
        recordRepository: Repository<Record>, unitOfWork: UnitOfWorkRepository) {
        this.accountRepository = accountRepository
        this.savingRepository = savingRepository
        this.transactionRepository = transactionRepository
        this.recordRepository = recordRepository
        this.unitOfWork = unitOfWork
    }

    async execute(request: RequestIncreaseSaveGoal): Promise<void> {
        try {
            const trx = await this.unitOfWork.start()

            let savingGoal = await this.savingRepository.get(request.id)
            if (savingGoal === null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

            let account = await this.accountRepository.get(request.accountId)
            if (account === null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

            if (savingGoal.getAccountId())
                if (savingGoal.getAccountId() != request.accountId)
                    throw new UnExpectedError("ACCOUNT_ID_DIFF_OF_SAVING_GOAL_ACCOUNT_ID")

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

            let newTransactionFrom = new Transaction(GetUID(), request.accountId, date,
                TransactionType.OTHER, TransactionStatus.COMPLETE,
            )

            let newRecordFrom = new Record(GetUID(), newTransactionFrom.getId(), increaseAmount, SAVING_CATEGORY_ID, RecordType.DEBIT)
            newRecordFrom.setDescription('Saving ' + savingGoal.getTitle()) 
            await this.recordRepository.create(newRecordFrom, trx)
       
            await this.transactionRepository.create(newTransactionFrom, trx);
            await this.savingRepository.update(savingGoal, trx)
            await this.accountRepository.update(account, trx)

            await this.unitOfWork.commit()
        } catch (err: any) {
            await this.unitOfWork.rollback()
            throw err
        }
    }
}