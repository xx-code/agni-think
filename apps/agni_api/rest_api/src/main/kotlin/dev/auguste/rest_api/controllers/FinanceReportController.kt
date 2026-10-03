package dev.auguste.rest_api.controllers

import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiCreateFinanceReportModel
import dev.auguste.rest_api.controllers.models.mapApiCreateFinanceReportModel
import usecases.CreatedOutput
import usecases.ListOutput
import usecases.finance_reports.dto.CreateFinanceReportInput
import usecases.finance_reports.dto.DeleteFinanceReportInput
import usecases.finance_reports.dto.GetFinanceReportInput
import usecases.finance_reports.dto.GetFinanceReportOutput
import usecases.interfaces.IUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v2/finance-reports")
class FinanceReportController(
    private val createFinanceReport: IUseCase<CreateFinanceReportInput, CreatedOutput>,
    private val deleteFinanceReport: IUseCase<DeleteFinanceReportInput, Unit>,
    private val getFinanceReport: IUseCase<GetFinanceReportInput, GetFinanceReportOutput>,
    private val getAllFinanceReport: IUseCase<QueryFilter, ListOutput<GetFinanceReportOutput>>
) {
    @GetMapping("/{id}")
    fun getFinanceReport(@PathVariable id: UUID): ResponseEntity<GetFinanceReportOutput> {
        return ResponseEntity.ok(getFinanceReport.execAsync(GetFinanceReportInput(id)))
    }

    @GetMapping
    fun getAllFinanceReport(query: QueryFilter): ResponseEntity<ListOutput<GetFinanceReportOutput>> {
        return ResponseEntity.ok(getAllFinanceReport.execAsync(query))
    }

    @PostMapping
    fun createFinanceReport(@RequestBody input: ApiCreateFinanceReportModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createFinanceReport.execAsync(mapApiCreateFinanceReportModel(input)))
    }

    @DeleteMapping("/{id}")
    fun deleteFinanceReport(@PathVariable id: UUID): ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteFinanceReport.execAsync(DeleteFinanceReportInput(id) ))
    }
}