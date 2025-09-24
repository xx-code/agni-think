import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError"
import { IUsecase } from "../interfaces"
import Repository from "@core/adapters/repository"
import { SaveGoal } from "@core/domains/entities/saveGoal"

export type GetSaveGoalItemDto = {
    title: string
    link: string
    price: number
    htmlToTrack: string
}

export type GetSaveGoalDto = {
    id: string,
    title: string,
    description: string,
    target: number,
    balance: number
    desirValue: number
    importance: number
    wishDueDate?: Date
    items: GetSaveGoalItemDto[] 
}

export class GetSaveGoalUseCase implements IUsecase<string, GetSaveGoalDto> {
    private savingRepository: Repository<SaveGoal>

    constructor(savingRepository: Repository<SaveGoal>) {
        this.savingRepository = savingRepository
    }

    async execute(id: string): Promise<GetSaveGoalDto> {
        let saveGoal = await this.savingRepository.get(id)

        if (saveGoal === null)
            throw new ResourceNotFoundError("SAVE_GAOL_NOT_FOUND")

        let response: GetSaveGoalDto = {
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

        return response
    }
}