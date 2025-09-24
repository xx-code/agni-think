import { Money } from "@core/domains/entities/money"
import { ValueError } from "@core/errors/valueError"
import { IUsecase } from "../interfaces"
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError"
import SaveGoalItem from "@core/domains/valueObjects/saveGoalItem"
import { ImportanceGoal, IntensityEmotionalDesir } from "@core/domains/constants"
import Repository from "@core/adapters/repository"
import { SaveGoal } from "@core/domains/entities/saveGoal"

export type RequestUpdateItemSaveGoalUseCase = {
    id: string,
    title: string,
    link: string,
    htmlToTrack: string
    price: number
}

export type RequestUpdateSaveGoalUseCase = {
    id: string
    target?: number
    title?: string
    desirValue?: IntensityEmotionalDesir,
    importance?: ImportanceGoal,
    wishDueDate?: Date,
    description?: string
    items?: RequestUpdateItemSaveGoalUseCase[]
}

export class UpdateSaveGoalUseCase implements IUsecase<RequestUpdateSaveGoalUseCase, void> {
    private savingRepo: Repository<SaveGoal>

    constructor(savingRepo: Repository<SaveGoal>) {
        this.savingRepo = savingRepo
    }

    async execute(request: RequestUpdateSaveGoalUseCase): Promise<void> {
        let saveGoal = await this.savingRepo.get(request.id)
        if (saveGoal === null)
            throw new ResourceNotFoundError("SAVE_GAOL_NOT_FOUND")

        if (request.title)
            saveGoal.setTitle(request.title)
        
        if (request.description)
            saveGoal.setDescription(request.description)

        if (request.target) {
            let target = new Money(request.target)

            if (target.getAmount() < saveGoal.getBalance().getAmount())
                throw new ValueError("You can't have a save goal target less than balance")

            saveGoal.setTarget(target)
        } 

        if (request.desirValue !== undefined)
        {
            saveGoal.setDesirValue(request.desirValue)
        }

        if (request.importance !== undefined) {
            saveGoal.setImportance(request.importance)
        }

        saveGoal.setWishDueDate(request.wishDueDate) 

        if (request.items) {
            let items: SaveGoalItem[] = [];
            for(let itemRequest of request.items) {
                const item = new SaveGoalItem()
                item.title = itemRequest.title
                item.link = itemRequest.link
                item.price = new Money(itemRequest.price) 
                item.htmlToTrack = itemRequest.htmlToTrack
            };
            saveGoal.setItems(items);
        }
        
        if (saveGoal.hasChange())
            await this.savingRepo.update(saveGoal)
    }
}