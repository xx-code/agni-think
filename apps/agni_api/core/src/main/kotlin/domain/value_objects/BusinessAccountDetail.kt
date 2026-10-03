package domain.value_objects

import domain.interfaces.IAccountDetail
import domain.enums.AccountType

data class BusinessAccountDetail(val buffer: Double) : IAccountDetail {
    override fun getType(): AccountType {
        return AccountType.BUSINESS
    }

    override fun toMap(): Map<String, Any> {
        return mapOf("buffer" to buffer)
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): IAccountDetail {
            if (map == null)
                return CheckingAccountDetail(buffer = 0.0)

            if (!map.containsKey("buffer"))
                return CheckingAccountDetail(buffer = 0.0)

            var buffer = map["buffer"]
            if (buffer is Int)
                buffer = buffer.toDouble()

            return CheckingAccountDetail(buffer =  buffer as Double)
        }
    }
}