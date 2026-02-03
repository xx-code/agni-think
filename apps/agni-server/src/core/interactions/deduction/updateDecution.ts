import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import { Deduction, DeductionType } from "@core/domains/entities/decution";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export type RequestUpdateDeductionDto = {
    id: string
    rate?: number
}

export class UpdateDeductionUseCase implements IUsecase<RequestUpdateDeductionDto, void> {
    private deductionRepo: Repository<Deduction>

    constructor(deductionRepo: Repository<Deduction>) {
        this.deductionRepo = deductionRepo
    }

    async execute(request: RequestUpdateDeductionDto, trx?: any): Promise<void> {
        try {
            const deduction = await this.deductionRepo.get(request.id)
            if (!deduction)
                throw new ResourceNotFoundError("DEDUCTION_NOT_FOUND")

            if (request.rate)
                deduction.setRate(request.rate)

            await this.deductionRepo.update(deduction)

        } catch(err) {
            throw err
        }
    }
}