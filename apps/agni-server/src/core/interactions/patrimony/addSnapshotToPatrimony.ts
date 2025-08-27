import { PatrimonyRepository, PatrimonySnapshotRepository } from "@core/repositories/patrimonyRepository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { PatrimonySnapshot } from "@core/domains/entities/patrimonySnapshot";
import { CreatedDto } from "@core/dto/base";
import { GetUID } from "@core/adapters/libs";
import { mapperTransactionStatus, Period, TransactionType } from "@core/domains/constants";
import { MomentDateService } from "@core/domains/entities/libs";

export type RequestAddSnapshotPatrimony = {
    patrimonyId: string
    balance: number
    status: string
    date: Date
}

export class AddSnapshotPatrimonyUseCase implements IUsecase<RequestAddSnapshotPatrimony, CreatedDto> {
    private patrimonyRepo: PatrimonyRepository
    private snapshotRepo: PatrimonySnapshotRepository

    constructor(patrimonyRepo: PatrimonyRepository, snapshotRepo: PatrimonySnapshotRepository) {
        this.patrimonyRepo = patrimonyRepo
        this.snapshotRepo = snapshotRepo
    }

    async execute(request: RequestAddSnapshotPatrimony): Promise<CreatedDto> {
        if (! await this.patrimonyRepo.exist(request.patrimonyId))
            throw new ResourceNotFoundError("PATRIMONY_NO_FOUND")

        const {startDate, endDate} = MomentDateService.getUTCDateByPeriod(request.date, Period.MONTH, 1)

        const pastSnapshot = await this.snapshotRepo.getLastest({ 
            patrimonyIds: [request.patrimonyId], 
            startDate: startDate, endDate: endDate
        })

        // if (pastSnapshot.length > 0)
        //     throw new ResourceNotFoundError("PATRIMONY_HAVE_ALREADY_SNAPSHOT_FOR_THIS_DATE")

        const snapshot = new PatrimonySnapshot(GetUID(), request.patrimonyId, request.balance, 
            mapperTransactionStatus(request.status), request.date)

        await this.snapshotRepo.save(snapshot)

        return { newId: snapshot.getId() }
    }
}