import { SAVING_CATEGORY_ID } from "@core/domains/constants";
import { TransactionType } from "@core/domains/entities/record";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { ValueError } from "@core/errors/valueError";
import { AccountRepository } from "@core/repositories/accountRepository";
import { RecordRepository } from "@core/repositories/recordRepository";
import { TransactionRepository } from "@core/repositories/transactionRepository";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";


export interface IDeleteTransactionUseCase {
    execute(id: string): void;
}

export interface IDeleteTransactoinUseCaseResponse {
    success(isDeleted: boolean): void;
    fail(err: Error): void
}

export class DeleteTransactionUseCase implements IDeleteTransactionUseCase {
    private transRepository: TransactionRepository;
    private presenter: IDeleteTransactoinUseCaseResponse;
    private accountRepo: AccountRepository;
    private recordRepo: RecordRepository;
    private unitOfWork: UnitOfWorkRepository

    constructor(repo: TransactionRepository, recordRepo: RecordRepository, unitOfWork: UnitOfWorkRepository, accountRepo: AccountRepository, presenter: IDeleteTransactoinUseCaseResponse) {
        this.transRepository = repo
        this.accountRepo = accountRepo
        this.presenter = presenter
        this.recordRepo = recordRepo
        this.unitOfWork = unitOfWork
    }
    
    async execute(id: string): Promise<void> {
        try {   
            await this.unitOfWork.start()

            if (!(await this.transRepository.isTransactionExistById(id)))
                throw new ResourceNotFoundError('Transaction not found')

            let transaction = await this.transRepository.get(id);

            if ([SAVING_CATEGORY_ID].includes(transaction.getCategoryRef()))
                throw new ValueError("You cannot update this type of transaction")

            let record = await this.recordRepo.get(transaction.getRecordRef())

            let account = await this.accountRepo.get(transaction.getAccountRef())
            
            record.getType() === TransactionType.CREDIT ? account.substractBalance(record.getMoney()) : account.addOnBalance(record.getMoney())
            console.log(account.getBalance())

            await this.accountRepo.update(account)
            
            await this.recordRepo.delete(record.getId())

            await this.transRepository.delete(id);
            
            await this.unitOfWork.commit()

            this.presenter.success(true);
        } catch(err) {
            await this.unitOfWork.rollback()

            this.presenter.fail(err as Error);
        }
    }
}