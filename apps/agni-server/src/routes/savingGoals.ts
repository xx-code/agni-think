import { IDeleteSaveGaolAdapter } from "@core/interactions/saveGoal/deleteSaveGoal";
import { Router } from "express";
import { diContainer } from "src";
import { ApiCreateSavingGoalController, ApiDecreaseSavingGoalController, ApiDeleteSavingGoalController, ApiGetAllSavingGoalController, ApiGetSavingGoalController, ApiIncreaseSavingGoalController, ApiUpdateSavingGoalController } from "src/controllers/saveGoals";

const router = Router()

let deleteSavingGoalAdapter: IDeleteSaveGaolAdapter = {
    dateService: diContainer.dateService!,
    transactionRepository: diContainer.transactionRepository!,
    accountRepository: diContainer.accountRepository!,
    savingRepository: diContainer.savingRepository!,
    recordRepository: diContainer.recordRepository!,
    unitOfWorkRepository: diContainer.unitOfWork!
}

router.post('/v1/save-goals', async (req, res) => {
    await (new ApiCreateSavingGoalController(diContainer.savingRepository!)).execute(req, res)    
})

router.get('/v1/save-goals', async (req, res) => {
    await (new ApiGetAllSavingGoalController(diContainer.savingRepository!)).execute(req, res)    
})

router.get('/v1/save-goals/:id', async (req, res) => {
    await (new ApiGetSavingGoalController(diContainer.savingRepository!)).execute(req, res)    
})

router.put('/v1/save-goals/:id', async (req, res) => {
    await (new ApiUpdateSavingGoalController(diContainer.savingRepository!)).execute(req, res)    
})

router.delete('/v1/save-goals/:id', async (req, res) => {
    await (new ApiDeleteSavingGoalController(deleteSavingGoalAdapter)).execute(req, res)    
})

router.patch('/v1/save-goals/:id/increase-balance', async (req, res) => {
    await (new ApiIncreaseSavingGoalController(diContainer.categoryRepository!, diContainer.savingRepository!, diContainer.accountRepository!, diContainer.transactionRepository!, diContainer.dateService!, diContainer.recordRepository!, diContainer.unitOfWork!)).execute(req, res)   
})

router.patch('/v1/save-goals/:id/decrease-balance', async (req, res) => {
    await (new ApiDecreaseSavingGoalController(diContainer.categoryRepository!, diContainer.savingRepository!, diContainer.accountRepository!, diContainer.transactionRepository!, diContainer.dateService!, diContainer.recordRepository!, diContainer.unitOfWork!)).execute(req, res)  
})

export default router