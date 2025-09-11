import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { PatrimonySnapshot } from "@core/domains/entities/patrimonySnapshot";
import { CreatedDto } from "@core/dto/base";
import { GetUID } from "@core/adapters/libs";
import { mapperTransactionStatus } from "@core/domains/constants";
import Repository from "@core/adapters/repository";
import { Patrimony } from "@core/domains/entities/patrimony";

export type RequestAddSnapshotPatrimony = {
    patrimonyId: string
    balance: number
    status: string
    date: Date
}

export class AddSnapshotPatrimonyUseCase implements IUsecase<RequestAddSnapshotPatrimony, CreatedDto> {
    private patrimonyRepo: Repository<Patrimony>
    private snapshotRepo: Repository<PatrimonySnapshot>

    constructor(patrimonyRepo: Repository<Patrimony>, snapshotRepo: Repository<PatrimonySnapshot>) {
        this.patrimonyRepo = patrimonyRepo
        this.snapshotRepo = snapshotRepo
    }

    async execute(request: RequestAddSnapshotPatrimony): Promise<CreatedDto> {
        if (!(await this.patrimonyRepo.get(request.patrimonyId)))
            throw new ResourceNotFoundError("PATRIMONY_NO_FOUND")

        const snapshot = new PatrimonySnapshot(GetUID(), request.patrimonyId, request.balance, 
            mapperTransactionStatus(request.status), request.date)

        await this.snapshotRepo.create(snapshot)

        return { newId: snapshot.getId() }
    }
}