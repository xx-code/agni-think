import { TagRepository } from "@core/repositories/tagRepository";
import { KnexConnector } from "./postgreSqlConnector";
import { Tag } from "@core/domains/entities/tag";
import { Knex } from "knex";
import { isEmpty } from "@core/domains/helpers";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { QueryFilterAllRepository, RepositoryListResult } from "@core/repositories/dto";

export class PostgreSqlTagRepository extends KnexConnector implements TagRepository {
    constructor(connector: Knex) {
        super(connector);
    }
        
    async initialisation(): Promise<void> {
        let isExist = await this.connector.schema.hasTable('tags') 
        if (!isExist) {
            await this.connector.schema.createTable('tags', (table) => {
                table.uuid('tag_id').primary();
                table.string('value').unique().notNullable();
                table.string('color').nullable();
                table.boolean('is_system');
            })
        }
    }

    async createTagNotExist(tags: string[]): Promise<void> {
        return 
    }

    async isTagExistByName(name: string): Promise<boolean> {
        let result = await this.connector('tags').where('value', name).first();
        return !isEmpty(result);
    }

    async isTagExistById(id: string): Promise<boolean> {
        let result = await this.connector('tags').where('tag_id', id).first();
        return !isEmpty(result);
    }

    async isTagExistByIds(ids: string[]): Promise<boolean> {
        let tags_ids = `(${ids.join(',')})`
        let result = await this.connector('tags').whereIn('tag_id', ids);
        return result.length === ids.length;
    }

    async save(tag: Tag): Promise<void> {
        if ((await this.isTagExistByName(tag.getValue())))
            throw new ResourceAlreadyExist(`Tag with value ${tag.getValue()} already exist`)
        
        await this.connector('tags').insert({
            tag_id: tag.getId(),
            value: tag.getValue(),
            color: tag.getColor(),
            is_system: tag.getIsSystem()
        });
    }

    async update(tag: Tag): Promise<void> {
        await this.connector('tags').where('tag_id', tag.getId()).update({
            value: tag.getValue(),
            color: tag.getColor(),
            is_system: tag.getIsSystem()
        });
    }

    async delete(id: string): Promise<void> {
        await this.connector('tags').where('tag_id', id).del();
    }

    async get(id: string): Promise<Tag> {
        let result = await this.connector('tags').where('tag_id', id).first();
        if (!result) {
            throw new ResourceNotFoundError(`Tag with Id: ${id} not found`);
        }
        return new Tag(result.tag_id, result.value, result.color);
    }

    async getByName(value: string): Promise<Tag> {
        let result = await this.connector('tags').where('value', value).first();
        if (!result) {
            throw new ResourceNotFoundError(`Tag with value: ${value} not found`);
        }
        return new Tag(result.tag_id, result.value, result.color);
    }

    async getAll(queryFilter: QueryFilterAllRepository): Promise<RepositoryListResult<Tag>> {
        const query = this.connector('tags').select('*');

        const total = await query.clone().clearSelect().clearOrder().count<{count: number}>("* as count").first() 
        const totalCount = total?.count ?? 0

        if (!queryFilter.queryAll)
            query.offset(queryFilter.offset).limit(queryFilter.limit) 

        const results = await query

        const tags = results.map(result => new Tag(result.tag_id, result.value, result.color));

        return { items: tags, total: totalCount}
    }
}