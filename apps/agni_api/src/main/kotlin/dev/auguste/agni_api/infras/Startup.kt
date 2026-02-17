package dev.auguste.agni_api.infras

import dev.auguste.agni_api.core.DOLLAR_CURRENT_ID
import dev.auguste.agni_api.core.FREEZE_CATEGORY_ID
import dev.auguste.agni_api.core.SAVING_CATEGORY_ID
import dev.auguste.agni_api.core.TRANSFERT_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.events.EventType
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.entities.Currency
import dev.auguste.agni_api.core.usecases.notifications.PushNotification
import dev.auguste.agni_api.infras.persistences.CategoryRepository
import dev.auguste.agni_api.infras.persistences.CurrencyRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class Startup (
    private val evenRegister: IEventRegister,
    private val pushNotification: PushNotification,
    private val categoryRepo: CategoryRepository,
    private val currencyRepo: CurrencyRepository
): ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    private fun registerEventLister() {
        try {
            logger.info("[*] Registering event listener")
            evenRegister.subscribe(EventType.NOTIFICATION,pushNotification)
        } catch (e: Exception) {
            logger.info("[!] Error while registering event listener: ${e.message}")
        }
    }

    private fun setupSystemCategories() {
        try {
            logger.info("[*] Setup system categories")
            if (categoryRepo.get(SAVING_CATEGORY_ID) == null)
                categoryRepo.create(
                    Category(
                        SAVING_CATEGORY_ID,
                        title = "Epargne",
                        icon = "i-lucide-piggy-bank",
                        color = "#4CAF50",
                        isSystem = true,
                    )
                )

            if (categoryRepo.get(TRANSFERT_CATEGORY_ID) == null)
                categoryRepo.create(
                    Category(
                        TRANSFERT_CATEGORY_ID,
                        title = "Transfert",
                        icon = "i-lucide-arrow-left-right",
                        color = "#29B6F6",
                        isSystem = true,
                    )
                )

            if (categoryRepo.get(FREEZE_CATEGORY_ID) == null)
                categoryRepo.create(
                    Category(
                        FREEZE_CATEGORY_ID,
                        title = "Freeze",
                        icon = "i-lucide-snowflake",
                        color = "#455A64",
                        isSystem = true,
                    )
                )
        } catch (e: Exception) {
            logger.info("[!] Error while setup categories: ${e.message}")
        }

    }


    private fun setupCurrency() {
        try {
            logger.info("[*] Setup system currency")
            if (currencyRepo.get(DOLLAR_CURRENT_ID) == null)
                currencyRepo.create(
                    Currency(
                        id = DOLLAR_CURRENT_ID,
                        name = "Dollar canada",
                        symbol = "$",
                        locale = "fr-Fr",
                        isBase = true
                    )
                )
        } catch (e: Exception) {
            logger.info("[!] Error while setup currency: ${e.message}")
        }
    }

    private fun setupSystemCurrencies() {}

    override fun run(args: ApplicationArguments) {
        logger.info("[STARTUP]: Initializing...")
        registerEventLister()
        setupSystemCategories()
        setupCurrency()

    }
}