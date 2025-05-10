import { GetUID } from "@core/adapters/libs";
import { Account } from "@core/domains/entities/account";
import { Money } from "@core/domains/entities/money";
import { SaveGoalItem, SaveGoal } from "@core/domains/entities/saveGoal";
import { SavingRepository } from "../../repositories/savingRepository";

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
    items: RequestAddItemSaveGoalUseCase[]
}

export interface IAddSaveGoalUseCase {
    execute(request: RequestAddSaveGoalUseCase): void
}

export interface IAddSaveGoalPresenter {
    success(newSavingId: string): void;
    fail(err: Error): void;
}

export class AddSaveGoalUseCase implements IAddSaveGoalUseCase {
    private savingRepository: SavingRepository
    private presenter: IAddSaveGoalPresenter

    constructor(savingRepo: SavingRepository, presenter: IAddSaveGoalPresenter) {
        this.savingRepository = savingRepo
        this.presenter = presenter
    }

    async execute(request: RequestAddSaveGoalUseCase): Promise<void> {
        try {
            let items: SaveGoalItem[] = []
            for(let itemRequest of request.items) {
                items.push({
                    id: GetUID(),
                    title: itemRequest.title,
                    link: itemRequest.link,
                    htmlToTrack: itemRequest.htmlToTrack,
                    price: new Money(itemRequest.price)
                })
            }

            let money = new Money(request.target)
            let newSaveGoal = new SaveGoal(GetUID(), request.title, money, new Money(0), items, request.description)
            
            await this.savingRepository.create(newSaveGoal)
            
            this.presenter.success(newSaveGoal.getId())
        } catch (err: any) {
            this.presenter.fail(err as Error);
        } 
    }
}