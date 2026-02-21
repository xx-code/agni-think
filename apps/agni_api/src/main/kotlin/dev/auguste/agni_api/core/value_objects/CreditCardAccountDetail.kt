package dev.auguste.agni_api.core.value_objects

import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.interfaces.IAccountDetail
import java.time.LocalDate
import java.time.ZoneOffset

data class CreditCardAccountDetail(
    val creditLimit: Double,
    val invoiceDate: LocalDate): IAccountDetail{

    override fun getType(): AccountType {
        return AccountType.CREDIT_CARD
    }

    override fun toMap(): Map<String, Any> {
        return mapOf("credit_limit" to creditLimit, "invoice_date" to invoiceDate.toString())
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): IAccountDetail {
            if (map == null)
                return CreditCardAccountDetail(creditLimit = 0.0, invoiceDate = LocalDate.now())

            if (!map.containsKey("credit_limit"))
                return CreditCardAccountDetail(creditLimit = 0.0, invoiceDate = LocalDate.now())

            if (!map.containsKey("invoice_date"))
                return CreditCardAccountDetail(creditLimit = 0.0, invoiceDate = LocalDate.now())

            var creditLimit = map["credit_limit"]
            if (creditLimit is Int)
                creditLimit = creditLimit.toDouble()

            val invoiceDate = LocalDate.parse(map.getValue("invoice_date") as String)

            return CreditCardAccountDetail(creditLimit = creditLimit as Double, invoiceDate = invoiceDate)
        }
    }
}
