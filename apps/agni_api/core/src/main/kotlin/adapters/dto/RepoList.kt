package adapters.dto

import domain.entities.Entity

data class RepoList<T: Entity>(val items: List<T>, val total: Long)
