import { ListDto, QueryAllFetch } from "@core/dto/base"
import { SavingRepository } from "../../repositories/savingRepository"
import { IUsecase } from "../interfaces"
import { BadRequestError } from "openai"
import UnExpectedError from "@core/errors/unExpectedError"
import { SortBy } from "@core/repositories/dto"

export type GetAllSaveGoalItemDto = {
    title: string
    price: number
    link: string
    htmlToTrack: string
}

export type GetAllSaveGoalDto = {
    id: string,
    title: string,
    description: string,
    target: number,
    balance: number,
    desirValue: number
    importance: number
    wishDueDate?: Date
    items: GetAllSaveGoalItemDto[]
}

export type QueryFilterSaveGoal = QueryAllFetch & {
    orderBy? : string 
    sortSense? : 'asc' | 'desc'
}

export class GetAllSaveGoalUseCase implements IUsecase<QueryFilterSaveGoal, ListDto<GetAllSaveGoalDto>> {
    private savingRepository: SavingRepository

    constructor(savingRepo: SavingRepository) {
        this.savingRepository = savingRepo
    }

    async execute(request: QueryFilterSaveGoal): Promise<ListDto<GetAllSaveGoalDto>> {

        if (request.orderBy && !['target', 'balance'].includes(request.orderBy))
            throw new UnExpectedError("ORDER_BY_MUST_BE_TARGERT_OR_BALANCE")

        if (request.orderBy && !request.sortSense)
            throw new UnExpectedError("YOU_HAVE_TO_CHOOSE_SENSE")

        let saveGoals = await this.savingRepository.getAll({
            offset: request.offset,
            limit: request.limit,
            queryAll: request.queryAll,
            sort: request.offset && request.sortSense ? {
                sortBy: request.orderBy as SortBy['sortBy'],
                asc: request.sortSense === 'asc' ? true : false
            } satisfies SortBy : undefined
        })

        let responses: GetAllSaveGoalDto[] = [] 

        for(let saveGoal of saveGoals.items) {

            responses.push(
                {
                    id: saveGoal.getId(),
                    title: saveGoal.getTitle(),
                    description: saveGoal.getDescription(),
                    balance: saveGoal.getBalance().getAmount(),
                    target: saveGoal.getTarget().getAmount(),
                    desirValue: saveGoal.getDesirValue(),
                    importance: saveGoal.getImportance(),
                    wishDueDate: saveGoal.getWishDueDate(),
                    items: saveGoal.getItems().map(item => ({title: item.title, link: item.link, price: item.price.getAmount(), htmlToTrack: item.htmlToTrack}))
                }
            ) 
        }

        return { items: responses, totals: saveGoals.total}
    }
}