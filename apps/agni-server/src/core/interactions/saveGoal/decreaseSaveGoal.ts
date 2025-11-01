import { GetUID } from "@core/adapters/libs";
import { RecordType, SAVING_CATEGORY_ID, TransactionStatus, TransactionType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { Record } from "@core/domains/entities/record";
import { Transaction } from "@core/domains/entities/transaction";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { ValueError } from "@core/errors/valueError";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { MomentDateService } from "@core/domains/entities/libs";
import Repository from "@core/adapters/repository";
import { SaveGoal } from "@core/domains/entities/saveGoal";
import { Account } from "@core/domains/entities/account";


export type RequestDecreaseSaveGoal = {
    id: string;
    accountId: string;
    decreaseAmount: number;
}

export class DecreaseSaveGoalUseCase implements IUsecase<RequestDecreaseSaveGoal, void> {

    private savingRepository: Repository<SaveGoal>
    private accountRepository: Repository<Account>
    private transactionRepository: Repository<Transaction>;
    private recordRepository: Repository<Record>;
    private unitOfWork: UnitOfWorkRepository

    constructor(accountRepository: Repository<Account>, 
        savingRepository: Repository<SaveGoal>, transactionRepository: Repository<Transaction>, 
        recordRepository: Repository<Record>, unitOfWork: UnitOfWorkRepository) {
        this.accountRepository = accountRepository
        this.savingRepository = savingRepository
        this.transactionRepository = transactionRepository
        this.recordRepository = recordRepository
        this.unitOfWork = unitOfWork
    }

    async execute(request: RequestDecreaseSaveGoal): Promise<void> {
        try {
            // await this.unitOfWork.start()

            let savingGoal = await this.savingRepository.get(request.id)
            if (savingGoal === null)
                throw new ResourceNotFoundError("SAVING_GOAL_NOT_FOUND")

            let account = await this.accountRepository.get(request.accountId)
            if (account === null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND");

            let decreaseBalance = new Money(request.decreaseAmount)

            if (savingGoal.getBalance().getAmount() < decreaseBalance.getAmount()) {
                throw new ValueError('Price must be smaller than save account')
            }
            
            savingGoal.decreaseBalance(decreaseBalance)
            account.addOnBalance(decreaseBalance)

            // transfert between account check transfert usecase
            let date = MomentDateService.getTodayWithTime()

            let idRecordSaving = GetUID()
            let newRecordSaving = new Record(idRecordSaving, decreaseBalance, date, RecordType.CREDIT)
            newRecordSaving.setDescription('Saving ' + savingGoal.getTitle())
            await this.recordRepository.create(newRecordSaving)

            let idTransTo = GetUID()
            let newTransactionTo = new Transaction(idTransTo, request.accountId, idRecordSaving, 
                SAVING_CATEGORY_ID, date, TransactionType.OTHER, TransactionStatus.COMPLETE,)
            await this.transactionRepository.create(newTransactionTo)

            await this.savingRepository.update(savingGoal)

            await this.accountRepository.update(account)

            // await this.unitOfWork.commit()
        } 
        catch(err: any)
        {
            // await this.unitOfWork.rollback()
            throw err
        }
    }
}