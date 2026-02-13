package dev.auguste.agni_api.infras

import org.slf4j.LoggerFactory
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class CronJobApplyScheduleInvoice(
    @Qualifier("applyScheduleInvoice")
    private val applyScheduleInvoiceUseCase: IUseCase<Unit, BackgroundTaskOut>
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun execute() {
        try {
            val res = applyScheduleInvoiceUseCase.execAsync(input = Unit)
            logger.info("[*] Finished cron schedule invoice: ${res.message}")
        } catch (e: Exception) {
            logger.info("[!] Error while executing task: ${e.message}")
        }
    }

    @Scheduled(cron = "0 0 */12 * * *")
    fun schedule() {
        println("[SCHEDULE] Cron schedule invoice")
        execute()
    }

    override fun run(args: ApplicationArguments) {
        logger.info("[STARTUP] Apply schedule invoice")
        execute()
    }
}

@Service
class CronJobRemoveFreezeInvoice(
    @Qualifier("removeFreezeInvoice")
    private val removeFreezeInvoice: IUseCase<Unit, BackgroundTaskOut>
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun execute() {
        try {
            val res = removeFreezeInvoice.execAsync(input = Unit)
            logger.info("[*] Finished cron remove freeze invoice: ${res.message}")
        } catch (e: Exception) {
            logger.info("[!] Error while executing task: ${e.message}")
        }
    }

    @Scheduled(cron = "0 0 */12 * * *")
    fun schedule() {
        println("[SCHEDULE] Cron freeze invoice")
        execute()
    }

    override fun run(args: ApplicationArguments) {
        logger.info("[STARTUP] Remove freeze invoice")
        execute()
    }
}

@Service
class CronJobUpdateBudgetDueDate(
    @Qualifier("updateDueBudget")
    private val updateBudgetDueDate: IUseCase<Unit, BackgroundTaskOut>
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun execute() {
        try {
            val res = updateBudgetDueDate.execAsync(input = Unit)
            logger.info("[*] Finished cron update budget due date: ${res.message}")
        } catch (e: Exception) {
            logger.info("[!] Error while executing task: ${e.message}")
        }
    }

    @Scheduled(cron = "0 0 */12 * * *")
    fun schedule() {
        println("[SCHEDULE] Cron update budget due date")
        execute()
    }

    override fun run(args: ApplicationArguments) {
        logger.info("[STARTUP] Apply budget due date")
        execute()
    }
}