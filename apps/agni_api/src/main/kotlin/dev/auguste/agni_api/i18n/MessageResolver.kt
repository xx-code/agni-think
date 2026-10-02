package dev.auguste.agni_api.i18n

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.util.Locale

@Component
class MessageResolver(
    private val objectMapper: ObjectMapper,
    @Value("\${app.i18n.default-locale:fr}") defaultLocaleTag: String,
    @Value("\${app.i18n.locales:fr,en}") supportedLocaleTags: String,
) {
    private val logger = LoggerFactory.getLogger(MessageResolver::class.java)

    private val defaultLocale: Locale = toLocale(defaultLocaleTag)

    private val supportedLocales: List<Locale> = supportedLocaleTags
        .split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { toLocale(it) }
        .ifEmpty { listOf(defaultLocale) }

    private val catalogs: Map<Locale, Map<String, String>> = supportedLocales.associateWith { load(it) }

    fun resolve(errorKey: String, metadata: Map<String, Any> = emptyMap()): String {
        val template = catalogs[currentLocale()].orEmpty()[errorKey]
            ?: catalogs[defaultLocale].orEmpty()[errorKey]
            ?: errorKey
        return interpolate(template, metadata)
    }

    private fun currentLocale(): Locale {
        val locale = LocaleContextHolder.getLocale() ?: return defaultLocale
        if (catalogs.containsKey(locale)) {
            return locale
        }
        return supportedLocales.firstOrNull { it.language == locale.language } ?: defaultLocale
    }

    private fun toLocale(tag: String): Locale = Locale.forLanguageTag(tag.replace("_", "-"))

    private fun load(locale: Locale): Map<String, String> {
        val resource = ClassPathResource("i18n/messages_${locale.toString()}.json")
        if (!resource.exists()) {
            logger.warn("No message catalog found for locale {}", locale)
            return emptyMap()
        }
        return resource.inputStream.use { stream ->
            objectMapper.readValue(stream, object : TypeReference<Map<String, String>>() {})
        }
    }

    private fun interpolate(template: String, metadata: Map<String, Any>): String {
        val interpolated = metadata.entries.fold(template) { acc, (key, value) ->
            acc.replace("{{$key}}", value?.toString().orEmpty())
        }
        return interpolated.replace(UNRESOLVED_PLACEHOLDER, "")
    }

    companion object {
        private val UNRESOLVED_PLACEHOLDER = Regex("""\{\{\s*\w+\s*\}\}""")
    }
}