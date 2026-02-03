import { ListDto, QueryFilter } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import { DeductionType } from "@core/domains/entities/decution";

export type GetAllDeductionTypeDto = {
    id: string
    title: string
    description: string
    base: string
    mode: string
}

export class GetAllDeductionTypeUseCase implements IUsecase<QueryFilter, ListDto<GetAllDeductionTypeDto> > {
    private deductionTypeRepo: Repository<DeductionType>

    constructor(deductionTypeRepo: Repository<DeductionType>) {
        this.deductionTypeRepo = deductionTypeRepo 
    }

    async execute(filter: QueryFilter, trx?: any): Promise<ListDto<GetAllDeductionTypeDto>> {
        try {
            const deductions = await this.deductionTypeRepo.getAll({ offset: filter.offset, limit: filter.limit }) 

            return {
                items: deductions.items.map(i => ({
                    id: i.getId(),
                    description: i.getDescription(),
                    title: i.getTitle(),
                    base: i.getBase(),
                    mode: i.getMode(),
                })),
                totals: deductions.total
            }
        } catch(err) {
            throw err
        }
    }
}