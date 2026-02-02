import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import { Deduction, DeductionType } from "@core/domains/entities/decution";
import { QueryFilter } from "@core/dto/base";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export type GetDeductionDto = {
    id: string
    typeId: string
    typeName: string
    rate: number
}


export class GetDeductionUseCase implements IUsecase<string, GetDeductionDto> {
    private deductionRepo: Repository<Deduction>
    private deductionTypeRepo: Repository<DeductionType>

    constructor(deductionRepo: Repository<Deduction>, deductionTypeRepo: Repository<DeductionType>) {
        this.deductionTypeRepo = deductionTypeRepo 
        this.deductionRepo = deductionRepo
    }

    async execute(request: string, trx?: any): Promise<GetDeductionDto> {
        try {
            const deduction = await this.deductionRepo.get(request)
            if (!deduction)
                throw new ResourceNotFoundError("DEDUCTION_NOT_FOUND")

            const deductionType = await this.deductionTypeRepo.get(deduction.getId())

            return {
                id: deduction.getId(),
                rate: deduction.getRate(),
                typeId: deduction?.getDeductionTypeId(),
                typeName: deductionType?.getDescription() || 'Unknow'
            }
        } catch(err) {
            throw err
        }
    }
}