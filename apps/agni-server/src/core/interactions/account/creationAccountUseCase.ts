import { GetUID } from '../../../core/adapters/libs';
import { AccountRepository } from '../../repositories/accountRepository';
import { Account } from '../../../core/domains/entities/account';
import { ResourceAlreadyExist } from '../../../core/errors/resourceAlreadyExistError';
import { mapperTypeAccount } from '@core/domains/constants';
import { IUsecase } from '../interfaces';
import { CreatedDto } from '@core/dto/base';
import Repository from '@core/adapters/repository';

export type RequestCreationAccountUseCase = {
  title: string 
  type: string
}

export class CreationAccountUseCase implements IUsecase<RequestCreationAccountUseCase, CreatedDto> {
  private repository: Repository<Account>;

  constructor(repo: Repository<Account>) {
    this.repository = repo;
  }

  async execute(request: RequestCreationAccountUseCase): Promise<CreatedDto> {
    if (await this.repository.existByName(request.title)) 
      throw new ResourceAlreadyExist('ACCOUNT_ALREADY_EXIST')

    let newAccount = new Account(GetUID(), request.title, mapperTypeAccount(request.type))

    await this.repository.create(newAccount) 

    return {newId: newAccount.getId()}
  }
}
