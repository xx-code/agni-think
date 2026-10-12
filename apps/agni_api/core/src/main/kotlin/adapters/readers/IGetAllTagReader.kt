package adapters.readers

import adapters.dto.QueryFilter
import adapters.dto.RepoList
import adapters.repositories.IQueryExtendBuilder
import adapters.repositories.QueryExtendBuilder
import domain.entities.Tag
import java.util.UUID

interface IGetAllTagReader {
    fun read(query: QueryFilter, userId: UUID?, queryExtend: IQueryExtendBuilder<Tag>? = null): RepoList<Tag>
}