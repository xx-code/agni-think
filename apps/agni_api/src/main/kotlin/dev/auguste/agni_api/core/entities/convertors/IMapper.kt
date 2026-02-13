package dev.auguste.agni_api.core.entities.convertors

interface IMapper<T, B> {
    fun to(value: T): B
    fun from(src: B): T
}