import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { AccountRepository } from "../../repositories/accountRepository";
import { CategoryRepository } from "../../repositories/categoryRepository";
import { RecordRepository } from "../../repositories/recordRepository";
import { TransactionRepository } from "../../repositories/transactionRepository";
import { GetUID } from "@core/adapters/libs";
import { FREEZE_CATEGORY_ID, RecordType, TransactionStatus, TransactionType } from "@core/domains/constants";
import { Category } from "@core/domains/entities/category";
import { Money } from "@core/domains/entities/money";
import { Record } from "@core/domains/entities/record";
import { Transaction } from "@core/domains/entities/transaction";
import { IUsecase } from "../interfaces";
import { CreatedDto } from "@core/dto/base";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { MomentDateService } from "@core/domains/entities/libs";


export type RequestNewFreezeBalance = {
    accountRef: string;
    endDate: string;
    amount: number;
}


export class AddFreezeBalanceUseCase implements IUsecase<RequestNewFreezeBalance, CreatedDto> {
    private transactionRepository: TransactionRepository;
    private recordRepository: RecordRepository;
    private accountRepository: AccountRepository;
    private unitOfWork: UnitOfWorkRepository;

    constructor(transactionRepo: TransactionRepository, accountRepo: AccountRepository, recordRepo: RecordRepository, unitOfWork: UnitOfWorkRepository) {
        this.transactionRepository = transactionRepo;
        this.recordRepository = recordRepo;
        this.accountRepository = accountRepo;
        this.unitOfWork = unitOfWork
    }

    async execute(request: RequestNewFreezeBalance): Promise<CreatedDto> {
        try {
            this.unitOfWork.start()

            let fetchedAccount = await this.accountRepository.get(request.accountRef)
            if (!fetchedAccount) {
                throw new ResourceNotFoundError('ACCOUNT_NOT_FOUND');
            }

            let amount = new Money(request.amount)
            const endDate = MomentDateService.formatDate(request.endDate)

            let newRecord = new Record(GetUID(), amount, endDate.toString(), RecordType.DEBIT)
            newRecord.setDescription("Freeze")

            fetchedAccount.substractBalance(amount)          

            await this.recordRepository.save(newRecord);

            await this.accountRepository.update(fetchedAccount)

            let newTransaction = new Transaction(GetUID(), request.accountRef, newRecord.getId(), FREEZE_CATEGORY_ID, 
            endDate.toString(), TransactionType.OTHER, TransactionStatus.COMPLETE)
            newTransaction.setIsFreeze()
            
            await this.transactionRepository.save(newTransaction)

            this.unitOfWork.commit()

            return { newId: newTransaction.getId()}
        } catch (err) {
            this.unitOfWork.rollback()
            throw err
        }
    }
}