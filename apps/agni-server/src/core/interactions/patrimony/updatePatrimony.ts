import { PatrimonyRepository } from "@core/repositories/patrimonyRepository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { AccountRepository } from "@core/repositories/accountRepository";
import { mapperPatrimonyType } from "@core/domains/constants";

export type RequestUpdatePatrimony = {
    patrimonyId: string
    title?: string
    accountIds?: string[]
    type?: string
}

export class UpdatePatrimonyUseCase implements IUsecase<RequestUpdatePatrimony, void> {
    private patrimonyRepo: PatrimonyRepository
    private accountRepo: AccountRepository

    constructor(patrimonyRepo: PatrimonyRepository, accountRepo: AccountRepository) {
        this.patrimonyRepo = patrimonyRepo
        this.accountRepo = accountRepo
    }

    async execute(request: RequestUpdatePatrimony): Promise<void> {
        const patrimony = await this.patrimonyRepo.get(request.patrimonyId)

        if (!patrimony)
            throw new ResourceNotFoundError("PATRIMONY_NOT_FOUND")

        if (request.title)
            patrimony.setTitle(request.title)

        if (request.accountIds) {
            if (!await this.accountRepo.isExistByIds(request.accountIds))
                throw new ResourceNotFoundError("SOME_ACCOUNT_NOT_FOUND") 
        }

        if (request.type)
            patrimony.setType(mapperPatrimonyType(request.type))

        if (patrimony.hasChange())
            await this.patrimonyRepo.update(patrimony)
    }
}