import { Knex } from "knex";
import Mapper, { KnexTable } from "./mapper";
import { KnexModel } from "./model";
import { DeductionType } from "@core/domains/entities/decution";
import { mapperDeductionBase, mapperDeductionMode } from "@core/domains/constants";

export class KnexDeductionTypeTable implements KnexTable {
    getTableName(): string {
        return 'deduction_types'
    }

    async createTable(knex: Knex): Promise<void> {
        if (!(await knex.schema.hasTable('deduction_types')))
            await knex.schema.createTable("deduction_types", (table) => {
                table.uuid('deduction_type_id').primary()
                table.string('title')
                table.string('description')
                table.string('base')
                table.string('mode')
            });
    }
}

export type DeductionTypeModel = KnexModel & {
    deduction_type_id: string
    title: string
    description: string
    base: string
    mode: string
}

export class DeductionTypeModelMapper implements Mapper<DeductionType, DeductionTypeModel> {
    toDomain(model: DeductionTypeModel): DeductionType {
        return new DeductionType(
            model.deduction_type_id,
            model.title,
            model.description,
            mapperDeductionBase(model.base),
            mapperDeductionMode(model.mode),
        )
    }

    fromDomain(entity: DeductionType): DeductionTypeModel {
        return {
            deduction_type_id: entity.getId(),
            description: entity.getDescription(),
            title: entity.getTitle(),
            mode: entity.getMode(),
            base: entity.getBase()
        }
    }

    getSortFilterFields(): string[] {
        return []
    }
    getIdField(): string {
        return 'deduction_type_id'
    }
    getNameField(): string {
        return 'title'
    }
}
