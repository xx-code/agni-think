import { Tag } from "@core/domains/entities/tag";
import { TagRepository } from "@core/repositories/tagRepository";

export class MockTagRepository implements TagRepository {
    private tags: Map<string, Tag> = new Map();

    async createTagNotExist(ids: string[]): Promise<void> {
        ids.forEach(id => {
            if (!this.tags.has(id)) {
                this.tags.set(id, new Tag(id, `Tag ${id}`, "#FFFFFF")); // Valeur par défaut
            }
        });
    }

    async isTagExistByName(name: string): Promise<boolean> {
        return Array.from(this.tags.values()).some(tag => tag.getValue() === name);
    }

    async isTagExistById(id: string): Promise<boolean> {
        return this.tags.has(id);
    }

    async isTagExistByIds(ids: string[]): Promise<boolean> {
        return ids.every(id => this.tags.has(id));
    }

    async save(tag: Tag): Promise<void> {
        this.tags.set(tag.getId(), tag);
    }

    async update(tag: Tag): Promise<void> {
        if (!this.tags.has(tag.getId())) {
            throw new Error(`Tag with ID ${tag.getId()} not found`);
        }
        this.tags.set(tag.getId(), tag);
    }

    async delete(id: string): Promise<void> {
        if (!this.tags.has(id)) {
            throw new Error(`Tag with ID ${id} not found`);
        }
        this.tags.delete(id);
    }

    async get(id: string): Promise<Tag> {
        const tag = this.tags.get(id);
        if (!tag) {
            throw new Error(`Tag with ID ${id} not found`);
        }
        return tag;
    }

    async getByName(value: string): Promise<Tag> {
        const tag = Array.from(this.tags.values()).find(tag => tag.getValue() === value);
        if (!tag) {
            throw new Error(`Tag with name "${value}" not found`);
        }
        return tag;
    }

    async getAll(): Promise<Tag[]> {
        return Array.from(this.tags.values());
    }
}
