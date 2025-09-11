import Repository from "@core/adapters/repository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { Transaction } from "@core/domains/entities/transaction";
import { Record } from "@core/domains/entities/record";

export type GetTransactionDto = {
    transactionId: string
    accountId: string
    amount: number
    date: Date
    status: string
    description: string
    recordType: string
    type: string
    categoryId: string
    tagRefs: string[]
    budgets: string[]
}

export class GetTransactionUseCase implements IUsecase<string, GetTransactionDto> {
    private transactionRepository: Repository<Transaction>;
    private recordRepository: Repository<Record>; 

    constructor(
        transactionRepository: Repository<Transaction>,
        recordRepository: Repository<Record> 
    ) {
        this.transactionRepository = transactionRepository
        this.recordRepository = recordRepository
    }

    async execute(id: string): Promise<GetTransactionDto> {
        let transaction = await this.transactionRepository.get(id);
        if (!transaction)
            throw new ResourceNotFoundError("TRANSACTION_NOT_FOUND")

        let record = await this.recordRepository.get(transaction.getRecordRef())
        if (record === null)
            throw new ResourceNotFoundError("RECORD_NOT_FOUND")

        let response: GetTransactionDto = {
            accountId: transaction.getAccountRef(),
            transactionId: transaction.getId(),
            amount: record.getMoney().getAmount(),
            date: transaction.getUTCDate(),
            description: record.getDescription(),
            recordType: record.getType(),
            categoryId: transaction.getCategoryRef(),
            status: transaction.getStatus(),
            type: transaction.getTransactionType(),
            tagRefs: transaction.getTags(),
            budgets: transaction.getBudgetRefs()
        }

        return response
        
    }
}