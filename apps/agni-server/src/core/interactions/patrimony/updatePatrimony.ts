import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { mapperPatrimonyType } from "@core/domains/constants";
import Repository from "@core/adapters/repository";
import { Patrimony } from "@core/domains/entities/patrimony";
import { Account } from "@core/domains/entities/account";

export type RequestUpdatePatrimony = {
    patrimonyId: string
    title?: string
    amount?: number
    accountIds?: string[]
    type?: string
}

export class UpdatePatrimonyUseCase implements IUsecase<RequestUpdatePatrimony, void> {
    private patrimonyRepo: Repository<Patrimony>
    private accountRepo: Repository<Account>

    constructor(patrimonyRepo: Repository<Patrimony>, accountRepo: Repository<Account>) {
        this.patrimonyRepo = patrimonyRepo
        this.accountRepo = accountRepo
    }

    async execute(request: RequestUpdatePatrimony): Promise<void> {
        const patrimony = await this.patrimonyRepo.get(request.patrimonyId)

        if (!patrimony)
            throw new ResourceNotFoundError("PATRIMONY_NOT_FOUND")

        if (request.title)
            patrimony.setTitle(request.title)

        if (request.amount)
            patrimony.setAmount(request.amount)

        if (request.accountIds) {
            if ((await this.accountRepo.getManyByIds(request.accountIds)).length === 0)
                throw new ResourceNotFoundError("SOME_ACCOUNT_NOT_FOUND") 
        }

        if (request.type)
            patrimony.setType(mapperPatrimonyType(request.type)) 

        if (patrimony.hasChange())
            await this.patrimonyRepo.update(patrimony)
    }
}