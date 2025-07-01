import { Tag } from "../domains/entities/tag";

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
    getAll(): Promise<Tag[]>
}

export interface TagChecker {
    inUse(tagId: string): Promise<boolean>
}