import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import { DeductionType } from "@core/domains/entities/decution";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";


export class DeleteDeductionUseCase implements IUsecase<string, void> {
    private deductionRepo: Repository<DeductionType>

    constructor(deductionTypeRepo: Repository<DeductionType>) {
        this.deductionRepo = deductionTypeRepo 
    }

    async execute(request: string, trx?: any): Promise<void> {
        try {
            if (!await this.deductionRepo.get(request))
                throw new ResourceNotFoundError("DEDUCTION_NOT_FOUND")

            await this.deductionRepo.delete(request)

        } catch(err) {
            throw err
        }
    }
}