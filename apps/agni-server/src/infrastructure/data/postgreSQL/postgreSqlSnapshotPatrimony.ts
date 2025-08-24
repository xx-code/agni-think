import { PatrimonySnapshotRepository, PatrimonyTransactionFilter } from "@core/repositories/patrimonyRepository";
import { KnexConnector } from "./postgreSqlConnector";
import { PatrimonySnapshot } from "@core/domains/entities/patrimonySnapshot";
import { RepositoryListResult } from "@core/repositories/dto";
import { Knex } from "knex";
import { isEmpty } from "@core/domains/helpers";
import { mapperTransactionStatus } from "@core/domains/constants";

export class PostgreSqlSnapshotPatrimonyRepository extends KnexConnector implements PatrimonySnapshotRepository {

    constructor(connector: Knex) {
        super(connector)
    }

    initialisation(): Promise<void> {
        throw new Error("Method not implemented.");
    }

    async save(snapshot: PatrimonySnapshot): Promise<void> {
        await this.connector('patrimony_snapshots').insert({
            patrimony_snapshot_id: snapshot.getId(),
            patrimony_id: snapshot.getPatrimonyId(),
            balance: snapshot.getCurrentBalanceObserver(),
            date: snapshot.getDate(),
            status: snapshot.getStatus()
        });
    }

    async exist(id: string): Promise<boolean> {
        let result = await this.connector('patrimony_snapshots').where('patrimony_snapshot_id', id).first();
        return !isEmpty(result);
    }

    async get(id: string): Promise<PatrimonySnapshot | null> {
        let result = await this.connector('patrimony_snapshots').where('patrimony_snapshot_id', id).first();
        if (!result)
            return null

        return new PatrimonySnapshot(
            result['patrimony_snapshot_id'],
            result['patrimony_id'],
            result['balance'],
            mapperTransactionStatus(result['status']),
            new Date(result['date'])
        )
    }

    async getLastest(filter: PatrimonyTransactionFilter): Promise<PatrimonySnapshot[]> {
        let query = this.connector('patrimony_snapshots').select('*');
        if (filter.patrimonyIds && filter.patrimonyIds.length > 0) {
            query.whereIn('patrimony_id', filter.patrimonyIds);
        }
        // On récupère le dernier snapshot par date
        query.orderBy('date', 'desc');
        const results = await query;
        if (!results || results.length === 0) return [];

        const latestByPatrimony: PatrimonySnapshot[] = [];
        const seen: Set<string> = new Set();
        for (const res of results) {
            if (!seen.has(res['patrimony_id'])) {
                latestByPatrimony.push(new PatrimonySnapshot(
                    res['patrimony_snapshot_id'],
                    res['patrimony_id'],
                    res['balance'],
                    mapperTransactionStatus(res['status']),
                    new Date(res['date'])
                ));
                seen.add(res['patrimony_id']);
            }
        }
        return latestByPatrimony;
    }

    async getAll(filter: PatrimonyTransactionFilter): Promise<RepositoryListResult<PatrimonySnapshot>> {
        let query = this.connector('patrimony_snapshots').select('*');
        if (filter.patrimonyIds && filter.patrimonyIds.length > 0) query.whereIn('patrimony_id', filter.patrimonyIds);

        if (filter.startDate) 
            query.where('date', '>=', filter.startDate);
        if (filter.endDate) 
            query.where('date', '<=', filter.endDate);

        let results = await query;

        return { 
            items: results.map(res => new PatrimonySnapshot(
                res['patrimony_snapshot_id'],
                res['patrimony_id'],
                res['balance'],
                mapperTransactionStatus(res['status']),
                new Date(res['date']
            ))), 
            total: results.length 
        } 
    }

    async getManyById(ids: string[]): Promise<PatrimonySnapshot[]> {
        const patrimonies = await this.connector('patrimony_snapshots').whereIn('patrimony_snapshot_id', ids);
        return patrimonies.map(res => new PatrimonySnapshot(
            res['patrimony_snapshot_id'],
            res['patrimony_id'],
            res['balance'],
            mapperTransactionStatus(res['status']),
            new Date(res['date']) 
        )) 
    }

    async delete(id: string): Promise<void> {
        await this.connector('patrimony_snapshots').where('patrimony_snapshot_id', id).del();
    }

    async deleteByIds(ids: string[]): Promise<void> {
        await this.connector('patrimony_snapshots').whereIn('patrimony_snapshot_id', ids).del();
    }

    async update(snapshot: PatrimonySnapshot): Promise<void> {
        await this.connector('patrimony_snapshots').where('patrimony_snapshot_id', snapshot.getId()).update({
            patrimony_snapshot_id: snapshot.getId(),
            patrimony_id: snapshot.getPatrimonyId(),
            balance: snapshot.getCurrentBalanceObserver(),
            date: snapshot.getDate(),
            status: snapshot.getStatus()
        });
    } 

}