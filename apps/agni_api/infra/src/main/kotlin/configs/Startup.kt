package configs

import domain.DOLLAR_CURRENT_ID
import domain.FREEZE_CATEGORY_ID
import domain.SAVING_CATEGORY_ID
import domain.TRANSFERT_CATEGORY_ID
import domain.UNKNOWN_CATEGORY_ID
import adapters.events.IEventRegister
import adapters.events.EventType
import adapters.events.listeners.ICreateExternalTransactionListener
import adapters.events.listeners.ICreateInvoiceEventListener
import adapters.events.listeners.ICreateManyExternalTransactionListener
import adapters.events.listeners.IDeleteInvoiceEventListener
import domain.entities.Category
import domain.entities.Currency
import usecases.notifications.PushNotification
import domain.entities.Color
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import persistences.CategoryRepository
import persistences.CurrencyRepository

@Component
@ConditionalOnProperty(
    name = ["agni.startup.enabled"],
    havingValue = "true",
    matchIfMissing = true
)
class Startup (
    private val evenRegister: IEventRegister,
    private val pushNotification: PushNotification,
    private val categoryRepo: CategoryRepository,
    private val currencyRepo: CurrencyRepository,
    private val createEmbedding: ICreateInvoiceEventListener,
    private val updateEmbedding: IDeleteInvoiceEventListener,
    private val createEmbeddingExternalTrans: ICreateExternalTransactionListener,
    private val createManyEmbeddingExternalTrans: ICreateManyExternalTransactionListener
): ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    private fun registerEventLister() {
        try {
            logger.info("[*] Registering event listener")
            evenRegister.subscribe(EventType.NOTIFICATION,pushNotification)
            evenRegister.subscribe(EventType.CREATE_INVOICE, createEmbedding)
            evenRegister.subscribe(EventType.DELETE_INVOICE, updateEmbedding)
            evenRegister.subscribe(EventType.CREATE_EXTERNAL_TRANSACTION, createEmbeddingExternalTrans)
            evenRegister.subscribe(EventType.CREATE_MANY_EXTERNAL_TRANSACTION, createManyEmbeddingExternalTrans)
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
                        color = Color("#4CAF50") ,
                        isSystem = true,
                    )
                )

            if (categoryRepo.get(TRANSFERT_CATEGORY_ID) == null)
                categoryRepo.create(
                    Category(
                        TRANSFERT_CATEGORY_ID,
                        title = "Transfert",
                        icon = "i-lucide-arrow-left-right",
                        color = Color("#29B6F6") ,
                        isSystem = true,
                    )
                )

            if (categoryRepo.get(FREEZE_CATEGORY_ID) == null)
                categoryRepo.create(
                    Category(
                        FREEZE_CATEGORY_ID,
                        title = "Freeze",
                        icon = "i-lucide-snowflake",
                        color = Color("#455A64"),
                        isSystem = true,
                    )
                )

            if (categoryRepo.get(UNKNOWN_CATEGORY_ID) == null)
                categoryRepo.create(
                    Category(
                        UNKNOWN_CATEGORY_ID,
                        title = "Unknown",
                        icon = "i-lucide-circle-question-mark",
                        color = Color("#313131"),
                        isSystem = true
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