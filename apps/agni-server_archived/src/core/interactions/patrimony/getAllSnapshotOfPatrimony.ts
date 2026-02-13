import { IUsecase } from "../interfaces";
import { ListDto, QueryFilter } from "@core/dto/base";
import Repository, { PatrimonySnapshotFilter } from "@core/adapters/repository";
import { PatrimonySnapshot } from "@core/domains/entities/patrimonySnapshot";

export type RequestAllSnapshotPatrimony = QueryFilter & {
    patrimonyId: string
}

export type GetAllSnapshotPatrimonyDto = {
    id: string,
    patrimonyId: string
    balance: number
    date: Date
    status: string
}

export class GetAllSnapshotOfPatrimony implements IUsecase<RequestAllSnapshotPatrimony, ListDto<GetAllSnapshotPatrimonyDto>> {
    private snapshotPatrimonyRepo: Repository<PatrimonySnapshot>

    constructor(
        snapshotPatrimonyRepo: Repository<PatrimonySnapshot>) {
            this.snapshotPatrimonyRepo = snapshotPatrimonyRepo
    }

    async execute(request: RequestAllSnapshotPatrimony): Promise<ListDto<GetAllSnapshotPatrimonyDto>> {
        const extendFilter = new PatrimonySnapshotFilter()
        extendFilter.patrimonyIds = [request.patrimonyId]
        const snapshots = await this.snapshotPatrimonyRepo.getAll({ 
            limit: 0, 
            offset: 0,
            queryAll: true
        }, extendFilter);

        return { 
            items: snapshots.items.map(i => ({
                id: i.getId(),
                patrimonyId: i.getPatrimonyId(),
                balance: i.getCurrentBalanceObserver(),
                date: i.getDate(),
                status: i.getStatus()
            })),
            totals: snapshots.total
        }
    }
    
}