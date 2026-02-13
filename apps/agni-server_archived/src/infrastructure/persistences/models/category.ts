import { Knex } from "knex";
import Mapper, { KnexTable } from "./mapper";
import { Category } from "@core/domains/entities/category";
import { KnexModel } from "./model";

export class KnexCategoryTable implements KnexTable {
    getTableName(): string {
        return 'categories'
    }

    async createTable(knex: Knex): Promise<void> {
        if (!(await knex.schema.hasTable('categories')))
            await knex.schema.createTable("categories", (table) => {
                table.uuid('category_id').primary()
                table.string('title')
                table.string('color').notNullable()
                table.string('icon_id')
                table.boolean('is_system')
            });
    }
} 

export type CategoryModel = KnexModel & {
    category_id: string
    title: string
    color: string
    icon_id: string
    is_system: boolean
}

export class CategoryModelMapper implements Mapper<Category, CategoryModel> {
    toDomain(model: CategoryModel): Category {
        return new Category(
            model.category_id, 
            model.title, 
            model.icon_id, 
            model.color, 
            model.is_system
        ) 
    }
    fromDomain(entity: Category): CategoryModel {
        return {
            category_id: entity.getId(),
            title: entity.getTitle(),
            color: entity.getColor(),
            icon_id: entity.getIconId(),
            is_system: entity.getIsSystem()
        }
    }
    getSortFilterFields(): string[] {
        return []
    }
    getIdField(): string {
        return 'category_id'
    }
    getNameField(): string {
        return 'title'
    }

}