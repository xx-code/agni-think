package adapters.readers

import adapters.dto.QueryFilter
import adapters.dto.RepoList
import adapters.repositories.IQueryExtendBuilder
import adapters.repositories.QueryExtendBuilder
import domain.entities.Category
import java.util.UUID

interface IGetAllCategoryReader {
    fun read(query: QueryFilter, userId: UUID?, queryExtend: IQueryExtendBuilder<Category>?): RepoList<Category>
}