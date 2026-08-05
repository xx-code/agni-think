package dev.auguste.agni_api.infras

import org.slf4j.LoggerFactory
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.internal_loan.AutoCompleteInternalLoan
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class CronJobOrchestratorEach12h(
    @Qualifier("applyScheduleInvoice")
    private val applyScheduleInvoiceUseCase: IUseCase<Unit, BackgroundTaskOut>,
    @Qualifier("removeFreezeInvoice")
    private val removeFreezeInvoice: IUseCase<Unit, BackgroundTaskOut>,
    @Qualifier("updateDueBudget")
    private val updateBudgetDueDate: IUseCase<Unit, BackgroundTaskOut>,
    @Qualifier("autoCompleteInternalLoan")
    private val autoCompleteInternalLoan: IUseCase<Unit, BackgroundTaskOut>
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun execute() {
        try {
            var res = applyScheduleInvoiceUseCase.execAsync(input = Unit)
            logger.info("[*] Finished cron schedule invoice: ${res.message}")

            res = removeFreezeInvoice.execAsync(input = Unit)
            logger.info("[*] Finished cron remove freeze invoice: ${res.message}")

            res = updateBudgetDueDate.execAsync(input = Unit)
            logger.info("[*] Finished cron update budget due date: ${res.message}")

            res = autoCompleteInternalLoan.execAsync(input = Unit)
            logger.info("[*] Finished cron update internal loan due date: ${res.message}")
        } catch (e: Exception) {
            logger.info("[!] Error while executing task: ${e.message}")
        }
    }

    @Scheduled(cron = "0 0 */12 * * *")
    fun schedule() {
        logger.info("[SCHEDULE] Cron schedule each 12h")
        execute()
    }

    override fun run(args: ApplicationArguments) {
        logger.info("[STARTUP] Apply Cron")
        execute()
    }
}