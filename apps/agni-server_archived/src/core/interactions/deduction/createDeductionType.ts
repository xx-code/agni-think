import { CreatedDto } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import { DeductionType } from "@core/domains/entities/decution";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { GetUID } from "@core/adapters/libs";
import { mapperDeductionBase, mapperDeductionMode } from "@core/domains/constants";

export type RequestCreateDeductionTypeDto = {
    title: string
    description: string
    base: string
    mode: string
}

export class CreateDeductionTypeUseCase implements IUsecase<RequestCreateDeductionTypeDto, CreatedDto> {
    private deductionTypeRepo: Repository<DeductionType>

    constructor(deductionTypeRepo: Repository<DeductionType>) {
        this.deductionTypeRepo = deductionTypeRepo 
    }

    async execute(request: RequestCreateDeductionTypeDto, trx?: any): Promise<CreatedDto> {
        try {
            if (await this.deductionTypeRepo.existByName(request.description))
                throw new ResourceAlreadyExist("DEUCTION_ALREADY_EXIST")

            const newDeductionType = new DeductionType(
                GetUID(), 
                request.title, 
                request.description,
                mapperDeductionBase(request.base),
                mapperDeductionMode(request.mode),
            )

            await this.deductionTypeRepo.create(newDeductionType)

            return { newId:  newDeductionType.getId()}
        } catch(err) {
            throw err
        }
    }
}