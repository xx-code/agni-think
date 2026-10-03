package domain.value_objects

import domain.interfaces.IAccountDetail
import domain.enums.AccountType

data class SavingAccountDetail(
    val secureAmount: Double
): IAccountDetail {
    override fun getType(): AccountType {
        return AccountType.SAVING
    }

    override fun toMap(): Map<String, Any> {
        return mapOf("secure_amount" to secureAmount)
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): IAccountDetail {
            if (map == null)
                return SavingAccountDetail(secureAmount = 0.0)

            if (!map.containsKey("secure_amount"))
                return SavingAccountDetail(secureAmount = 0.0)

            var secureAmount = map["secure_amount"]
            if (secureAmount is Int)
                secureAmount = secureAmount.toDouble()

            return SavingAccountDetail(secureAmount = secureAmount as Double)
        }
    }

}