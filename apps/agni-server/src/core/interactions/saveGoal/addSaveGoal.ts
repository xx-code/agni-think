import { GetUID } from "@core/adapters/libs";
import { Money } from "@core/domains/entities/money";
import { SaveGoal } from "@core/domains/entities/saveGoal";
import { SavingRepository } from "../../repositories/savingRepository";
import { IUsecase } from "../interfaces";
import { CreatedDto } from "@core/dto/base";
import SaveGoalItem from "@core/domains/valueObjects/saveGoalItem";
import { MomentDateService } from "@core/domains/entities/libs";

export type RequestAddItemSaveGoalUseCase = {
    title: string
    price: number
    link: string 
    htmlToTrack: string
}
export type RequestAddSaveGoalUseCase = {
    target: number;
    title: string;
    description: string
    desirValue: number
    importance: number
    wishDueDate?: string
    items: RequestAddItemSaveGoalUseCase[]
}


export class AddSaveGoalUseCase implements IUsecase<RequestAddSaveGoalUseCase, CreatedDto> {
    private savingRepository: SavingRepository

    constructor(savingRepo: SavingRepository) {
        this.savingRepository = savingRepo
    }

    async execute(request: RequestAddSaveGoalUseCase): Promise<CreatedDto> {
        let items: SaveGoalItem[] = []

        for(let itemRequest of request.items) {
            var item = new SaveGoalItem()
            item.title = itemRequest.title
            item.htmlToTrack = itemRequest.htmlToTrack
            item.link = itemRequest.link
            item.price = new Money(itemRequest.price) 

            items.push(item)
        }

        let money = new Money(request.target)
        let newSaveGoal = new SaveGoal(
            GetUID(), 
            request.title, 
            money, 
            new Money(0), 
            request.desirValue,
            request.importance,
            request.wishDueDate?MomentDateService.formatDate(request.wishDueDate).toISOString():undefined,
            items, request.description)

        await this.savingRepository.create(newSaveGoal)

        return { newId: newSaveGoal.getId()}
    }
}