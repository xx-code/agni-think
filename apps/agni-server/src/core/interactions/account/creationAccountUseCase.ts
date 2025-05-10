import { GetUID } from '../../../core/adapters/libs';
import { AccountRepository } from '../../repositories/accountRepository';
import { Account } from '../../../core/domains/entities/account';
import { ResourceAlreadyExist } from '../../../core/errors/resourceAlreadyExistError';
import { AccountType, mapperTypeAccount } from '@core/domains/constants';

export type RequestCreationAccountUseCase = {
  title: string 
  type: string
}

export interface ICreationAccountUseCase {
  execute(request: RequestCreationAccountUseCase): void;
}

export interface ICreationAccountUseCaseResponse {
  success(idNewAccount: string): void;
  fail(err: Error): void;
}

export class CreationAccountUseCase implements ICreationAccountUseCase {
  private repository: AccountRepository;
  private presenter: ICreationAccountUseCaseResponse;

  constructor(repo: AccountRepository, presenter: ICreationAccountUseCaseResponse) {
    this.repository = repo;
    this.presenter = presenter;
  }

  async execute(request: RequestCreationAccountUseCase): Promise<void> {
    try {
      if (await this.repository.isExistByName(request.title)) 
        throw new ResourceAlreadyExist('Account name already exist')

      let newAccount = new Account(GetUID(), request.title, mapperTypeAccount(request.type))

      await this.repository.save(newAccount)

      this.presenter.success(newAccount.getId())
    } catch (err) {
      this.presenter.fail(err as Error);
    }
  }
}
