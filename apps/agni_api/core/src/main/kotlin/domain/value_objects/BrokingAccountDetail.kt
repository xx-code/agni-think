package domain.value_objects

import domain.interfaces.IAccountDetail
import domain.enums.AccountType
import domain.enums.ContributionAccountType
import domain.enums.ManagementAccountType

data class BrokingAccountDetail(val managementType: ManagementAccountType, val contributionType: ContributionAccountType):
    IAccountDetail {
    override fun getType(): AccountType {
       return AccountType.BROKING
    }

    override fun toMap(): Map<String, Any> {
        return mapOf(
            "management_type" to managementType.value,
            "contribution_account" to contributionType.value
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): IAccountDetail {
            if (map == null)
                return BrokingAccountDetail(ManagementAccountType.MANAGED, ContributionAccountType.UNREGISTERED)

            if (!map.containsKey("management_type") || !map.containsKey("contribution_account"))
                return BrokingAccountDetail(ManagementAccountType.MANAGED, ContributionAccountType.UNREGISTERED)

            return BrokingAccountDetail(
                ManagementAccountType.fromString(map.getValue("management_type") as String),
                ContributionAccountType.fromString(map.getValue("contribution_account") as String),
            )
        }
    }
}
