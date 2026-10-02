package domain.value_objects

import domain.interfaces.IAccountDetail

data class CheckingAccountDetail(val buffer: Double): IAccountDetail {
    override fun getType(): domain.enums.AccountType {
        return _root_ide_package_.domain.enums.AccountType.CHECKING
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
