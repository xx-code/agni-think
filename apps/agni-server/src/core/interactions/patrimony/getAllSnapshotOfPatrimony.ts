import { IUsecase } from "../interfaces";
import { ListDto } from "@core/dto/base";
import { MomentDateService } from "@core/domains/entities/libs";
import { mapperPeriod } from "@core/domains/constants";
import Repository, { PatrimonySnapshotFilter } from "@core/adapters/repository";
import { PatrimonySnapshot } from "@core/domains/entities/patrimonySnapshot";

export type RequestAllSnapshotPatrimony = {
    patrimonyId: string
    period: string
    periodTime: number
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
        const period = mapperPeriod(request.period)
        const beginDate = MomentDateService.getUTCDateSubstraction(new Date(), period, request.periodTime)
        const { startDate, endDate } = MomentDateService.getUTCDateByPeriod(beginDate, period, request.periodTime)

        const extendFilter = new PatrimonySnapshotFilter()
        extendFilter.startDate = startDate
        extendFilter.endDate = new Date()
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