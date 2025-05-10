import { Category } from "@core/domains/entities/category";

export class MockCategoryRepository implements MockCategoryRepository {
    private categories: Category[] = [];

    async isCategoryExistById(id: string): Promise<boolean> {
        return this.categories.some(category => category.getId() === id);
    }

    async isCategoryExistByName(name: string): Promise<boolean> {
        return this.categories.some(category => category.getTitle() === name);
    }

    async isCategoryExistByIds(ids: string[]): Promise<boolean> {
        return ids.every(id => this.categories.some(category => category.getId() === id));
    }

    async save(dbCategory: Category): Promise<void> {
        this.categories.push(dbCategory);
    }

    async delete(id: string): Promise<void> {
        this.categories = this.categories.filter(category => category.getId() !== id);
    }

    async update(category: Category): Promise<void> {
        const index = this.categories.findIndex(cat => cat.getId() === category.getId());
        if (index !== -1) {
            this.categories[index] = category;
        }
    }

    async get(id: string): Promise<Category> {
        const category = this.categories.find(category => category.getId() === id);
        if (!category) {
            throw new Error("Category not found");
        }
        return category;
    }

    async getByTitle(title: string): Promise<Category> {
        const category = this.categories.find(category => category.getTitle() === title);
        if (!category) {
            throw new Error("Category not found");
        }
        return category;
    }

    async getAll(): Promise<Category[]> {
        return this.categories;
    }
}