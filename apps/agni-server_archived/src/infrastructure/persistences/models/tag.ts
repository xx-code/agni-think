import { Tag } from "@core/domains/entities/tag";
import { Knex } from "knex";
import Mapper, { KnexTable } from "./mapper";
import { KnexModel } from "./model";

export class KnexTagTable implements KnexTable {
    getTableName(): string {
        return 'tags'
    }

    async createTable(knex: Knex): Promise<void> {
        if (!(await knex.schema.hasTable('tags')))
            await knex.schema.createTable('tags', (table) => {
                table.uuid('tag_id').primary();
                table.string('value').unique().notNullable();
                table.string('color').nullable();
                table.boolean('is_system');
            });
    }

}

export type TagModel = KnexModel & {
    tag_id: string
    value: string
    color?: string
    is_system: boolean
}

export class TagModelMapper implements Mapper<Tag, TagModel> {
    toDomain(model: TagModel): Tag {
        return new Tag(
            model.tag_id,
            model.value,
            model.color,
            model.is_system
        )
    }
    fromDomain(entity: Tag): TagModel {
        return {
            tag_id: entity.getId(),
            value: entity.getValue(),
            is_system: entity.getIsSystem()
        }
    }
    getSortFilterFields(): string[] {
        return []
    }
    getIdField(): string {
        return 'tag_id'
    }
    getNameField(): string {
        return 'value'
    }
}