import Repository from "@core/adapters/repository";
import { ResourceNotFoundError } from "../../errors/resournceNotFoundError";
import { IUsecase } from "../interfaces";
import { Account } from "@core/domains/entities/account";


export class DeleteAccountUseCase implements IUsecase<string, void> {
    private repository: Repository<Account>;

    constructor(repo: Repository<Account>) {
        this.repository = repo;
    }

    async execute(id: string): Promise<void> {
        if (!(await this.repository.get(id)))
            throw new ResourceNotFoundError('ACCOUNT_NOT_FOUND')
            
        await this.repository.delete(id)
    }
}