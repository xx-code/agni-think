import { PatrimonySnapshotRepository } from "@core/repositories/patrimonyRepository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";


export class RemoveSnapshotFromPatrimonyUseCase implements IUsecase<string, void> {
    private snapshotRepo: PatrimonySnapshotRepository

    constructor(snapshotRepo: PatrimonySnapshotRepository) {
        this.snapshotRepo = snapshotRepo
    }

    async execute(id: string): Promise<void> {
        if (!await this.snapshotRepo.exist(id))
            throw new ResourceNotFoundError("SNAPSHOT_PATRIMONY_NOT_FOUND")

        await this.snapshotRepo.delete(id)
    }

}