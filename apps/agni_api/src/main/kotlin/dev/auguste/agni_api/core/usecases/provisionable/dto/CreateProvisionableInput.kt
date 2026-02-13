package dev.auguste.agni_api.core.usecases.provisionable.dto

import java.time.LocalDate


data class CreateProvisionableInput (
    val title: String,
    val initialCost: Double,
    val acquisitionDate: LocalDate,
    val expectedLifespanMonth: Int,
    val residualValue: Double
)