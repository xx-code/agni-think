import { GetUID } from '../../../core/adapters/libs';
import { AccountRepository } from '../../repositories/accountRepository';
import { Account } from '../../../core/domains/entities/account';
import { ResourceAlreadyExist } from '../../../core/errors/resourceAlreadyExistError';
import { AccountType, mapperTypeAccount } from '@core/domains/constants';
import { IUsecase } from '../interfaces';
import { CreatedDto } from '@core/dto/base';

export type RequestCreationAccountUseCase = {
  title: string 
  type: string
}

export class CreationAccountUseCase implements IUsecase<RequestCreationAccountUseCase, CreatedDto> {
  private repository: AccountRepository;

  constructor(repo: AccountRepository) {
    this.repository = repo;
  }

  async execute(request: RequestCreationAccountUseCase): Promise<CreatedDto> {
    if (await this.repository.isExistByName(request.title)) 
      throw new ResourceAlreadyExist('ACCOUNT_ALREADY_EXIST')

    let newAccount = new Account(GetUID(), request.title, mapperTypeAccount(request.type))

    await this.repository.save(newAccount) 

    return {newId: newAccount.getId()}
  }
}
