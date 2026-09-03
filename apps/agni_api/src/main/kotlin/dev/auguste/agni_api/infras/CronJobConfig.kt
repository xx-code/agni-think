package dev.auguste.agni_api.infras

import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.interfaces.ISuspendableUseCase
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class CronJobOrchestratorEach12h(
    @Qualifier("applyScheduleInvoice")
    private val applyScheduleInvoiceUseCase: ISuspendableUseCase<Unit, BackgroundTaskOut>,
    @Qualifier("removeFreezeInvoice")
    private val removeFreezeInvoice: ISuspendableUseCase<Unit, BackgroundTaskOut>,
    @Qualifier("updateDueBudget")
    private val updateBudgetDueDate: ISuspendableUseCase<Unit, BackgroundTaskOut>,
    @Qualifier("autoCompleteInternalLoan")
    private val autoCompleteInternalLoan: ISuspendableUseCase<Unit, BackgroundTaskOut>,
    @Qualifier("applySpendingPeriodTemplate")
    private val applySpendingPeriodTemplate: ISuspendableUseCase<Unit, BackgroundTaskOut>
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    private suspend fun executeTask(taskName: String, action: suspend () -> BackgroundTaskOut) {
        try {
            val res = action()
            logger.info("[*] Finished cron $taskName: ${res.message}")
        } catch (e: Exception) {
            logger.error("[!] Error while executing $taskName", e)
        }
    }

    private suspend fun executeAll() {
        executeTask("schedule invoice") { applyScheduleInvoiceUseCase.execAsync(Unit) }
        executeTask("remove freeze invoice") { removeFreezeInvoice.execAsync(Unit) }
        executeTask("update budget due date") { updateBudgetDueDate.execAsync(Unit) }
        executeTask("update internal loan due date") { autoCompleteInternalLoan.execAsync(Unit) }
        executeTask("spending period template") { applySpendingPeriodTemplate.execAsync(Unit) }
    }

    @Scheduled(cron = "0 0 */12 * * *")
    fun schedule() {
        logger.info("[SCHEDULE] Running 12-hour cron schedule")
        runBlocking { executeAll() }
    }

    override fun run(args: ApplicationArguments) {
        logger.info("[STARTUP] Applying initial startup cron tasks")
        runBlocking { executeAll() }
    }
}