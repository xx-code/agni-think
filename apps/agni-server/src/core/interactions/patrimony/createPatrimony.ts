import { CreatedDto } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { Patrimony } from "@core/domains/entities/patrimony";
import { GetUID } from "@core/adapters/libs";
import { mapperPatrimonyType, TransactionStatus } from "@core/domains/constants";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { PatrimonySnapshot } from "@core/domains/entities/patrimonySnapshot";
import Repository from "@core/adapters/repository";
import { Account } from "@core/domains/entities/account";

export type RequestCreatePatrimony = {
    title: string
    amount: number
    accountIds?: string[]
    type: string
}

export class CreatePatrimonyUseCase implements IUsecase<RequestCreatePatrimony, CreatedDto> {
    private patrimonyRepo: Repository<Patrimony>
    private accountRepo: Repository<Account>
    private snapshotRepo: Repository<PatrimonySnapshot>
    private unitOfWork: UnitOfWorkRepository

    constructor(
        patrimonyRepo: Repository<Patrimony>, 
        accountRepo: Repository<Account>,
        snapshotRepo: Repository<PatrimonySnapshot>,
        unitOfWork: UnitOfWorkRepository
    ) {
        this.accountRepo = accountRepo
        this.patrimonyRepo = patrimonyRepo
        this.snapshotRepo = snapshotRepo
        this.unitOfWork = unitOfWork
    }

    async execute(request: RequestCreatePatrimony): Promise<CreatedDto> {
        try {
            const trx = await this.unitOfWork.start()

            if (await this.patrimonyRepo.existByName(request.title))
                throw new ResourceAlreadyExist("PATRIMONY_ALREADY_EXIST")

            if (request.accountIds && request.accountIds.length > 1) {
                if ((await this.accountRepo.getManyByIds(request.accountIds)).length === 0)
                    throw new ResourceNotFoundError("SOME_ACCOUNT_NOT_FOUND")
            }

            const patrimony = new Patrimony(GetUID(), request.title, request.amount, 
                mapperPatrimonyType(request.type), request.accountIds ? request.accountIds : []) 

            await this.patrimonyRepo.create(patrimony, trx)

            if (patrimony.getAmount() > 0) {
                const firstSnapshot = new PatrimonySnapshot(GetUID(), 
                    patrimony.getId(), patrimony.getAmount(), TransactionStatus.COMPLETE, 
                    new Date()
                );
                await this.snapshotRepo.create(firstSnapshot, trx);
            } 

            await this.unitOfWork.commit()

            return { newId: patrimony.getId() }
            
        } catch(err) {
            await this.unitOfWork.rollback()
            throw err
        } 
    }
}