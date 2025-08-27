import { PatrimonySnapshotRepository } from "@core/repositories/patrimonyRepository";
import { IUsecase } from "../interfaces";
import { ListDto } from "@core/dto/base";
import { MomentDateService } from "@core/domains/entities/libs";
import { mapperPeriod } from "@core/domains/constants";

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
    private snapshotPatrimonyRepo: PatrimonySnapshotRepository

    constructor(
        snapshotPatrimonyRepo: PatrimonySnapshotRepository) {
            this.snapshotPatrimonyRepo = snapshotPatrimonyRepo
    }

    async execute(request: RequestAllSnapshotPatrimony): Promise<ListDto<GetAllSnapshotPatrimonyDto>> {
        const period = mapperPeriod(request.period)
        const beginDate = MomentDateService.getUTCDateSubstraction(new Date(), period, request.periodTime)
        const { startDate, endDate } = MomentDateService.getUTCDateByPeriod(beginDate, period, request.periodTime)

        const snapshots = await this.snapshotPatrimonyRepo.getAll({ 
            patrimonyIds: [request.patrimonyId],
            startDate: startDate,
            endDate: new Date()
        });

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