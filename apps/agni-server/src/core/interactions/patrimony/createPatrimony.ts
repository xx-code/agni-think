import { CreatedDto } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import { PatrimonyRepository, PatrimonySnapshotRepository } from "@core/repositories/patrimonyRepository";
import { AccountRepository } from "@core/repositories/accountRepository";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { Patrimony } from "@core/domains/entities/patrimony";
import { GetUID } from "@core/adapters/libs";
import { mapperPatrimonyType, TransactionStatus } from "@core/domains/constants";
import { PatrimonyAccount } from "@core/domains/valueObjects/patrimonyAccount";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { PatrimonySnapshot } from "@core/domains/entities/patrimonySnapshot";

export type RequestCreatePatrimony = {
    title: string
    amount: number
    accountIds?: string[]
    type: string
}

export class CreatePatrimonyUseCase implements IUsecase<RequestCreatePatrimony, CreatedDto> {
    private patrimonyRepo: PatrimonyRepository
    private accountRepo: AccountRepository
    private snapshotRepo: PatrimonySnapshotRepository
    private unitOfWork: UnitOfWorkRepository

    constructor(
        patrimonyRepo: PatrimonyRepository, 
        accountRepo: AccountRepository,
        snapshotRepo: PatrimonySnapshotRepository,
        unitOfWork: UnitOfWorkRepository
    ) {
        this.accountRepo = accountRepo
        this.patrimonyRepo = patrimonyRepo
        this.snapshotRepo = snapshotRepo
        this.unitOfWork = unitOfWork
    }

    async execute(request: RequestCreatePatrimony): Promise<CreatedDto> {
        try {
            await this.unitOfWork.start()

            if (await this.patrimonyRepo.existByName(request.title))
                throw new ResourceAlreadyExist("PATRIMONY_ALREADY_EXIST")

            if (request.accountIds && request.accountIds.length > 1) {
                if (!await this.accountRepo.isExistByIds(request.accountIds))
                    throw new ResourceNotFoundError("SOME_ACCOUNT_NOT_FOUND")
            }

            const patrimony = new Patrimony(GetUID(), request.title, request.amount, 
                mapperPatrimonyType(request.type), request.accountIds ? 
                    request.accountIds.map(id => (new PatrimonyAccount(id))) : []) 

            await this.patrimonyRepo.save(patrimony)

            if (patrimony.getAmount() > 0) {
                const firstSnapshot = new PatrimonySnapshot(GetUID(), 
                    patrimony.getId(), patrimony.getAmount(), TransactionStatus.COMPLETE, 
                    new Date()
                );
                await this.snapshotRepo.save(firstSnapshot);
            } 

            await this.unitOfWork.commit()

            return { newId: patrimony.getId() }
            
        } catch(err) {
            await this.unitOfWork.rollback()
            throw err
        } 
    }
}