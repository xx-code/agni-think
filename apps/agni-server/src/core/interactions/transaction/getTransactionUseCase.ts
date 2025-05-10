import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { CategoryRepository } from "@core/repositories/categoryRepository";
import { RecordRepository } from "@core/repositories/recordRepository";
import { TagRepository } from "@core/repositories/tagRepository";
import { TransactionRepository } from "@core/repositories/transactionRepository";


export interface IGetTransactionUseCase {
    execute(id: string): void;   
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
    category: TransactionCategoryResponse
    tags: TransactionTagResponse[]
}

export interface IGetTransactionUseCaseResponse {
    success(transaction: TransactionResponse): void;
    fail(err: Error): void;
}

export interface IGetTransactionAdapter {
    transactionRepository: TransactionRepository
    categoryRepository: CategoryRepository
    tagRepository: TagRepository
    recordRepository: RecordRepository
}

export class GetTransactionUseCase implements IGetTransactionUseCase {
    private transactionRepository: TransactionRepository;
    private categoryRepository: CategoryRepository;
    private tagRepository: TagRepository;
    private recordRepository: RecordRepository; 
    private presenter: IGetTransactionUseCaseResponse;

    constructor(adapter: IGetTransactionAdapter, presenter: IGetTransactionUseCaseResponse) {
        this.transactionRepository = adapter.transactionRepository
        this.categoryRepository = adapter.categoryRepository
        this.tagRepository = adapter.tagRepository
        this.recordRepository = adapter.recordRepository
        this.presenter = presenter;
    }

    async execute(id: string): Promise<void> {
        try {
            let transaction = await this.transactionRepository.get(id);

            let record = await this.recordRepository.get(transaction.getRecordRef())

            let category = await this.categoryRepository.get(transaction.getCategoryRef())
            
            let tags: TransactionTagResponse[] = []
            for(let tagRef of transaction.getTags()) {
                let tag = await this.tagRepository.get(tagRef)
                if (tag === null)
                    throw new ResourceNotFoundError('No tag found in transaction')

                tags.push({
                    id: tag.getId(),
                    value: tag.getValue(),
                    color: tag.getColor()
                })
            }

            let response: TransactionResponse = {
                accountId: transaction.getAccountRef(),
                transactionId: transaction.getId(),
                amount: record.getMoney().getAmount(),
                date: transaction.getDate(),
                description: record.getDescription(),
                type: record.getType(),
                category: {
                    id: category.getId(),
                    title: category.getTitle(),
                    icon: category.getIconId(),
                    color: category.getColor()
                },
                tags: tags
            }

            this.presenter.success(response);
        } catch (err) {
            this.presenter.fail(err as Error);
        }
    }
}