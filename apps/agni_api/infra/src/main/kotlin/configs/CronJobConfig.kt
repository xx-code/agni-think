package configs

import usecases.dto.BackgroundTaskOut
import usecases.dto.Result
import usecases.interfaces.IUseCase
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(
    name = ["agni.startup.enabled"],
    havingValue = "true",
    matchIfMissing = true
)
class CronJobOrchestratorEach12h(
    @Qualifier("applyScheduleInvoice")
    private val applyScheduleInvoiceUseCase: IUseCase<Unit, BackgroundTaskOut>,
    @Qualifier("removeFreezeInvoice")
    private val removeFreezeInvoice: IUseCase<Unit, BackgroundTaskOut>,
    @Qualifier("updateDueBudget")
    private val updateBudgetDueDate: IUseCase<Unit, BackgroundTaskOut>,
    @Qualifier("autoCompleteInternalLoan")
    private val autoCompleteInternalLoan: IUseCase<Unit, BackgroundTaskOut>,
    @Qualifier("applySpendingPeriodTemplate")
    private val applySpendingPeriodTemplate: IUseCase<Unit, BackgroundTaskOut>,
    @Qualifier("makeProvisionInstallment")
    private val makeProvisionInstallment: IUseCase<Unit, BackgroundTaskOut>
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    private suspend fun executeTask(taskName: String, action: suspend () -> Result<BackgroundTaskOut>) {
        try {
            val res = action().getOrThrow()
            logger.info("[*] Finished cron $taskName: ${res.message}")
        } catch (e: Exception) {
            logger.error("[!] Error while executing $taskName", e)
        }
    }

    private suspend fun executeAll() {
        executeTask("provision make installment fund") { makeProvisionInstallment.execute(Unit) }
        executeTask("schedule invoice") { applyScheduleInvoiceUseCase.execute(Unit) }
        executeTask("remove freeze invoice") { removeFreezeInvoice.execute(Unit) }
        executeTask("update budget due date") { updateBudgetDueDate.execute(Unit) }
        executeTask("update internal loan due date") { autoCompleteInternalLoan.execute(Unit) }
        executeTask("spending period template") { applySpendingPeriodTemplate.execute(Unit) }
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