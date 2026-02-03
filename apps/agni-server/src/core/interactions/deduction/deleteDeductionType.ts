import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import { DeductionType } from "@core/domains/entities/decution";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";


export class DeleteDeductionTypeUseCase implements IUsecase<string, void> {
    private deductionTypeRepo: Repository<DeductionType>

    constructor(deductionTypeRepo: Repository<DeductionType>) {
        this.deductionTypeRepo = deductionTypeRepo 
    }

    async execute(request: string, trx?: any): Promise<void> {
        try {
            if (!await this.deductionTypeRepo.get(request))
                throw new ResourceNotFoundError("DEDUCTION_TYPE_NOT_FOUND")

            await this.deductionTypeRepo.delete(request)
        } catch(err) {
            throw err
        }
    }
}