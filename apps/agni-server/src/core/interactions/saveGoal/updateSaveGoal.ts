import { Money } from "@core/domains/entities/money"
import { ValueError } from "@core/errors/valueError"
import { SavingRepository } from "@core/repositories/savingRepository"
import { IUsecase } from "../interfaces"
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError"
import SaveGoalItem from "@core/domains/valueObjects/saveGoalItem"

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
    description?: string
    items?: RequestUpdateItemSaveGoalUseCase[]
}

export class UpdateSaveGoalUseCase implements IUsecase<RequestUpdateSaveGoalUseCase, void> {
    private savingRepo: SavingRepository

    constructor(savingRepo: SavingRepository) {
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