import { Tag } from "../domains/entities/tag";
import { QueryFilterAllRepository, RepositoryListResult } from "./dto";

export interface TagRepository {
    createTagNotExist(ids: string[]): Promise<void>
    isTagExistByName(name: string): Promise<boolean>
    isTagExistById(id: string): Promise<boolean>
    isTagExistByIds(ids: string[]): Promise<boolean>
    save(tag: Tag): Promise<void>
    update(tag: Tag): Promise<void>
    delete(title: string): Promise<void>
    get(id: string): Promise<Tag|null>
    getByName(value: string): Promise<Tag>
    getAll(queryFilter: QueryFilterAllRepository): Promise<RepositoryListResult<Tag>>
}

export interface TagChecker {
    inUse(tagId: string): Promise<boolean>
}