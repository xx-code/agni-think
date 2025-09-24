import { PatrimonySnapshot } from "@core/domains/entities/patrimonySnapshot";
import { Knex } from "knex";
import Mapper, { KnexFilterExtendAdapter, KnexTable } from "./mapper";
import { PatrimonySnapshotFilter, QueryFilterExtend } from "@core/adapters/repository";
import { mapperTransactionStatus } from "@core/domains/constants";
import { KnexModel } from "./model";

export class KnexPatrimonySnapshotTable implements KnexTable  {
    getTableName(): string {
        return 'patrimony_snapshots'
    }

    async createTable(knex: Knex): Promise<void> {
        if (!(await knex.schema.hasTable('patrimony_snapshots')))
            await knex.schema.createTable('patrimony_snapshots', (table) => {
                table.uuid('patrimony_snapshot_id').primary()
                table.uuid('patrimony_id').index().references('patrimony_id').inTable('patrimonies').onDelete('CASCADE')
                table.float('balance')
                table.date('date')
                table.string('status')
            });
    }

}

export type PatrimonySnapshotModel = KnexModel & {
    patrimony_snapshot_id: string
    patrimony_id: string
    balance: number
    date: Date
    status: string
}

export class PatrimonySnapshotModelMapper implements Mapper<PatrimonySnapshot, PatrimonySnapshotModel> {
    toDomain(model: PatrimonySnapshotModel): PatrimonySnapshot {
        return new PatrimonySnapshot(
            model.patrimony_snapshot_id,
            model.patrimony_id,
            model.balance,
            mapperTransactionStatus(model.status),
            new Date(model.date)
        )
    }
    fromDomain(entity: PatrimonySnapshot): PatrimonySnapshotModel {
        return { 
            patrimony_snapshot_id: entity.getId(),
            patrimony_id: entity.getPatrimonyId(),
            balance: entity.getCurrentBalanceObserver(),
            status: mapperTransactionStatus(entity.getStatus()),
            date: new Date(entity.getDate())
        } 
    }
    getSortFilterFields(): string[] {
        return ['date']
    }
    getIdField(): string {
        return 'patrimony_id'
    }
    getNameField(): string {
        throw new Error("Method not implemented.");
    }
}

export class PatrimonySnapshotFilterExtendAdpater implements KnexFilterExtendAdapter<PatrimonySnapshot, PatrimonySnapshotModel> {
    filterQuery(query: Knex.QueryBuilder, filter: PatrimonySnapshotFilter): void {
        if (filter.patrimonyIds && filter.patrimonyIds.length > 0) query.whereIn('patrimony_id', filter.patrimonyIds);

        if (filter.startDate) query.where('date', '>=', filter.startDate);

        if (filter.endDate) query.where('date', '<=', filter.endDate);
    }
}