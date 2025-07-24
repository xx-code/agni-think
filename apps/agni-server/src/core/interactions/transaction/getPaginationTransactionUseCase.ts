import { TransactionRepository, TransactionFilter, SortBy } from "../../repositories/transactionRepository";
import { mapperMainTransactionCategory, mapperTransactionType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { isEmpty, DateParser } from "@core/domains/helpers";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import ValidationError from "@core/errors/validationError";
import { IUsecase } from "../interfaces";
import { ListDto } from "@core/dto/base";
import { RecordRepository } from "@core/repositories/recordRepository";
import { TransactionDependencies } from "../facades";


export type RequestGetPagination = {
    page: number
    limit: number
    sortBy?: string
    sortSense?: string
    accountFilter?: Array<string>
    budgetFilter?: Array<string>
    categoryFilter?: Array<string>
    tagFilter?: Array<string>
    dateStart?: string
    dateEnd?: string
    types?: string[]
    minPrice?: number
    maxPrice?: number
}

export type GetAllTransactionCategoryDto = {
    id: string
    title: string,
    icon: string,
    color: string|null
}

export type GetAllTransactionTagDto = {
    id: string
    value: string
    color: string|null
}

export type GetAllTransactionDto = {
    transactionId: string
    accountId: string
    amount: number
    date: string
    description: string
    recordType: string
    type: string
    categoryRef: string
    tagRefs: string[]
    budgets: string[]
}

export class GetPaginationTransaction implements IUsecase<RequestGetPagination, ListDto<GetAllTransactionDto>> {
    private transactionRepository: TransactionRepository;
    private transactionDependencies: TransactionDependencies
    private recordRepository: RecordRepository

    constructor(transactionRepository: TransactionRepository, transactionDependencies: TransactionDependencies, recordRepository: RecordRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionDependencies = transactionDependencies
        this.recordRepository = recordRepository 
    }

    async execute(request: RequestGetPagination): Promise<ListDto<GetAllTransactionDto>> {
        let page = 1
        if (request.page) {
            if (request.page <= 0)
                throw new ValidationError("Page must be greater than 0")
            page = request.page
        }

        let limit = 30 // refactoring to be set by controller
        if (request.limit) {
            if (request.limit <= 0)
                throw new ValidationError('Size must be greather than 0')
            limit = request.limit
        }
        

        if (request.accountFilter !== undefined && request.accountFilter?.length > 0)
            if (!(await this.transactionDependencies.accountRepository?.isExistByIds(request.accountFilter)))
                throw new ResourceNotFoundError("an account to filter not valid")
        
        if (request.categoryFilter !== undefined && request.categoryFilter.length > 0)
            if (!(await this.transactionDependencies.categoryRepository?.isCategoryExistByIds(request.categoryFilter)))
                throw new ResourceNotFoundError("an category to filter not valid")

        if (request.tagFilter !== undefined && request.tagFilter.length > 0)
            if (!(await this.transactionDependencies.tagRepository?.isTagExistByIds(request.tagFilter)))
                throw new ResourceNotFoundError("an tag to filter not valid")
        
        if (request.budgetFilter !== undefined && request.budgetFilter.length > 0)
            if (!(await this.transactionDependencies.budgetRepository?.isBudgetExistByIds(request.budgetFilter)))
                throw new ResourceNotFoundError("an budget to filter not valid")

        if (!isEmpty(request.dateStart) && !isEmpty(request.dateEnd))
            if (DateParser.fromString(request.dateEnd!).compare(DateParser.fromString(request.dateStart!)) < 0)
                throw new ValidationError('Date start must be less than date end')

        let types = []
        if (request.types)
        {
            for(const type of request.types) {
                types.push(mapperMainTransactionCategory(type))
            }
        }

        let minPrice;
        if (!isEmpty(request.minPrice))
            minPrice  = new Money(request.minPrice)

        let maxPrice;
        if (!isEmpty(request.maxPrice))
            maxPrice = new Money(request.maxPrice)

        let filters: TransactionFilter = {
            accounts: request.accountFilter || [], 
            tags: request.tagFilter || [],
            categories: request.categoryFilter || [],
            startDate: request.dateStart,
            endDate: request.dateEnd,
            budgets: request.budgetFilter || [],
            types: types,
            minPrice: minPrice,
            maxPrice: maxPrice
        };

        let sortBy: SortBy|null = {
            sortBy: 'date',
            asc: false
        };

        if (request.sortBy)
            sortBy.sortBy = request.sortBy

        if (request.sortSense)
            if (!['asc', 'desc'].includes(request.sortSense))
                throw new ValidationError('SORT_SENSE_MUST_BE_ASC_DESC') 

        let response = await this.transactionRepository.getPaginations(page, limit, sortBy, filters);

        let transactions: GetAllTransactionDto[] = []
        for (let i = 0; i < response.items.length ; i++) {
            const transaction = response.items[i]
            const record = await this.recordRepository.get(transaction.getRecordRef())
            if (record !== null)
                transactions.push({
                    accountId: transaction.getAccountRef(),
                    transactionId: transaction.getId(),
                    amount: record.getMoney().getAmount(),
                    categoryRef: transaction.getCategoryRef(),
                    date: transaction.getDate(),
                    tagRefs: transaction.getTags(),
                    description: record.getDescription(),
                    recordType: record.getType(),
                    type: transaction.getTransactionType(),
                    budgets: transaction.getBudgetRefs()
                })
        }

        return { items: transactions,  totals: response.total };
    }
}