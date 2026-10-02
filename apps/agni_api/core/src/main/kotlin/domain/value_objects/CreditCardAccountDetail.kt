package domain.value_objects

import domain.interfaces.IAccountDetail
import java.time.LocalDate

data class CreditCardAccountDetail(
    val creditLimit: Double,
    val invoiceDate: LocalDate): IAccountDetail{

    override fun getType(): domain.enums.AccountType {
        return _root_ide_package_.domain.enums.AccountType.CREDIT_CARD
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
