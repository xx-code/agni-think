import { PatrimonySnapshotRepository } from "@core/repositories/patrimonyRepository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { mapperTransactionStatus } from "@core/domains/constants";

export type RequestUpdateSnapshotPatrimony = {
    snapshotId: string
    patrimonyId?: string
    balance?: number
    status?: string
    date?: Date
}

export class UpdateSnapshotPatrimonyUseCase implements IUsecase<RequestUpdateSnapshotPatrimony, void> {
    private snapshotRepo: PatrimonySnapshotRepository 

    constructor(snapshotRepo: PatrimonySnapshotRepository )  {
        this.snapshotRepo = snapshotRepo
    }

    async execute(request: RequestUpdateSnapshotPatrimony): Promise<void> {
        let snapshot = await this.snapshotRepo.get(request.snapshotId)
        if (!snapshot)
            throw new ResourceNotFoundError("SNAPSHOT_PATRIMONY_NOT_FOUND")

        if (request.patrimonyId)
            snapshot.setPatrimonyId(request.patrimonyId)

        if (request.balance)
            snapshot.setCurrentBalanceObserver(request.balance)

        if (request.date)
            snapshot.setDate(request.date)

        if (request.status)
            snapshot.setStatus(mapperTransactionStatus(request.status))

        if (snapshot.hasChange())
            await this.snapshotRepo.update(snapshot)
    }

}