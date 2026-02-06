package dev.auguste.agni_api.core.adapters.dto

import dev.auguste.agni_api.core.entities.Entity

data class RepoList<T: Entity>(val items: List<T>, val total: Long)
