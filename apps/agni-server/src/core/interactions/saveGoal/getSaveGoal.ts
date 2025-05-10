import { SavingRepository } from "../../repositories/savingRepository"
import { TransactionRepository } from "../../repositories/transactionRepository"

export type SaveGoalItemResponse = {
    id: string
    title: string
    link: string
    price: number
    htmlToTrack: string
}

export type SaveGoalResponse = {
    id: string,
    title: string,
    description: string,
    target: number,
    balance: number
    items: SaveGoalItemResponse[] 
}


export interface IGetSaveGoalUseCase {
    execute(id: string): void
}

export interface IGetSaveGoalPresenter {
    success(response: SaveGoalResponse): void;
    fail(err: Error): void;
}

export class GetSaveGoalUseCase implements IGetSaveGoalUseCase {
    private savingRepository: SavingRepository
    private presenter: IGetSaveGoalPresenter

    constructor(presenter: IGetSaveGoalPresenter, savingRepository: SavingRepository) {
        this.presenter = presenter
        this.savingRepository = savingRepository
    }

    async execute(id: string): Promise<void> {
        try {
            let saveGoal = await this.savingRepository.get(id)

            let response: SaveGoalResponse = {
                id: saveGoal.getId(),
                title: saveGoal.getTitle(),
                description: saveGoal.getDescription(),
                balance: saveGoal.getBalance().getAmount(),
                target: saveGoal.getTarget().getAmount(),
                items: saveGoal.getItems().map(item => ({id: item.id, title: item.title, link: item.link, price: item.price.getAmount(), htmlToTrack: item.htmlToTrack}))
            }

            this.presenter.success(response)
        } catch(err: any) {
            this.presenter.fail(err)
        }
    }
}