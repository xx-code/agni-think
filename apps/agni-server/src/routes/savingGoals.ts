import { IDeleteSaveGaolAdapter } from "@core/interactions/saveGoal/deleteSaveGoal";
import { Router } from "express";
import { ApiCreateSavingGoalController, ApiDecreaseSavingGoalController, ApiDeleteSavingGoalController, ApiGetAllSavingGoalController, ApiGetSavingGoalController, ApiIncreaseSavingGoalController, ApiUpdateSavingGoalController } from "src/controllers/saveGoals";
import container from '../di_contenair';

const router = Router()



router.post('/v1/save-goals', async (req, res) => {
    await (new ApiCreateSavingGoalController(container.getRepository('saving'))).execute(req, res)    
})

router.get('/v1/save-goals', async (req, res) => {
    await (new ApiGetAllSavingGoalController(container.getRepository('saving'))).execute(req, res)    
})

router.get('/v1/save-goals/:id', async (req, res) => {
    await (new ApiGetSavingGoalController(container.getRepository('saving'))).execute(req, res)    
})

router.put('/v1/save-goals/:id', async (req, res) => {
    await (new ApiUpdateSavingGoalController(container.getRepository('saving'))).execute(req, res)    
})

router.delete('/v1/save-goals/:id', async (req, res) => {
    let deleteSavingGoalAdapter: IDeleteSaveGaolAdapter = {
        dateService: container.getService('date_service'),
        transactionRepository: container.getRepository('transaction'),
        accountRepository: container.getRepository('account'),
        savingRepository: container.getRepository('saving'),
        recordRepository: container.getRepository('record'),
        unitOfWorkRepository: container.getRepository('unit_of_work')
    }
    await (new ApiDeleteSavingGoalController(deleteSavingGoalAdapter)).execute(req, res)    
})

router.patch('/v1/save-goals/:id/increase-balance', async (req, res) => {
    await (new ApiIncreaseSavingGoalController(container.getRepository('category'), container.getRepository('saving'), container.getRepository('account'), container.getRepository('transaction'), container.getService('date_service'), container.getRepository('record'), container.getRepository('unit_of_work'))).execute(req, res)   
})

router.patch('/v1/save-goals/:id/decrease-balance', async (req, res) => {
    await (new ApiDecreaseSavingGoalController(container.getRepository('category'), container.getRepository('saving'), container.getRepository('account'), container.getRepository('transaction'), container.getService('date_service'), container.getRepository('record'), container.getRepository('unit_of_work'))).execute(req, res)  
})

export default router