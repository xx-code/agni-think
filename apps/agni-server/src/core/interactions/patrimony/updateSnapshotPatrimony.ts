import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { mapperTransactionStatus, Period } from "@core/domains/constants";
import { MomentDateService } from "@core/domains/entities/libs";
import Repository from "@core/adapters/repository";
import { PatrimonySnapshot } from "@core/domains/entities/patrimonySnapshot";

export type RequestUpdateSnapshotPatrimony = {
    snapshotId: string
    patrimonyId?: string
    balance?: number
    status?: string
    date?: Date
}

export class UpdateSnapshotPatrimonyUseCase implements IUsecase<RequestUpdateSnapshotPatrimony, void> {
    private snapshotRepo: Repository<PatrimonySnapshot> 

    constructor(snapshotRepo: Repository<PatrimonySnapshot> )  {
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

        if (snapshot.getDate() !== request.date) {
            const {startDate, endDate} = MomentDateService.getUTCDateByPeriod(snapshot.getDate(), Period.MONTH, 1)
            
            // if (pastSnapshot.length > 0)
            //     throw new ResourceNotFoundError("PATRIMONY_HAVE_ALREADY_SNAPSHOT_FOR_THIS_DATE")
        }   

        if (snapshot.hasChange())
            await this.snapshotRepo.update(snapshot)
    }

}