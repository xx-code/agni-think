package dev.auguste.agni_api.infras

import dev.auguste.agni_api.core.adapters.events.IEventListener
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.usecases.notifications.PushNotification
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class Startup (
    private val evenRegister: IEventRegister,
    private val pushNotification: PushNotification
): ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    private fun registerEventLister() {
        try {
            logger.info("[*] Registering event listener")
            evenRegister.subscribe("notification",pushNotification)
        } catch (e: Exception) {
            logger.info("[!] Error while registering event listener: ${e.message}")
        }
    }

    override fun run(args: ApplicationArguments) {
        logger.info("[STARTUP]: Initializing...")
        registerEventLister()
    }
}