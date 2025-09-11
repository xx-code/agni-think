import { KnexModel } from "@infra/persistences/models/model";
import Entity from "../../core/domains/entities/entity";
import KnexRepository from "../persistences/knexRepository";
import Mapper, { KnexTable } from "../persistences/models/mapper";
import knex, { Knex } from "knex";
import { v4 as uuid } from "uuid";
import { describe, it, expect, beforeAll, afterAll, beforeEach } from "vitest";

// const db: Knex = knex( {
//   client: process.env.DB_CLIENT || '',
//   connection: {
//     host: process.env.DB_HOST || '',
//     user: process.env.DB_USER || '',
//     password: process.env.DB_PASSWORD || '',
//     database : process.env.DB_NAME || ''
//   }
// })


export type TestModel = KnexModel & {
    id: string
    title: string
    values: any
}

class TestModelMapper implements Mapper<TestEntity, TestModel> {
  getIdField(): string {
    return "id";
  }
  getNameField(): string {
    return "title";
  }
  getSortFilterFields(): string[] {
    return ["title"];
  }
  fromDomain(entity: TestEntity) {
    return {
      id: entity.getId(),
      title: entity.getTitle(),
      values: JSON.stringify(entity.getValues()) 
    };
  }
  toDomain(model: TestModel): TestEntity {
    return new TestEntity(model.id, model.title, JSON.parse(model.values || "[]"));
  }
}


class TestEntity extends Entity {
    private title: string
    private values: string[]

    constructor(id: string, title: string, values: string[]) {
        super(id)
        this.title = title
        this.values = values
    }

    setTitle(title: string) {
        this.title = title
    }

    getTitle(): string {
        return this.title
    }

    setValues(values: string[]) {
        this.values = values
    }

    getValues(): string[] {
        return this.values
    }
} 

class KnexTestTable implements KnexTable {
    getTableName(): string {
        return 'test_models'
    }
    async createTable(knex: Knex): Promise<void> {
        await knex.schema.createTable("test_models", (table) => {
            table.uuid("id").primary();
            table.string("title");
            table.json("values")
        });
    }

}

describe("KnexRepository CRUD", () => {
    let repo: KnexRepository<TestEntity, any>;
    let db: Knex
    let testTable: KnexTable
        
    beforeAll(async () => {
        db = knex({
            client: "sqlite3",
            connection: { filename: ":memory:" },
            useNullAsDefault: true,
        });

        testTable = new KnexTestTable()

        await testTable.createTable(db)

        repo = new KnexRepository<TestEntity, any>(db, new KnexTestTable(), new TestModelMapper());
    });

    afterAll(async () => {
        await db.destroy();
    });

    beforeEach(async () => {
        await db(testTable.getTableName()).truncate();
    });

    it("should create and get an entity", async () => {
        const id = uuid().toString();
        const entity = new TestEntity(id, "First title", ["1", "2"]);

        await repo.create(entity);
        const result = await repo.get(id);

        expect(result).not.toBeNull();
        expect(result?.getId()).toBe(id);
        expect(result?.getTitle()).toBe("First title");
        expect(result?.getValues().length).toBe(2)
        expect(result?.getValues()[0]).toBe("1")
    });

    it("should update an entity", async () => {
        const id = uuid();
        const entity = new TestEntity(id, "Original", ["1", "2"]);
        await repo.create(entity);

        entity.setTitle("Updated");
        await repo.update(entity);

        const updated = await repo.get(id);
        expect(updated?.getTitle()).toBe("Updated");
    });

    it("should delete an entity", async () => {
        const id = uuid();
        const entity = new TestEntity(id, "ToDelete", ["1", "2"]);
        await repo.create(entity);

        await repo.delete(id);
        const result = await repo.get(id);

        expect(result).toBeNull();
    });

    it("should get many by ids", async () => {
        const id1 = uuid();
        const id2 = uuid();
        await repo.create(new TestEntity(id1, "One", ["1", "2"]));
        await repo.create(new TestEntity(id2, "Two", ["1", "2"]));

        const results = await repo.getManyByIds([id1, id2]);
        expect(results).toHaveLength(2);
        const titles = results.map(r => r.getTitle());
        expect(titles).toContain("One");
        expect(titles).toContain("Two");
    });

    it("should check existence by name", async () => {
        const id = uuid();
        await repo.create(new TestEntity(id, "Special", ["1", "2"]));

        const exists = await repo.existByName("Special");
        const notExists = await repo.existByName("Nope");

        expect(exists).toBe(true);
        expect(notExists).toBe(false);
    });

    it("should sort and paginate results with getAll", async () => {
        await repo.create(new TestEntity(uuid(), "B-title", ["1", "2"]));
        await repo.create(new TestEntity(uuid(), "A-title", ["1", "2"]));
        await repo.create(new TestEntity(uuid(), "C-title", ["1", "2"]));

        const results = await repo.getAll(
            {
            sort: { sortBy: "title", asc: true },
            limit: 2,
            offset: 0,
            queryAll: true,
            }
        );

        expect(results.total).toBe(3);
        expect(results.items[0].getTitle()).toBe("A-title");
        expect(results.items[1].getTitle()).toBe("B-title");
    });
}) 