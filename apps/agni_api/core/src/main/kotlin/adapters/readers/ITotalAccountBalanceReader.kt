package adapters.readers

interface ITotalAccountBalanceReader {
    fun getOnlyTotalCashAmount(): Long
    fun getTotalAmount(): Double
}