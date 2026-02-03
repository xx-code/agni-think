import { CreatedDto } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import { DeductionType } from "@core/domains/entities/decution";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export type GetDeductionTypeDto = {
    id: string
    title: string
    description: string
    base: string
    mode: string
}

export class GetDeductionTypeUseCase implements IUsecase<string, GetDeductionTypeDto> {
    private deductionTypeRepo: Repository<DeductionType>

    constructor(deductionTypeRepo: Repository<DeductionType>) {
        this.deductionTypeRepo = deductionTypeRepo 
    }

    async execute(request: string, trx?: any): Promise<GetDeductionTypeDto> {
        try {
            const deductionType = await this.deductionTypeRepo.get(request)
            if (!deductionType)
                throw new ResourceNotFoundError("DEDUCTION_NOT_FOUND")

            return {
                id: deductionType.getId(),
                description: deductionType.getDescription(),
                title: deductionType.getTitle(),
                base: deductionType.getBase(),
                mode: deductionType.getMode()
            }
        } catch(err) {
            throw err
        }
    }
}