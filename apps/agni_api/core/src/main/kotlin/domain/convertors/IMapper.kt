package domain.convertors

interface IMapper<T, B> {
    fun to(value: T): B
    fun from(src: B): T
}