package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.querydsl.sql.Column
import org.springframework.data.domain.Persistable
import org.springframework.data.annotation.Transient
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

abstract class JdbcModel : Persistable<UUID> {
    @Transient
    private var isNewEntry: Boolean = true

    override fun isNew(): Boolean = isNewEntry

    fun setAsExisting() {
        isNewEntry = false
    }
}