import { ListDto } from "@core/dto/base"
import { SavingRepository } from "../../repositories/savingRepository"
import { IUsecase } from "../interfaces"

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

export class GetAllSaveGoalUseCase implements IUsecase<void, ListDto<GetAllSaveGoalDto>> {
    private savingRepository: SavingRepository

    constructor(savingRepo: SavingRepository) {
        this.savingRepository = savingRepo
    }

    async execute(): Promise<ListDto<GetAllSaveGoalDto>> {
        let saveGoals = await this.savingRepository.getAll()

        let responses: GetAllSaveGoalDto[] = [] 

        for(let saveGoal of saveGoals) {

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

        return { items: responses, totals: responses.length}
    }
}