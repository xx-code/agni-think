import { CreatedDto } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import { PatrimonyRepository } from "@core/repositories/patrimonyRepository";
import { AccountRepository } from "@core/repositories/accountRepository";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { Patrimony } from "@core/domains/entities/patrimony";
import { GetUID } from "@core/adapters/libs";
import { mapperPatrimonyType } from "@core/domains/constants";
import { PatrimonyAccount } from "@core/domains/valueObjects/patrimonyAccount";

export type RequestCreatePatrimony = {
    title: string
    accountIds?: string[]
    type: string
}

export class CreatePatrimonyUseCase implements IUsecase<RequestCreatePatrimony, CreatedDto> {
    private patrimonyRepo: PatrimonyRepository
    private accountRepo: AccountRepository

    constructor(patrimonyRepo: PatrimonyRepository, accountRepo: AccountRepository) {
        this.accountRepo = accountRepo
        this.patrimonyRepo = patrimonyRepo
    }

    async execute(request: RequestCreatePatrimony): Promise<CreatedDto> {
        if (await this.patrimonyRepo.existByName(request.title))
            throw new ResourceAlreadyExist("PATRIMONY_ALREADY_EXIST")

        if (request.accountIds && request.accountIds.length > 1) {
            if (!await this.accountRepo.isExistByIds(request.accountIds))
                throw new ResourceNotFoundError("SOME_ACCOUNT_NOT_FOUND")
        }

        const patrimony = new Patrimony(GetUID(), request.title, 
            mapperPatrimonyType(request.type), request.accountIds ? 
                request.accountIds.map(id => (new PatrimonyAccount(id))) : []) 

        this.patrimonyRepo.save(patrimony)

        return { newId: patrimony.getId() }
    }
}