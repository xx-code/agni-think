import Repository from "@core/adapters/repository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { PatrimonySnapshot } from "@core/domains/entities/patrimonySnapshot";


export class RemoveSnapshotFromPatrimonyUseCase implements IUsecase<string, void> {
    private snapshotRepo: Repository<PatrimonySnapshot>

    constructor(snapshotRepo: Repository<PatrimonySnapshot>) {
        this.snapshotRepo = snapshotRepo
    }

    async execute(id: string): Promise<void> {
        if (!await this.snapshotRepo.get(id))
            throw new ResourceNotFoundError("SNAPSHOT_PATRIMONY_NOT_FOUND")

        await this.snapshotRepo.delete(id)
    }

}