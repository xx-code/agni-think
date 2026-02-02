import { Knex } from "knex";
import Mapper, {  KnexTable } from "./mapper";
import { KnexModel } from "./model";
import { Deduction, DeductionType } from "@core/domains/entities/decution";
import { mapperDeductionBase, mapperDeductionMode } from "@core/domains/constants";

export class KnexDeductionTable implements KnexTable {
    getTableName(): string {
        return 'deductions'
    }

    async createTable(knex: Knex): Promise<void> {
        if (!(await knex.schema.hasTable('deductions')))
            await knex.schema.createTable("deductions", (table) => {
                table.uuid('deduction_id').primary()
                table.uuid('deduction_type_id')
                table.float('rate')
            });
    }
}

export type DeductionModel = KnexModel & {
    deduction_id: string
    deduction_type_id: string 
    rate: number
}

export class DeductionModelMapper implements Mapper<Deduction, DeductionModel> {
    toDomain(model: DeductionModel): Deduction {
        return new Deduction(
            model.deduction_id,
            model.deduction_type_id, 
            model.rate
        )
    }

    fromDomain(entity: Deduction): DeductionModel {
        return {
            deduction_id: entity.getId(),
            deduction_type_id: entity.getDeductionTypeId(),
            rate: entity.getRate()
        }
    }

    getSortFilterFields(): string[] {
        return []
    }
    getIdField(): string {
        return 'deduction_id'
    }
    getNameField(): string {
        return ''
    }
}

