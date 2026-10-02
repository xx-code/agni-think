package domain.value_objects

import domain.interfaces.IAccountDetail

data class BrokingAccountDetail(val managementType: domain.enums.ManagementAccountType, val contributionType: domain.enums.ContributionAccountType):
    IAccountDetail {
    override fun getType(): domain.enums.AccountType {
       return _root_ide_package_.domain.enums.AccountType.BROKING
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
                return BrokingAccountDetail(_root_ide_package_.domain.enums.ManagementAccountType.MANAGED, _root_ide_package_.domain.enums.ContributionAccountType.UNREGISTERED)

            if (!map.containsKey("management_type") || !map.containsKey("contribution_account"))
                return BrokingAccountDetail(_root_ide_package_.domain.enums.ManagementAccountType.MANAGED, _root_ide_package_.domain.enums.ContributionAccountType.UNREGISTERED)

            return BrokingAccountDetail(
                _root_ide_package_.domain.enums.ManagementAccountType.fromString(map.getValue("management_type") as String),
                _root_ide_package_.domain.enums.ContributionAccountType.fromString(map.getValue("contribution_account") as String),
            )
        }
    }
}
