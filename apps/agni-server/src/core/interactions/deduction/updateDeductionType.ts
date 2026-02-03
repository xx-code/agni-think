import { CreatedDto } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import { DeductionType } from "@core/domains/entities/decution";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export type RequestUpdateDeductionTypeDto = {
    id: string
    title?: string
    description?: string
}

export class UpdateDeductionTypeUseCase implements IUsecase<RequestUpdateDeductionTypeDto, void> {
    private deductionTypeRepo: Repository<DeductionType>

    constructor(deductionTypeRepo: Repository<DeductionType>) {
        this.deductionTypeRepo = deductionTypeRepo 
    }

    async execute(request: RequestUpdateDeductionTypeDto, trx?: any): Promise<void> {
        try {
            const deductionType = await this.deductionTypeRepo.get(request.id)
            if (!deductionType)
                throw new ResourceNotFoundError("DEDUCTION_TYPE_NOT_FOUND")

            if (request.title)
                deductionType.setTitle(request.title)

            if (request.description)
                deductionType.setDescription(request.description)

            if (deductionType.hasChange())
            {
                await this.deductionTypeRepo.update(deductionType)
            }

        } catch(err) {
            console.log(err)
            throw err        
        }
    }
}