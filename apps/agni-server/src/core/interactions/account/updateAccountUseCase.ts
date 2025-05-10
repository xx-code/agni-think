import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { AccountRepository } from "../../repositories/accountRepository";
import { mapperTypeAccount } from "@core/domains/constants";


export type RequestUpdateAccountUseCase = {
    id: string
    title: string
    type: string
}

export interface IUpdateAccountUseCase {
    execute(request: RequestUpdateAccountUseCase): void
}

export interface IUpdateAccountUseCaseResponse {
    success(accountUpdated: boolean): void
    fail(err: Error): void
}

export class UpdateAccountUseCase implements IUpdateAccountUseCase {
    private repository: AccountRepository;
    private presenter: IUpdateAccountUseCaseResponse;
    
    constructor(repo: AccountRepository, presenter: IUpdateAccountUseCaseResponse) {
        this.repository = repo;
        this.presenter = presenter;
    }
    
    async execute(request: RequestUpdateAccountUseCase): Promise<void> {
        try {
            let fetchedAccount = await this.repository.get(request.id)

            if ((await this.repository.isExistByName(request.title)) && fetchedAccount.getTitle() !== request.title)
                throw new ResourceAlreadyExist('Account name already exist')
            
            fetchedAccount.setTitle(request.title)
            fetchedAccount.setType(mapperTypeAccount(request.type))
            if (fetchedAccount.hasChange())
                await this.repository.update(fetchedAccount)

            this.presenter.success(true)
        } catch(err) {
            this.presenter.success(false)
            this.presenter.fail(err as Error)
        }
    }
}