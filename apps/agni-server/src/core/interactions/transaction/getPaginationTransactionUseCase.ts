import { TransactionRepository, TransactionFilter, SortBy } from "../../repositories/transactionRepository";
import { RecordRepository } from "../../repositories/recordRepository";
import { AccountRepository } from "../../repositories/accountRepository";
import { TagRepository } from "../../repositories/tagRepository";
import { CategoryRepository } from "../../repositories/categoryRepository";
import { mapperMainTransactionCategory, mapperTransactionType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { isEmpty, DateParser } from "@core/domains/helpers";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import ValidationError from "@core/errors/validationError";


export type RequestGetPagination = {
    page: number
    limit: number
    sortBy: string
    sortSense: string
    accountFilter: Array<string>;
    categoryFilter: Array<string>;
    tagFilter: Array<string>;
    dateStart: string
    dateEnd: string
    type: string | null | undefined;
    mainCategory: string 
    minPrice: number
    maxPrice: number
}

export type TransactionCategoryResponse = {
    id: string
    title: string,
    icon: string,
    color: string|null
}

export type TransactionTagResponse = {
    id: string
    value: string
    color: string|null
}

export type TransactionResponse = {
    transactionId: string
    accountId: string
    amount: number
    date: string
    description: string
    type: string
    mainCategory: string
    category: TransactionCategoryResponse
    tags: TransactionTagResponse[]
}

export type TransactionPaginationResponse = {
    transactions: TransactionResponse[];
    currentPage: number;
    maxPages: number;
}

export interface IGetPaginationTransaction {
    execute(request: RequestGetPagination): void;
}

export interface IGetPaginationTransactionResponse {
    success(response: TransactionPaginationResponse): void;
    fail(err: Error): void;
}

export interface IGetPaginationTransactionAdapter {
    transactionRepository: TransactionRepository
    accountRepository: AccountRepository
    categoryRepository: CategoryRepository
    tagRepository: TagRepository
    recordRepository: RecordRepository
}

export class GetPaginationTransaction implements IGetPaginationTransaction {
    private transactionRepository: TransactionRepository;
    private accountRepository: AccountRepository;
    private categoryRepository: CategoryRepository;
    private tagRepository: TagRepository;
    private recordRepository: RecordRepository;
    private presenter: IGetPaginationTransactionResponse;

    constructor(adapter: IGetPaginationTransactionAdapter, presenter: IGetPaginationTransactionResponse) {
        this.transactionRepository = adapter.transactionRepository;
        this.accountRepository = adapter.accountRepository;
        this.categoryRepository = adapter.categoryRepository;
        this.tagRepository = adapter.tagRepository;
        this.recordRepository = adapter.recordRepository;
        this.presenter = presenter;
    }

    async execute(request: RequestGetPagination): Promise<void> {
        try {
            let page = 1
            if (request.page) {
                if (request.page <= 0)
                    throw new ValidationError("Page must be greater than 0")
                page = request.page
            }

            let limit = 30
            if (request.limit) {
                if (request.limit <= 0)
                    throw new ValidationError('Size must be greather than 0')
                limit = request.limit
            }
            
            if (request.accountFilter.length > 0)
                if (!(await this.accountRepository.isExistByIds(request.accountFilter)))
                    throw new ResourceNotFoundError("an account to filter not valid")
            
            if (request.categoryFilter.length > 0)
                if (!(await this.categoryRepository.isCategoryExistByIds(request.categoryFilter)))
                    throw new ResourceNotFoundError("an category to filter not valid")

            if (request.tagFilter.length > 0)
                if (!(await this.tagRepository.isTagExistByIds(request.tagFilter)))
                    throw new ResourceNotFoundError("an tag to filter not valid")

            if (!isEmpty(request.dateStart) && !isEmpty(request.dateEnd))
                if (DateParser.fromString(request.dateEnd!).compare(DateParser.fromString(request.dateStart!)) < 0)
                    throw new ValidationError('Date start must be less than date end')

            let mainCat = null
            if (!isEmpty(request.mainCategory))
                mainCat = mapperMainTransactionCategory(request.mainCategory)

            let type = null;
            if (!isEmpty(request.type))
                type = mapperTransactionType(request.type!)

            let minPrice = null;
            if (!isEmpty(request.minPrice))
                minPrice  = new Money(request.minPrice)

            let maxPrice = null;
            if (!isEmpty(request.maxPrice))
                maxPrice = new Money(request.maxPrice)
 
            let filters: TransactionFilter = {
                accounts: request.accountFilter, 
                tags: request.tagFilter,
                categories: request.categoryFilter,
                startDate: request.dateStart,
                endDate: request.dateEnd,
                type: type,
                mainCategory: mainCat,
                minPrice: minPrice,
                maxPrice: maxPrice
            };

            let sortBy: SortBy|null = null;

            request.sortBy = 'date';
            request.sortSense = 'desc'
      
            let response = await this.transactionRepository.getPaginations(page, limit, null, filters);

            let transactions: TransactionResponse[] = []
            for (let i = 0; i < response.transactions.length ; i++) {
                let transaction = response.transactions[i]
                let category = await this.categoryRepository.get(transaction.getCategoryRef())
                let record = await this.recordRepository.get(transaction.getRecordRef())

                let categoryRes: TransactionCategoryResponse = {
                    id: category.getId(),
                    title: category.getTitle(),
                    color: category.getColor(),
                    icon: category.getIconId()
                }

                let tagsRes: TransactionTagResponse[] = []
                for(let tag_ref of transaction.getTags()) {
                    let tag = await this.tagRepository.get(tag_ref)

                    tagsRes.push({
                        id: tag.getId(),
                        value: tag.getValue(),
                        color: tag.getColor()
                    })
                }

                transactions.push({
                    accountId: transaction.getAccountRef(),
                    transactionId: transaction.getId(),
                    amount: record.getMoney().getAmount(),
                    category: categoryRes,
                    date: transaction.getDate(),
                    tags: tagsRes,
                    description: record.getDescription(),
                    type: record.getType(),
                    mainCategory: transaction.getTransactionType()
                })
            }

            this.presenter.success({ transactions: transactions, currentPage: page, maxPages: response.maxPage });
        } catch (err) {
            this.presenter.fail(err as Error);
        }
    }
}