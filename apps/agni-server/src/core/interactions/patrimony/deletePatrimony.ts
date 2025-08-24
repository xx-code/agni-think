import { PatrimonyRepository } from "@core/repositories/patrimonyRepository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";

export default class DeletePatrimonyUseCase implements IUsecase<string, void> {
    private patrimonyRepo: PatrimonyRepository
    private unitOfWorkRepo: UnitOfWorkRepository

    constructor(patrimonyRepo: PatrimonyRepository, unitOfWorkRepo: UnitOfWorkRepository) {
        this.patrimonyRepo = patrimonyRepo
        this.unitOfWorkRepo = unitOfWorkRepo
    }

    async execute(patrimonyId: string): Promise<void> {
        try {
            this.unitOfWorkRepo.start()

            if (!await this.patrimonyRepo.exist(patrimonyId))
                throw new ResourceNotFoundError("PATRIMONY_NOT_FOUND")

            await this.patrimonyRepo.delete(patrimonyId)

            this.unitOfWorkRepo.commit()
        } catch(err) {
            this.unitOfWorkRepo.rollback()
            throw err;
        } 
    }
}