import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { AccountRepository } from "../../repositories/accountRepository";
import { mapperTypeAccount } from "@core/domains/constants";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export type RequestUpdateAccountUseCase = {
    id: string
    title?: string
    type?: string
}

export class UpdateAccountUseCase implements IUsecase<RequestUpdateAccountUseCase, void> {
    private repository: AccountRepository;
    
    constructor(repo: AccountRepository) {
        this.repository = repo;
    }
    
    async execute(request: RequestUpdateAccountUseCase): Promise<void> {
        let fetchedAccount = await this.repository.get(request.id)
        if (fetchedAccount == null)
            throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND") 

        if (request.title) {
            if ((await this.repository.isExistByName(request.title)) && fetchedAccount.getTitle() !== request.title)
                throw new ResourceAlreadyExist("ACCOUNT_ALREADY_EXIST")

            fetchedAccount.setTitle(request.title)
        }

        if (request.type)
            fetchedAccount.setType(mapperTypeAccount(request.type))

        if (fetchedAccount.hasChange()) {
            await this.repository.update(fetchedAccount)
        }

        return;
    }
}