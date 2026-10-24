package adapters

import domain.entities.BankRegister
import domain.entities.ExternalTransaction
import java.time.LocalDate
import java.util.UUID

interface IBankingHandlerAdapter {
    suspend fun syncBankTransactions(bankRegisters: List<BankRegister>)
}