import { CreatedDto } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import { Deduction, DeductionType } from "@core/domains/entities/decution";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { GetUID } from "@core/adapters/libs";
import { mapperDeductionBase, mapperDeductionMode } from "@core/domains/constants";

export type RequestCreateDeductionDto = {
    typeId: string 
    rate: number
}

export class CreateDeductionUseCase implements IUsecase<RequestCreateDeductionDto, CreatedDto> {
    private deductionTypeRepo: Repository<DeductionType>
    private deductionRepo: Repository<Deduction>

    constructor(deductionTypeRepo: Repository<DeductionType>, deductionRepo: Repository<Deduction>) {
        this.deductionTypeRepo = deductionTypeRepo 
        this.deductionRepo = deductionRepo
    }

    async execute(request: RequestCreateDeductionDto, trx?: any): Promise<CreatedDto> {
        try {
            if (!await this.deductionTypeRepo.get(request.typeId))
                throw new ResourceNotFoundError("DEDUCTION_TYPE_NOT_FOUND")

            const newDeduction = new Deduction(
                GetUID(), 
                request.typeId, 
                request.rate
            )

            await this.deductionRepo.create(newDeduction)

            return { newId: newDeduction.getId() }
        } catch(err) {
            throw err
        }
    }
}