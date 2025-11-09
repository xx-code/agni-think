import Repository from "@core/adapters/repository";
import { IUsecase } from "../interfaces";
import { Holding } from "@core/domains/entities/holding";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export class DeleteHoldingUseCase implements IUsecase<string, void> {
    private hodlingRepo: Repository<Holding>

    constructor(holdingRepo: Repository<Holding>) {
        this.hodlingRepo = holdingRepo
    }

    async execute(request: string): Promise<void> {
        if (!await this.hodlingRepo.get(request))
            throw new ResourceNotFoundError("HOLDING_NOT_FOUND")

        await this.hodlingRepo.delete(request)
    }
}