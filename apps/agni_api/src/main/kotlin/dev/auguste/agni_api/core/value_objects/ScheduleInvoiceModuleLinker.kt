package dev.auguste.agni_api.core.value_objects

import dev.auguste.agni_api.core.entities.enums.InvoiceModuleLinkerType
import dev.auguste.agni_api.core.entities.enums.ScheduleInvoiceModuleLinkerType
import java.util.UUID

data class ScheduleInvoiceModuleLinker(
    val sourceId: UUID,
    val module: ScheduleInvoiceModuleLinkerType): IValueObject {
    override fun toMap(): Map<String, Any> {
        return mapOf(
            "source_id" to sourceId.toString(),
            "module" to module.value,
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): ScheduleInvoiceModuleLinker? {
            if (map == null)
                return null

            if (!map.containsKey("source_id") && !map.containsKey("module"))
                return null

            val sourceId = UUID.fromString(map["source_id"] as String)
            val module = ScheduleInvoiceModuleLinkerType.fromString(map["module"] as String)

            return ScheduleInvoiceModuleLinker(
                sourceId,
                module
            )
        }
    }
}