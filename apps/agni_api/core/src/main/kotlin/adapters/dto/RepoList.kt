package adapters.dto

import domain.entities.Entity

data class RepoList<T: domain.entities.Entity>(val items: List<T>, val total: Long)
