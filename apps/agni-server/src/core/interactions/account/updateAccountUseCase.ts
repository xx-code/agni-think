import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { AccountType, mapperContributionAcccountType, mapperManagementAccountType, mapperTypeAccount } from "@core/domains/constants";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import Repository from "@core/adapters/repository";
import { Account } from "@core/domains/entities/account";
import { CreditCardAccountDetail } from "@core/domains/valueObjects/creditCardAccount";
import UnExpectedError from "@core/errors/unExpectedError";
import { BrockageAccountDetail } from "@core/domains/valueObjects/brockageAccount";

export type RequestUpdateAccountUseCase = {
    id: string
    title?: string
    type?: string
    currencyId?: string
    creditLimit?: number
    contributionType?: string
    managementType?: string
}

export class UpdateAccountUseCase implements IUsecase<RequestUpdateAccountUseCase, void> {
    private repository: Repository<Account>;
    
    constructor(repo: Repository<Account>) {
        this.repository = repo;
    }
    
    async execute(request: RequestUpdateAccountUseCase): Promise<void> {
        let fetchedAccount = await this.repository.get(request.id)
        if (fetchedAccount == null)
            throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND") 

        if (request.title) {
            if ((await this.repository.existByName(request.title)) && fetchedAccount.getTitle() !== request.title)
                throw new ResourceAlreadyExist("ACCOUNT_ALREADY_EXIST")

            fetchedAccount.setTitle(request.title)
        }

        if (request.creditLimit && fetchedAccount.getType() === AccountType.CREDIT_CARD) {
            const detailCredit = new CreditCardAccountDetail(request.creditLimit)
            detailCredit.creditLimite = request.creditLimit
            fetchedAccount.setDetail(detailCredit)
        }

        if ((request.contributionType || request.managementType) && fetchedAccount.getType() === AccountType.BROKING) {
            const currentDetail = (fetchedAccount.getDetail() as BrockageAccountDetail)

            if (currentDetail === undefined && (!request.contributionType || !request.managementType))
                throw new UnExpectedError("ACCOUNT_DETAIL_NOT_FAIL_CORRUPTED")

            const detailBroke = new BrockageAccountDetail(currentDetail.managementType, currentDetail.contributionAccount)

            if (request.contributionType) {
                detailBroke.contributionAccount = mapperContributionAcccountType(request.contributionType)
            }
            
            if (request.managementType)
                detailBroke.managementType = mapperManagementAccountType(request.managementType)

            fetchedAccount.setDetail(detailBroke)
        }


        if (fetchedAccount.hasChange()) {
            await this.repository.update(fetchedAccount)
        }

        return;
    }
}