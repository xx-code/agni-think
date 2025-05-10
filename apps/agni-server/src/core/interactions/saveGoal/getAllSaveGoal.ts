import { SavingRepository } from "../../repositories/savingRepository"

export interface IGetAllSaveGoalUseCase {
    execute(): void
}

export type SaveGoalItemResponse = {
    id: string
    title: string
    price: number
    link: string
    htmlToTrack: string
}

export type SaveGoalsResponse = {
    id: string,
    title: string,
    description: string,
    target: number,
    balance: number,
    items: SaveGoalItemResponse[]
}

export interface IGetAllSaveGoalPresenter {
    success(response: SaveGoalsResponse[]): void;
    fail(err: Error): void;
}

export class GetAllSaveGoalUseCase implements IGetAllSaveGoalUseCase {
    private savingRepository: SavingRepository
    private presenter: IGetAllSaveGoalPresenter

    constructor(presenter: IGetAllSaveGoalPresenter, savingRepo: SavingRepository) {
        this.presenter = presenter
        this.savingRepository = savingRepo
    }

    async execute(): Promise<void> {
        try {
            let saveGoals = await this.savingRepository.getAll()

            let responses: SaveGoalsResponse[] = [] 

            for(let saveGoal of saveGoals) {

                responses.push(
                    {
                        id: saveGoal.getId(),
                        title: saveGoal.getTitle(),
                        description: saveGoal.getDescription(),
                        balance: saveGoal.getBalance().getAmount(),
                        target: saveGoal.getTarget().getAmount(),
                        items: saveGoal.getItems().map(item => ({id: item.id, title: item.title, link: item.link, price: item.price.getAmount(), htmlToTrack: item.htmlToTrack}))
                    }
                ) 
            }

            this.presenter.success(responses)
        } catch(err: any) {
            this.presenter.fail(err)
        }
    }
}