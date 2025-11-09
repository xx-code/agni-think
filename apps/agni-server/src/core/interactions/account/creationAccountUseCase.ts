import { GetUID } from '../../../core/adapters/libs';
import { Account } from '../../../core/domains/entities/account';
import { ResourceAlreadyExist } from '../../../core/errors/resourceAlreadyExistError';
import { AccountType, mapperContributionAcccountType, mapperManagementAccountType, mapperTypeAccount } from '@core/domains/constants';
import { IUsecase } from '../interfaces';
import { CreatedDto } from '@core/dto/base';
import Repository from '@core/adapters/repository';
import { Currency } from '@core/domains/entities/currency';
import { ResourceNotFoundError } from '@core/errors/resournceNotFoundError';
import { IAccountDetail } from '@core/domains/interface/accountDetail';
import { BrockageAccountDetail } from '@core/domains/valueObjects/brockageAccount';
import { CreditCardAccountDetail } from '@core/domains/valueObjects/creditCardAccount';
import { ValueError } from '@core/errors/valueError';

export type RequestCreationAccountUseCase = {
  title: string 
  type: string
  currencyId: string
  creditLimit?: number
  contributionType?: string
  managementType?: string
}

export class CreationAccountUseCase implements IUsecase<RequestCreationAccountUseCase, CreatedDto> {
  private repository: Repository<Account>;
  private currencyRepo: Repository<Currency>

  constructor(repo: Repository<Account>, currencyRepo: Repository<Currency>) {
    this.repository = repo;
    this.currencyRepo = currencyRepo
  }

  async execute(request: RequestCreationAccountUseCase): Promise<CreatedDto> {
    if (await this.repository.existByName(request.title)) 
      throw new ResourceAlreadyExist('ACCOUNT_ALREADY_EXIST')

    if (!await this.currencyRepo.get(request.currencyId))
      throw new ResourceNotFoundError("CURRENCY_NOT_FOUND")

    let type = mapperTypeAccount(request.type)

    let detail: IAccountDetail | undefined
    switch(type) {
      case AccountType.BROKING:
        if (!request.contributionType)
          throw new ValueError("CONTRIBUTION_TYPE_FOR_BROKING")

        if (!request.managementType)
          throw new ValueError("MANAGEMENT_TYPE_FOR_BROKING")

        let managementType = mapperManagementAccountType(request.managementType)
        let contributionType = mapperContributionAcccountType(request.contributionType)

        detail = new BrockageAccountDetail(managementType, contributionType)
        break
      case AccountType.CREDIT_CARD:
        if (!request.creditLimit)
          throw new ValueError("CREDIT_LIMIT_MUST_BE_DEFINE_FOR_CREDIT_CARD")

        detail = new CreditCardAccountDetail(request.creditLimit)
        break
      default:
        break
    }

    let newAccount = new Account(GetUID(), request.title, type, request.currencyId, detail)

    await this.repository.create(newAccount) 

    return {newId: newAccount.getId()}
  }
}
