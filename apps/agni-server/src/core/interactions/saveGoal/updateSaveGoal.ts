import { GetUID } from "@core/adapters/libs"
import { Money } from "@core/domains/entities/money"
import { SaveGoalItem } from "@core/domains/entities/saveGoal"
import { SavingRepository } from "@core/repositories/savingRepository"

export type RequestUpdateItemSaveGoalUseCase = {
    id: string,
    title: string,
    link: string,
    htmlToTrack: string
    price: number
}

export type RequestUpdateSaveGoalUseCase = {
    savingGoalRef: string
    target: number
    title: string
    description: string
    items: RequestUpdateItemSaveGoalUseCase[]
}

export interface IUpdateSaveGoalUseCase {
    execute(request: RequestUpdateSaveGoalUseCase): void
}

export interface IUpdateSaveGoalPresenter {
    success(isSave: boolean): void;
    fail(err: Error): void;
}

export class UpdateSaveGoalUseCase implements IUpdateSaveGoalUseCase {
    private savingRepo: SavingRepository
    private presenter: IUpdateSaveGoalPresenter

    constructor(presenter: IUpdateSaveGoalPresenter, savingRepo: SavingRepository) {
        this.presenter = presenter
        this.savingRepo = savingRepo
    }

    async execute(request: RequestUpdateSaveGoalUseCase): Promise<void> {
        try {
            let saveGoal = await this.savingRepo.get(request.savingGoalRef)

            saveGoal.setTitle(request.title)
            saveGoal.setDescription(request.description)
            let target = new Money(request.target)
            saveGoal.setTarget(target)

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
            saveGoal.setItems(items)
            
            if (saveGoal.hasChange())
                await this.savingRepo.update(saveGoal)

            this.presenter.success(true)
        } catch(err: any) {
            this.presenter.fail(err)
        }
    }
}