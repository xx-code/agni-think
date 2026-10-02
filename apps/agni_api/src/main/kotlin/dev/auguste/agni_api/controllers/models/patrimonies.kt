package dev.auguste.agni_api.controllers.models

import domain.enums.PatrimonySnapshotStatusType
import domain.enums.PatrimonyType
import usecases.patrimonies.dto.CreatePatrimonyInput
import usecases.patrimonies.dto.UpdatePatrimonyInput
import usecases.patrimonies.snapshots.dto.AddSnapshotToPatrimonyInput
import usecases.patrimonies.snapshots.dto.UpdateSnapshotFromPatrimonyInput
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.util.UUID

data class ApiCreatePatrimonyModel(
    @field:NotEmpty("title must not be empty")
    val title: String,

    @field:NotEmpty("amount not be empty")
    @field:Min(0,"amount not be empty")
    val amount: Double,
    val accountIds: Set<UUID>,
    @field:NotNull("type patrimony not be null")
    val type: String
)

data class ApiAddSnapshotToPatrimonyModel(
    @field:Min(0,"amount be positive or zero")
    val balance: Double,
    @field:NotNull("status must not be null")
    val status: String,
    @field:NotNull("date status must not be null")
    val date: LocalDate
)

data class ApiUpdatePatrimonyModel(
    val title: String?,
    val amount: Double?,
    val accountIds: Set<UUID>?,
    val type: String?
)

data class ApiUpdateSnapshotFromPatrimonyModel(
    val balance: Double?,
    val status: String?,
    val date: LocalDate?
)

fun mapApiCreatePatrimony(model: ApiCreatePatrimonyModel): CreatePatrimonyInput {
    return CreatePatrimonyInput(
        title = model.title,
        amount = model.amount,
        accountIds = model.accountIds,
        type = PatrimonyType.fromString(model.type)
    )
}

fun mapApiUpdatePatrimony(id: UUID, model: ApiUpdatePatrimonyModel): UpdatePatrimonyInput {
    return UpdatePatrimonyInput(
        id = id,
        title = model.title,
        amount = model.amount,
        accountIds = model.accountIds,
        type = model.type?.let { PatrimonyType.fromString(model.type) }
    )
}

fun mapApiAddSnapshotToPatrimony(id: UUID, model: ApiAddSnapshotToPatrimonyModel): AddSnapshotToPatrimonyInput {
    return AddSnapshotToPatrimonyInput(
        patrimonyId = id,
        balance = model.balance,
        status = PatrimonySnapshotStatusType.fromString(model.status) ,
        date = model.date,
    )
}

fun mapApiUpdateSnapshotToPatrimony(snapShotId: UUID, model: ApiUpdateSnapshotFromPatrimonyModel): UpdateSnapshotFromPatrimonyInput {
    return UpdateSnapshotFromPatrimonyInput(
        id = snapShotId,
        balance = model.balance,
        status = model.status?.let { PatrimonySnapshotStatusType.fromString(it) },
        date = model.date
    )
}