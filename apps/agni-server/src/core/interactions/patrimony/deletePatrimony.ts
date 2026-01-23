import Repository from "@core/adapters/repository";
import { IUsecase } from "../interfaces";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { Patrimony } from "@core/domains/entities/patrimony";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export default class DeletePatrimonyUseCase implements IUsecase<string, void> {
    private patrimonyRepo: Repository<Patrimony>
    private unitOfWorkRepo: UnitOfWorkRepository

    constructor(patrimonyRepo: Repository<Patrimony>, unitOfWorkRepo: UnitOfWorkRepository) {
        this.patrimonyRepo = patrimonyRepo
        this.unitOfWorkRepo = unitOfWorkRepo
    }

    async execute(patrimonyId: string): Promise<void> {
        try {
            const trx = await this.unitOfWorkRepo.start()

            if (!await this.patrimonyRepo.get(patrimonyId))
                throw new ResourceNotFoundError("PATRIMONY_NOT_FOUND")

            await this.patrimonyRepo.delete(patrimonyId, trx)

            await this.unitOfWorkRepo.commit()
        } catch(err) {
            await this.unitOfWorkRepo.rollback()
            throw err;
        } 
    }
}