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
    offset: number
    limit: number
    sortBy?: string
    sortSense?: string
    accountFilterIds?: Array<string>
    budgetFilterIds?: Array<string>
    categoryFilterIds?: Array<string>
    tagFilterIds?: Array<string>
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
    status: string
    categoryId: string
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
        let offset = 0
        if (request.offset) {
            if (request.offset < 0)
                throw new ValidationError("Page must be greater than 0")
            offset = request.offset
        }

        let limit = 25 // refactoring to be set by controller
        if (request.limit) {
            if (request.limit <= 0)
                throw new ValidationError('Size must be greather than 0')
            limit = request.limit
        }
        

        if (request.accountFilterIds !== undefined && request.accountFilterIds?.length > 0)
            if (!(await this.transactionDependencies.accountRepository?.isExistByIds(request.accountFilterIds)))
                throw new ResourceNotFoundError("an account to filter not valid")
        
        if (request.categoryFilterIds !== undefined && request.categoryFilterIds.length > 0)
            if (!(await this.transactionDependencies.categoryRepository?.isCategoryExistByIds(request.categoryFilterIds)))
                throw new ResourceNotFoundError("an category to filter not valid")

        if (request.tagFilterIds !== undefined && request.tagFilterIds.length > 0)
            if (!(await this.transactionDependencies.tagRepository?.isTagExistByIds(request.tagFilterIds)))
                throw new ResourceNotFoundError("an tag to filter not valid")
        
        if (request.budgetFilterIds !== undefined && request.budgetFilterIds.length > 0)
            if (!(await this.transactionDependencies.budgetRepository?.isBudgetExistByIds(request.budgetFilterIds)))
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
            accounts: request.accountFilterIds || [], 
            tags: request.tagFilterIds || [],
            categories: request.categoryFilterIds || [],
            startDate: request.dateStart,
            endDate: request.dateEnd,
            budgets: request.budgetFilterIds || [],
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

        let response = await this.transactionRepository.getPaginations(offset, limit, sortBy, filters);

        let transactions: GetAllTransactionDto[] = []
        for (let i = 0; i < response.items.length ; i++) {
            const transaction = response.items[i]
            const record = await this.recordRepository.get(transaction.getRecordRef())
            if (record !== null)
                transactions.push({
                    accountId: transaction.getAccountRef(),
                    transactionId: transaction.getId(),
                    amount: record.getMoney().getAmount(),
                    categoryId: transaction.getCategoryRef(),
                    date: transaction.getDate(),
                    tagRefs: transaction.getTags(),
                    description: record.getDescription(),
                    status: transaction.getStatus(),
                    recordType: record.getType(),
                    type: transaction.getTransactionType(),
                    budgets: transaction.getBudgetRefs()
                })
        }

        return { items: transactions,  totals: response.total };
    }
}