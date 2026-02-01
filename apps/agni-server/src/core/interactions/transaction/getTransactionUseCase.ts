import Repository, { RecordFilter } from "@core/adapters/repository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { Transaction } from "@core/domains/entities/transaction";
import { Record } from "@core/domains/entities/record";

export type GetTransactionDto = {
    id: string
    accountId: string
    status: string
    type: string
    subTotalAmount: number
    totalAmount: number
    records: {
        id: string
        type: string
        description: string
        categoryId: string
        amount: number
        tagRefs: string[]
        budgetRefs: string[]
    }[]
    deductions: {
        id: string
        amount: number
    }[]
    date: Date
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

        const extendRecordFilter = new RecordFilter()
        extendRecordFilter.transactionIds = [transaction.getId()]
        const records = await this.recordRepository.getAll({offset: 0, limit: 0, queryAll: true}, extendRecordFilter)

        const subTotal = records.items.map(i => i.getMoney().getAmount()).reduce((prev, curr) => curr += prev) ?? 0

        let response: GetTransactionDto = {
            id: transaction.getId(), 
            accountId: transaction.getAccountRef(),
            date: transaction.getDate(),
            status: transaction.getStatus(),
            type: transaction.getTransactionType(),
            subTotalAmount: subTotal,
            totalAmount: subTotal,
            records: records.items.map(i => ({
                id: i.getId(), 
                amount: i.getMoney().getAmount(),
                budgets: i.getBudgetRefs(),
                tagRefs: i.getTags(),
                categoryId: i.getCategoryRef(),
                description: i.getDescription(),
                budgetRefs: i.getBudgetRefs(),
                type: i.getType()
            })) ?? [],
            deductions: []
        }

        return response
        
    }
}