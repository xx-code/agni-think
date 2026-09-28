package dev.auguste.agni_api.core.value_objects

import dev.auguste.agni_api.core.entities.enums.InvoiceModuleLinkerType
import java.util.UUID

data class InvoiceModuleLinker(
    val sourceId: UUID,
    val module: InvoiceModuleLinkerType): IValueObject {
    override fun toMap(): Map<String, Any> {
        return mapOf(
            "source_id" to sourceId.toString(),
            "module" to module.value,
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): InvoiceModuleLinker {
            if (map == null)
                return InvoiceModuleLinker(UUID.randomUUID(), InvoiceModuleLinkerType.SCHEDULE_INVOICE)

            if (!map.containsKey("source_id") && !map.containsKey("module"))
                return InvoiceModuleLinker(UUID.randomUUID(), InvoiceModuleLinkerType.SCHEDULE_INVOICE)

            val sourceId = UUID.fromString(map["source_id"] as String)
            val module = InvoiceModuleLinkerType.fromString(map["module"] as String)

            return InvoiceModuleLinker(
                sourceId,
                module
            )
        }
    }
}