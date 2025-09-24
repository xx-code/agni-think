import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { GetUID } from "@core/adapters/libs";
import { FREEZE_CATEGORY_ID, RecordType, TransactionStatus, TransactionType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { Record } from "@core/domains/entities/record";
import { Transaction } from "@core/domains/entities/transaction";
import { IUsecase } from "../interfaces";
import { CreatedDto } from "@core/dto/base";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import Repository from "@core/adapters/repository";
import { Account } from "@core/domains/entities/account";


export type RequestNewFreezeBalance = {
    accountId: string;
    endDate: Date;
    amount: number;
}


export class AddFreezeBalanceUseCase implements IUsecase<RequestNewFreezeBalance, CreatedDto> {
    private transactionRepository: Repository<Transaction>;
    private recordRepository: Repository<Record>;
    private accountRepository: Repository<Account>;
    private unitOfWork: UnitOfWorkRepository;

    constructor(transactionRepo: Repository<Transaction>, accountRepo: Repository<Account>, recordRepo: Repository<Record>, unitOfWork: UnitOfWorkRepository) {
        this.transactionRepository = transactionRepo;
        this.recordRepository = recordRepo;
        this.accountRepository = accountRepo;
        this.unitOfWork = unitOfWork
    }

    async execute(request: RequestNewFreezeBalance): Promise<CreatedDto> {
        try {
            await this.unitOfWork.start()

            let fetchedAccount = await this.accountRepository.get(request.accountId);
            if (!fetchedAccount) {
                throw new ResourceNotFoundError('ACCOUNT_NOT_FOUND');
            }

            let amount = new Money(request.amount)

            let newRecord = new Record(GetUID(), amount, request.endDate, RecordType.DEBIT)
            newRecord.setDescription("Freeze")

            fetchedAccount.substractBalance(amount)          

            await this.recordRepository.create(newRecord);

            await this.accountRepository.update(fetchedAccount)

            let newTransaction = new Transaction(GetUID(), request.accountId, newRecord.getId(), FREEZE_CATEGORY_ID, 
            request.endDate, TransactionType.OTHER, TransactionStatus.COMPLETE)
            newTransaction.setIsFreeze()
            
            await this.transactionRepository.create(newTransaction)

            await this.unitOfWork.commit()

            return { newId: newTransaction.getId()}
        } catch (err) {
            await this.unitOfWork.rollback()
            throw err
        }
    }
}