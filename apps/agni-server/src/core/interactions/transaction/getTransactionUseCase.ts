import { RecordRepository } from "@core/repositories/recordRepository";
import { TransactionRepository } from "@core/repositories/transactionRepository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export type GetTransactionDto = {
    transactionId: string
    accountId: string
    amount: number
    date: string
    status: string
    description: string
    recordType: string
    type: string
    categoryId: string
    tagRefs: string[]
    budgets: string[]
}

export class GetTransactionUseCase implements IUsecase<string, GetTransactionDto> {
    private transactionRepository: TransactionRepository;
    private recordRepository: RecordRepository; 

    constructor(
        transactionRepository: TransactionRepository,
        recordRepository: RecordRepository 
    ) {
        this.transactionRepository = transactionRepository
        this.recordRepository = recordRepository
    }

    async execute(id: string): Promise<GetTransactionDto> {
        let transaction = await this.transactionRepository.get(id);
        let record = await this.recordRepository.get(transaction.getRecordRef())
        if (record === null)
            throw new ResourceNotFoundError("RECORD_NOT_FOUND")

        let response: GetTransactionDto = {
            accountId: transaction.getAccountRef(),
            transactionId: transaction.getId(),
            amount: record.getMoney().getAmount(),
            date: transaction.getDate(),
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