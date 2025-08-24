import { PatrimonyRepository, PatrimonySnapshotRepository } from "@core/repositories/patrimonyRepository";
import { IUsecase } from "../interfaces";
import { ListDto } from "@core/dto/base";

export type RequestAllSnapshotPatrimony = {
    patrimonyId: string
    startDate?: Date
    endDate?: Date
}

export type GetAllSnapshotPatrimonyDto = {
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
        const snapshots = await this.snapshotPatrimonyRepo.getAll({ 
            patrimonyIds: [request.patrimonyId],
            startDate: request.startDate,
            endDate: request.endDate
        });

        return { 
            items: snapshots.items.map(i => ({
                balance: i.getCurrentBalanceObserver(),
                date: i.getDate(),
                status: i.getStatus()
            })),
            totals: snapshots.total
        }
    }
    
}