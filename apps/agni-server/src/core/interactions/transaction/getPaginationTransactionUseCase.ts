import { mapperMainTransactionCategory, mapperTransactionStatus, TransactionStatus } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { isEmpty } from "@core/domains/helpers";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import ValidationError from "@core/errors/validationError";
import { IUsecase } from "../interfaces";
import { ListDto } from "@core/dto/base";
import { TransactionDependencies } from "../facades";
import { MomentDateService } from "@core/domains/entities/libs";
import { QueryFilterAllRepository, SortBy } from "@core/repositories/dto";
import Repository, { RecordFilter, TransactionFilter } from "@core/adapters/repository";
import { Transaction } from "@core/domains/entities/transaction";


export type RequestGetPagination = {
    offset: number
    limit: number
    sortBy?: string
    sortSense?: string
    accountFilterIds?: Array<string>
    budgetFilterIds?: Array<string>
    categoryFilterIds?: Array<string>
    tagFilterIds?: Array<string>
    dateStart?: Date
    dateEnd?: Date
    status?: string
    types?: string[]
    minPrice?: number
    maxPrice?: number
    isFreeze?: boolean
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

export class GetPaginationTransaction implements IUsecase<RequestGetPagination, ListDto<GetAllTransactionDto>> {
    private transactionRepository: Repository<Transaction>;
    private transactionDependencies: TransactionDependencies

    constructor(
        transactionRepository: Repository<Transaction>, 
        transactionDependencies: TransactionDependencies) {
        this.transactionRepository = transactionRepository;
        this.transactionDependencies = transactionDependencies
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
            if ((await this.transactionDependencies.accountRepository?.getManyByIds(request.accountFilterIds))?.length === 0)
                throw new ResourceNotFoundError("an account to filter not valid")
        
        if (request.categoryFilterIds !== undefined && request.categoryFilterIds.length > 0)
            if ((await this.transactionDependencies.categoryRepository?.getManyByIds(request.categoryFilterIds))?.length === 0)
                throw new ResourceNotFoundError("an category to filter not valid")

        if (request.tagFilterIds !== undefined && request.tagFilterIds.length > 0)
            if ((await this.transactionDependencies.tagRepository?.getManyByIds(request.tagFilterIds))?.length === 0)
                throw new ResourceNotFoundError("an tag to filter not valid")
        
        if (request.budgetFilterIds !== undefined && request.budgetFilterIds.length > 0)
            if ((await this.transactionDependencies.budgetRepository?.getManyByIds(request.budgetFilterIds))?.length === 0)
                throw new ResourceNotFoundError("an budget to filter not valid")

        if (request.dateStart && request.dateEnd)
            if (MomentDateService.compareDate(request.dateEnd, request.dateStart) < 0) {
                throw new ValidationError('Date start must be less than date end');
            }

        let types = []
        if (request.types)
        {
            for(const type of request.types) {
                types.push(mapperMainTransactionCategory(type))
            }
        }

        let status:TransactionStatus|undefined = undefined;
        if (request.status)
            status = mapperTransactionStatus(request.status) 

        let minPrice: Money|undefined = undefined
        if (!isEmpty(request.minPrice))
            minPrice  = new Money(request.minPrice)

        let maxPrice: Money|undefined = undefined
        if (!isEmpty(request.maxPrice))
            maxPrice = new Money(request.maxPrice)

        let sortBy: SortBy|undefined = {
            sortBy: 'date',
            asc: false
        };
        if (request.sortBy)
            sortBy.sortBy = request.sortBy

        if (request.sortSense)
                if (!['asc', 'desc'].includes(request.sortSense))
                    throw new ValidationError('SORT_SENSE_MUST_BE_ASC_DESC')

        let filters: QueryFilterAllRepository = {
            offset: request.offset,
            limit: request.limit,
            sort: sortBy,
            queryAll: false
        };

        const extendTransactionFilter = new TransactionFilter()
        extendTransactionFilter.accounts = request.accountFilterIds
        extendTransactionFilter.startDate = request.dateStart
        extendTransactionFilter.endDate = request.dateEnd
        extendTransactionFilter.isFreeze = request.isFreeze
        extendTransactionFilter.status = status
        extendTransactionFilter.types = types
        let transactions = await this.transactionRepository.getAll(filters, extendTransactionFilter);

        const extendRecordFilter = new RecordFilter()
        extendRecordFilter.transactionIds = transactions.items.map(i => i.getId())
        extendRecordFilter.categories = request.categoryFilterIds
        extendRecordFilter.budgets = request.budgetFilterIds
        extendRecordFilter.tags = request.tagFilterIds
        const records = await this.transactionDependencies.recordRepository?.getAll({offset: 0, limit: 0, queryAll: true}, extendRecordFilter)

        let responses: GetAllTransactionDto[] = []
        for (let i = 0; i < transactions.items.length ; i++) {
            const transaction = transactions.items[i]
            const transactionRecords = records?.items.filter(i => i.getTransactionId() === transaction.getId());

            let subTotal = 0;
            if (transactionRecords && transactionRecords?.length > 0) {
                subTotal = transactionRecords?.map(i => i.getMoney().getAmount()).reduce((prev, curr) => curr += prev) ?? 0

                responses.push({
                    id: transaction.getId(), 
                    accountId: transaction.getAccountRef(),
                    date: transaction.getDate(),
                    status: transaction.getStatus(),
                    type: transaction.getTransactionType(),
                    subTotalAmount: subTotal,
                    totalAmount: subTotal,
                    records: transactionRecords?.map(i => ({
                        id: i.getId(), 
                        amount: i.getMoney().getAmount(),
                        budgetRefs: i.getBudgetRefs(),
                        tagRefs: i.getTags(),
                        categoryId: i.getCategoryRef(),
                        description: i.getDescription(),
                        type: i.getType()
                    })) ?? [],
                    deductions: []
                })
            } 
        }

        return { items: responses,  totals: transactions.total};
    }
}