package dev.auguste.agni_api.core.usecases.provisionable.dto

import java.time.LocalDate


data class CreateProvisionInput (
    val title: String,
    val initialCost: Double,
    val acquisitionDate: LocalDate,
    val expectedLifespanMonth: Int,
    val residualValue: Double
)