package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.usecases.finance_reports.dto.CreateFinanceReportInput

data class ApiCreateFinanceReportModel(
    val title: String,
    val description: String
)

fun mapApiCreateFinanceReportModel(apiCreate: ApiCreateFinanceReportModel): CreateFinanceReportInput {
    return CreateFinanceReportInput(
        title = apiCreate.title,
        description = apiCreate.description
    )
}