import { Category } from "@core/domains/entities/category";
import { RepositoryError } from "@core/errors/repositoryError";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { CategoryRepository } from "@core/repositories/categoryRepository";
import knex, {Knex} from "knex";
import { KnexConnector } from "./postgreSqlConnector";
import { isEmpty } from "@core/domains/helpers";

export class PostgreSqlCategoryRepository extends KnexConnector implements CategoryRepository {
    constructor(connector: Knex) {
        super(connector)
    }

    async initialisation() {
         try {
            let isExist = await this.connector.schema.hasTable('categories') 
            if (!isExist) {
                await this.connector.schema.createTable("categories", (table) => {
                    table.uuid('category_id').primary()
                    table.string('title')
                    table.string('color').notNullable()
                    table.string('icon_id')
                })
            }
        } catch (err: any) {
            throw new RepositoryError(err)
        }
    }

    async isCategoryExistById(id: string): Promise<boolean> {
        let result = await this.connector('categories').where('category_id', id).first()
        
        return !isEmpty(result)
    }

    async isCategoryExistByName(name: string): Promise<boolean> {
        let result = await this.connector('categories').where('title', 'like', `${name}`).first()

        return !isEmpty(result)
    }

    async isCategoryExistByIds(ids: string[]): Promise<boolean> {
        let result = await this.connector('categories').whereIn('category_id', ids)

        return result.length === ids.length
    }

    async save(dbCategory: Category): Promise<void> {
        if ((await this.isCategoryExistByName(dbCategory.getTitle())))
            throw new ResourceAlreadyExist(`Category with name ${dbCategory.getTitle()} already exist`)

        try {
            await this.connector('categories').insert({
                category_id: dbCategory.getId(),
                title: dbCategory.getTitle(),
                color: dbCategory.getColor(),
                icon_id: dbCategory.getIconId()
            })   
        } catch (error: any) {
            throw new RepositoryError(error)
        }
    }

    async delete(id: string): Promise<void> {
        await this.connector('categories').where('category_id', id).del()
    }

    async update(category: Category): Promise<void> {
        if (!(await this.isCategoryExistById(category.getId())))
            throw new ResourceNotFoundError(`Category with id ${category.getId()} not found`)

        await this.connector('categories').where('category_id', category.getId()).update({
            title: category.getTitle(),
            color: category.getColor(),
            icon_id: category.getIconId()
        })
    }

    async get(id: string): Promise<Category> {
        let result = await this.connector('categories').where('category_id', id).select('*').first()

        if (!result)
            throw new ResourceNotFoundError(`Category with ${id} not found`)

        let category = new Category(result['category_id'], result['title'], result['icon_id'], result['color'])
        
        return category
    }

    async getByTitle(title: string): Promise<Category> {
        let result = await this.connector('categories').where('title', 'like', `%${title}%`).select('*')
        let category = new Category(result[0]['category_id'], result[0]['title'], result[0]['icon_id'], result[0]['color'])
        
        return category
    }

    async getAll(): Promise<Category[]> {
        let results = await this.connector('categories').select('*')

        return results.map(result => (new Category(result['category_id'], result['title'], result['icon_id'], result['color'])))
    }

}