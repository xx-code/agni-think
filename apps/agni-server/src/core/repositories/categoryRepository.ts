import { Category } from "../domains/entities/category";


export interface CategoryRepository {
    isCategoryExistById(id: string): Promise<boolean>
    isCategoryExistByName(name: string): Promise<boolean>
    isCategoryExistByIds(ids: string[]): Promise<boolean>
    save(dbCategory: Category): Promise<void>
    delete(id: string): Promise<void>
    update(category: Category): Promise<void>
    get(id: string): Promise<Category|null>
    getByTitle(title: string): Promise<Category>
    getAll(): Promise<Category[]>
}

export interface CategoryUseChecker {
    isInUse(categoryId: string): Promise<boolean>
}