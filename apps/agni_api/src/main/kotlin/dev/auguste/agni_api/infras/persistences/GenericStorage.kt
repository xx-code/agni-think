package dev.auguste.agni_api.infras.persistences

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.PagingAndSortingRepository

@NoRepositoryBean
interface GenericStorage<TModel: Any, ID: Any>: CrudRepository<TModel, ID>, PagingAndSortingRepository<TModel, ID>