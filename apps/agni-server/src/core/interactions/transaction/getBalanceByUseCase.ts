import { DeductionBase, DeductionMode, mapperMainTransactionCategory, RecordType, TransactionStatus, TransactionType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import ValidationError from "@core/errors/validationError";
import { IUsecase } from "../interfaces";
import { MomentDateService } from "@core/domains/entities/libs";
import Repository, { RecordFilter, TransactionFilter } from "@core/adapters/repository";
import { Transaction } from "@core/domains/entities/transaction";
import { Record } from "@core/domains/entities/record";
import { RequestGetPagination } from "./getPaginationTransactionUseCase";
import {  DeductionType } from "@core/domains/entities/decution";

export type GetBalanceDto = {
    balance: number
    income: number
    spend: number
}


export class GetBalanceByUseCase implements IUsecase<RequestGetPagination, GetBalanceDto> {
    private transactionRepository: Repository<Transaction>;
    private recordRepository: Repository<Record>
    private deductionRepository: Repository<DeductionType>

    constructor(
        transaction_repo: Repository<Transaction>, 
        recordRepository: Repository<Record>,
        deductionRepository: Repository<DeductionType>
    ) {
        this.transactionRepository = transaction_repo;
        this.recordRepository = recordRepository
        this.deductionRepository = deductionRepository
    }

    async execute(request: RequestGetPagination): Promise<GetBalanceDto> {
        try {
            if (request.dateStart && request.dateEnd) {
                // compare date
                if (MomentDateService.compareDate(request.dateEnd, request.dateStart) < 0) {
                    throw new ValidationError('Date start must be less than date end');
                }
            }

            let types = []
            if (request.types)
            {
                for(const type of request.types) {
                    types.push(mapperMainTransactionCategory(type))
                }
            }

            let minPrice;
            if (request.minPrice)
                minPrice  = new Money(request.minPrice)

            let maxPrice;
            if (request.maxPrice)
                maxPrice = new Money(request.maxPrice)

            let dateStart;
            if (request.dateStart)
                dateStart = request.dateStart

            let dateEnd;
            if (request.dateEnd)
                dateEnd = request.dateEnd


            let filter = {
                offset: 0,
                limit: 0,
                queryAll: true,
            }


            const extendTransactionFilter = new TransactionFilter()
            extendTransactionFilter.accounts = request.accountFilterIds 
            extendTransactionFilter.startDate = request.dateStart
            extendTransactionFilter.endDate = request.dateEnd
            extendTransactionFilter.isFreeze = false
            extendTransactionFilter.types = types
            extendTransactionFilter.status = TransactionStatus.COMPLETE

            const transactions = await this.transactionRepository.getAll(filter, extendTransactionFilter);

            const extendRecordFilter = new RecordFilter()
            extendRecordFilter.transactionIds = transactions.items.map(i => i.getId())
            extendRecordFilter.categories = request.categoryFilterIds
            extendRecordFilter.budgets = request.budgetFilterIds
            extendRecordFilter.tags = request.tagFilterIds
            const records = await this.recordRepository.getAll(filter, extendRecordFilter)

            const deductionIds = [  ...new Set(
                transactions.items
                .flatMap(item => item.getCollectionDeductions())
                .map(d => d.deductionId))
            ]

            const deductions = await this.deductionRepository.getManyByIds(deductionIds) 
            
            let income = 0
            let spend = 0

            for(const transaction of transactions.items) {
                let subTotal = 0
                const transRecords = records.items.filter(i => i.getTransactionId() === transaction.getId())
                if (transRecords.length > 0)  {
                    const recordCredit = transRecords.filter(i => i.getType() === RecordType.CREDIT).map(i => i.getMoney().getAmount())
                    const recordDebit = transRecords.filter(i => i.getType() === RecordType.DEBIT).map(i => i.getMoney().getAmount())

                    if (recordCredit.length > 0)
                        subTotal += recordCredit.reduce((prev, curr) =>  curr += prev) ?? 0

                    if (recordDebit.length > 0)
                        subTotal -= recordDebit.reduce((prev, curr) =>  curr += prev) ?? 0
                    
                }

                const transDeductions = deductions.filter(i => transaction.getCollectionDeductions().map(i => i.deductionId).includes(i.getId()))

                const deductionSubTotal = transDeductions.filter(i => i.getBase() === DeductionBase.SUBTOTAL)
                const deductionTotal = transDeductions.filter(i => i.getBase() === DeductionBase.TOTAL)

                deductionSubTotal.forEach(i => {
                    const deduc = transaction.getCollectionDeductions().find(trans => trans.deductionId === i.getId())
                    if (deduc) 
                        subTotal = i.getMode() === DeductionMode.FLAT ? subTotal + deduc.amount : subTotal + (subTotal * (deduc.amount/100) )
                })

                deductionTotal.forEach(i => {
                    const deduc = transaction.getCollectionDeductions().find(trans => trans.deductionId === i.getId())
                    if (deduc) 
                        subTotal = i.getMode() === DeductionMode.FLAT ? subTotal + deduc.amount : subTotal + (subTotal * (deduc.amount/100) )
                })

                if (transaction.getTransactionType() === TransactionType.INCOME)
                    income +=  subTotal
                if (transaction.getTransactionType() === TransactionType.OTHER) {
                    if (subTotal > 0)
                        income += subTotal
                    else 
                        spend += Math.abs(subTotal) 
                } else 
                    spend += Math.abs(subTotal)

            }


            const balance = income - spend 

            return {
                balance: Number(balance.toFixed(2)),
                income: Number(income.toFixed(2)),
                spend: Math.abs(Number(spend.toFixed(2)))  
            }
        } catch(err) {
            throw err
        } 
    }
}