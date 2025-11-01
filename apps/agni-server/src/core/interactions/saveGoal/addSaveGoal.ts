import { GetUID } from "@core/adapters/libs";
import { Money } from "@core/domains/entities/money";
import { SaveGoal } from "@core/domains/entities/saveGoal";
import { IUsecase } from "../interfaces";
import { CreatedDto } from "@core/dto/base";
import SaveGoalItem from "@core/domains/valueObjects/saveGoalItem";
import Repository from "@core/adapters/repository";
import { Account } from "@core/domains/entities/account";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

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
    wishDueDate?: Date
    accountId?: string
    items: RequestAddItemSaveGoalUseCase[]
}


export class AddSaveGoalUseCase implements IUsecase<RequestAddSaveGoalUseCase, CreatedDto> {
    private savingRepository: Repository<SaveGoal>
    private accountRepository: Repository<Account>

    constructor(savingRepo: Repository<SaveGoal>, accountRepository: Repository<Account>) {
        this.savingRepository = savingRepo
        this.accountRepository = accountRepository
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

        if (request.accountId && (!await this.accountRepository.get(request.accountId)))
            throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

        let money = new Money(request.target)
        let newSaveGoal = new SaveGoal(
            GetUID(), 
            request.title, 
            money, 
            new Money(0), 
            request.desirValue,
            request.importance,
            request.wishDueDate,
            items, 
            request.accountId,
            request.description)

        await this.savingRepository.create(newSaveGoal)

        return { newId: newSaveGoal.getId()}
    }
}