import { 
    Account, 
    ContributionAccount, 
    ManagementAccount, 
    mapperContributionAcccountType, 
    mapperManagementAccount } from "../constants";
import { IAccountDetail } from "../interface/accountDetail";
import ValueObject from "./valueObject";

export class BrockageAccountDetail extends ValueObject implements IAccountDetail  {
    public managementType: ManagementAccount
    public contributionAccount: ContributionAccount

    constructor(managementType: ManagementAccount, contributionAccount: ContributionAccount) {
        super()
        this.managementType = managementType
        this.contributionAccount = contributionAccount
    }

    isEqual(object: BrockageAccountDetail): boolean {
        return this.managementType == object.managementType && this.contributionAccount == object.contributionAccount
    }

    toJson(): string {
        return JSON.stringify({
            management_type: this.managementType, 
            contribution_account: this.contributionAccount
        })
    }

    getType(): Account {
        return Account.BROKING
    }

    getJson() {
        return this.toJson()
    }


   static fromJson(value: any): BrockageAccountDetail { 
        try {
            const object: {
                management_type: string
                contribution_account: string
            } = value;

            return new BrockageAccountDetail(mapperManagementAccount(object.management_type), mapperContributionAcccountType(object.contribution_account)) 
        } catch(err) {
            throw err
        }
   }
}