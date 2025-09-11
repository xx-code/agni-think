import Repository from "@core/adapters/repository"
import { FREEZE_CATEGORY_ID, SAVING_CATEGORY_ID, TRANSFERT_CATEGORY_ID } from "@core/domains/constants"
import { Category } from "@core/domains/entities/category"
import { Tag } from "@core/domains/entities/tag"
import { CategoryRepository } from "@core/repositories/categoryRepository"
import { TagRepository } from "@core/repositories/tagRepository"

export class SystemSeeder {
    categoryRepository: Repository<Category>
    tagRepository: Repository<Tag>

    constructor(categoryRepository: Repository<Category>, tagRepository: Repository<Tag>) {
        this.categoryRepository = categoryRepository
        this.tagRepository = tagRepository
    }

    async seed() {
        await this.seedCategory()
    }

    private async seedCategory() {
        const savingCate = new Category(SAVING_CATEGORY_ID, 'Epargne', 'i-lucide-piggy-bank', '#4CAF50', true)  
        const freeze = new Category(FREEZE_CATEGORY_ID, 'Freeze', 'i-lucide-snowflake', '#455A64', true)  
        const transfer = new Category(TRANSFERT_CATEGORY_ID, 'Transfert', 'i-lucide-arrow-left-right', '#29B6F6', true)  

        if (!(await this.categoryRepository.get(SAVING_CATEGORY_ID)))
            await this.categoryRepository.create(savingCate)

        if (!(await this.categoryRepository.get(FREEZE_CATEGORY_ID)))
            await this.categoryRepository.create(freeze)

        if (!(await this.categoryRepository.get(TRANSFERT_CATEGORY_ID)))
            await this.categoryRepository.create(transfer)
    }
}