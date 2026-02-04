package dev.auguste.agni_api.core.value_objects

import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.enums.ContributionAccountType
import dev.auguste.agni_api.core.entities.enums.ManagementAccountType
import dev.auguste.agni_api.core.entities.interfaces.IAccountDetail

data class BrokingAccountDetail(val managementType: ManagementAccountType, val contributionType: ContributionAccountType):
    IAccountDetail {
    override fun getType(): AccountType {
       return AccountType.BROKING
    }

    override fun getDetails(): Map<String, Any> {
        return mapOf(
            "managementType" to managementType,
            "contributionType" to contributionType
        )
    }

}
