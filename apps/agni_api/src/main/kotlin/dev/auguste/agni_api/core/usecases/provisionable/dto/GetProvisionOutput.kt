package dev.auguste.agni_api.core.usecases.provisionable.dto

import java.time.LocalDate
import java.util.UUID

data class GetProvisionOutput(
    val id: UUID,
    val title: String,
    val initialCost: Double,
    val totalCost: Double,
    val acquisitionDate: LocalDate,
    val expectedLifespanMonth: Int,
    val costByMonth: Double,
    val monthlyPayment: Double,
    val residualValue: Double,
    val nextPaymentDate: LocalDate?,
    val nextPaymentAmount: Double?,
)
