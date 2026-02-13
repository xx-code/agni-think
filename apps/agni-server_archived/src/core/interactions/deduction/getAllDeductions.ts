import { ListDto, QueryFilter } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import { Deduction, DeductionType } from "@core/domains/entities/decution";

export type GetAllDeductionDto = {
    id: string
    typeId: string
    typeName: string
    rate: number
}

export class GetAllDeductionUseCase implements IUsecase<QueryFilter, ListDto<GetAllDeductionDto> > {
    private deductionTypeRepo: Repository<DeductionType>
    private deductionRepo: Repository<Deduction>

    constructor(deductionRepo: Repository<Deduction>, deductionTypeRepo: Repository<DeductionType>) {
        this.deductionTypeRepo = deductionTypeRepo 
        this.deductionRepo = deductionRepo
    }

    async execute(filter: QueryFilter, trx?: any): Promise<ListDto<GetAllDeductionDto>> {
        try {

            const deductions = await this.deductionRepo.getAll({ offset: filter.offset, limit: filter.limit }) 
            const deductionTypes = await this.deductionTypeRepo.getManyByIds(deductions.items.map(i => i.getDeductionTypeId()))

            return {
                items: deductions.items.map(i => ({
                    id: i.getId(), 
                    rate: i.getRate(),
                    typeId: i.getDeductionTypeId(),
                    typeName: deductionTypes.find(d => d.getId() === i.getDeductionTypeId())?.getTitle() || 'Unknow'
                })),
                totals: deductions.total
            }    
        } catch(err) {
            throw err
        }
    }
}