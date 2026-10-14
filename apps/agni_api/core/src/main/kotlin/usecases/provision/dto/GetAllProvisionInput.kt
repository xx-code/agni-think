package usecases.provision.dto

import adapters.dto.QueryFilter

data class GetAllProvisionInput(
    val queryFilter: QueryFilter
)
