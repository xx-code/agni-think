import { ResourceNotFoundError } from "../../errors/resournceNotFoundError";
import { AccountRepository } from "../../repositories/accountRepository";
import { IUsecase } from "../interfaces";


export class DeleteAccountUseCase implements IUsecase<string, void> {
    private repository: AccountRepository;

    constructor(repo: AccountRepository) {
        this.repository = repo;
    }

    async execute(id: string): Promise<void> {
        if (!(await this.repository.isExistById(id)))
            throw new ResourceNotFoundError('ACCOUNT_NOT_FOUND')
            
        await this.repository.delete(id)
    }
}