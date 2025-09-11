import { GetUID } from "@core/adapters/libs";
import { RecordType, TransactionStatus, TransactionType, TRANSFERT_CATEGORY_ID } from "@core/domains/constants"; import { Money } from "@core/domains/entities/money";
import { Record } from "@core/domains/entities/record";
import { Transaction } from "@core/domains/entities/transaction";
import { ValueError } from "@core/errors/valueError";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import Repository from "@core/adapters/repository";
import { Account } from "@core/domains/entities/account";


export type RequestTransfertTransactionUseCase = {
    accountIdFrom: string;
    accountIdTo: string;
    date: Date;
    amount: number;
}

export class TransfertTransactionUseCase implements IUsecase<RequestTransfertTransactionUseCase, void> {
    private transactionRepository: Repository<Transaction>
    private accountRepository: Repository<Account>
    private recordRepository: Repository<Record>
    private unitOfWork: UnitOfWorkRepository


    constructor(
        transactionRepository: Repository<Transaction>,
        accountRepository: Repository<Account>,
        recordRepository: Repository<Record>,
        unitOfWork: UnitOfWorkRepository
    ) {
        this.transactionRepository = transactionRepository
        this.accountRepository = accountRepository
        this.recordRepository = recordRepository
        this.unitOfWork = unitOfWork
    }

    async execute(request: RequestTransfertTransactionUseCase): Promise<void> {
        try {
            await this.unitOfWork.start()

            let accountFrom = await this.accountRepository.get(request.accountIdFrom);
            if (accountFrom === null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")
 
            let accountTo = await this.accountRepository.get(request.accountIdTo);
            if (accountTo === null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

            let amount = new Money(request.amount)

            if (accountFrom.getBalance() < amount.getAmount())
                throw new ValueError('AMOUNT_TRANSFERT_MUST_BE_LEAST_THAN_ACCOUNT_BALANCE')

            accountFrom.substractBalance(amount)
            accountTo.addOnBalance(amount)

            await this.accountRepository.update(accountFrom)
            await this.accountRepository.update(accountTo)

            
            let fromRecord: Record = new Record(GetUID(), amount, request.date, RecordType.DEBIT)
            fromRecord.setDescription(`Transfert du compte ${accountFrom.getTitle()}`) 

            let toRecord: Record = new Record(GetUID(), amount, request.date, RecordType.CREDIT)
            toRecord.setDescription(`Transfert au compte ${accountTo.getTitle()}`)

            await this.recordRepository.create(fromRecord);

            await this.recordRepository.create(toRecord);

            let transFrom = new Transaction(GetUID(), accountFrom.getId(), fromRecord.getId(), TRANSFERT_CATEGORY_ID, request.date, TransactionType.OTHER, TransactionStatus.COMPLETE)
            await this.transactionRepository.create(transFrom)

            let transTo = new Transaction(GetUID(), accountTo.getId(), toRecord.getId(), TRANSFERT_CATEGORY_ID, request.date, TransactionType.OTHER, TransactionStatus.COMPLETE)
            await this.transactionRepository.create(transTo);

            await this.unitOfWork.commit()
        } catch (err) {
            await this.unitOfWork.rollback()
            throw err
        }
    }
}