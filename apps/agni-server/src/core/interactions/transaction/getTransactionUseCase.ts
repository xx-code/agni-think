import Repository, { RecordFilter } from "@core/adapters/repository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { Transaction } from "@core/domains/entities/transaction";
import { Record } from "@core/domains/entities/record";
import { DeductionType } from "@core/domains/entities/decution";
import { DeductionBase, DeductionMode } from "@core/domains/constants";

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
    private deductionRepository: Repository<DeductionType>; 

    constructor(
        transactionRepository: Repository<Transaction>,
        recordRepository: Repository<Record> ,
        deductionRepository: Repository<DeductionType>
    ) {
        this.transactionRepository = transactionRepository
        this.recordRepository = recordRepository
        this.deductionRepository = deductionRepository
    }

    async execute(id: string): Promise<GetTransactionDto> {
        let transaction = await this.transactionRepository.get(id);
        if (!transaction)
            throw new ResourceNotFoundError("TRANSACTION_NOT_FOUND")

        const extendRecordFilter = new RecordFilter()
        extendRecordFilter.transactionIds = [transaction.getId()]
        const records = await this.recordRepository.getAll({offset: 0, limit: 0, queryAll: true}, extendRecordFilter)

        const deductions = await this.deductionRepository.getManyByIds(transaction.getCollectionDeductions().map(i => i.deductionId)) 

        let subTotal = records.items.map(i => i.getMoney().getAmount()).reduce((prev, curr) => curr += prev) ?? 0

        const deductionSubTotal = deductions.filter(i => i.getBase() === DeductionBase.SUBTOTAL)
        const deductionTotal = deductions.filter(i => i.getBase() === DeductionBase.TOTAL)

        deductionSubTotal?.forEach(i => {
            const deduc = transaction.getCollectionDeductions().find(trans => trans.deductionId === i.getId())
            if (deduc) 
                subTotal = i.getMode() === DeductionMode.FLAT ? subTotal + deduc.amount : subTotal + (subTotal * (deduc.amount/100) )
        })

        let total = subTotal
        deductionTotal?.forEach(i => {
            const deduc = transaction.getCollectionDeductions().find(trans => trans.deductionId === i.getId())
            if (deduc) 
                total = i.getMode() === DeductionMode.FLAT ? total + deduc.amount : total + (total * (deduc.amount/100) )
        })

        let response: GetTransactionDto = {
            id: transaction.getId(), 
            accountId: transaction.getAccountRef(),
            date: transaction.getDate(),
            status: transaction.getStatus(),
            type: transaction.getTransactionType(),
            subTotalAmount: subTotal,
            totalAmount: total,
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
            deductions:  transaction.getCollectionDeductions().map(i => ({
                id: i.deductionId,
                amount: i.amount
            }))
        }

        return response
        
    }
}