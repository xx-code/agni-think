import { ICreationBudgetAdapter } from "@core/interactions/budgets/creationBudgetUseCase";
import { IGetAllBudgetAdapter } from "@core/interactions/budgets/getAllBudgetUseCase";
import { IGetBudgetAdpater } from "@core/interactions/budgets/getBudgetUseCase";
import { IUpdateBudgetAdapter } from "@core/interactions/budgets/updateBudgetUseCase";
import { Router } from "express";
import { ApiAutoBudgetController, ApiCreateBudgetController, ApiDeleteBudgetController, ApiGetAllBudgetController, ApiGetBudgetController, ApiUpdateBudgetController } from "src/controllers/budgets";
import container from '../di_contenair'

const router = Router()



router.post('/v1/budgets', async (req, res) => {
    let createBudgetAdapter: ICreationBudgetAdapter = {
        budgetRepository: container.getRepository('budget'),
        categoryRepository: container.getRepository('category'),
        tagRepository: container.getRepository('tag'),
        dateService: container.getService('date_service')
    }

    await (new ApiCreateBudgetController(createBudgetAdapter, container.getService('date_service'))).execute(req, res)
})

router.get('/v1/budgets', async (req, res) => {
    let getAllBudgetAdapter: IGetAllBudgetAdapter = {
        budgetRepository: container.getRepository('budget'),
        categoryRepository: container.getRepository('category'),
        tagRepository: container.getRepository('tag'),
        recordRepository: container.getRepository('record'),
        transactionRepository: container.getRepository('transaction')!
    }
    await (new ApiGetAllBudgetController(getAllBudgetAdapter)).execute(req, res)
})

router.get('/v1/budgets/:id', async (req, res) => {
    let getBudgetAdapter: IGetBudgetAdpater = {
        budgetRepository: container.getRepository('budget'),
        transactionRepository: container.getRepository('transaction'),
        categoryRepository: container.getRepository('category'),
        recordRepository: container.getRepository('record'),
        tagRepository: container.getRepository('tag')
    } 
    await (new ApiGetBudgetController(getBudgetAdapter)).execute(req, res)
})

router.put('/v1/budgets/:id', async (req, res) => {
    let updateBudgetAdapter: IUpdateBudgetAdapter = {
        budgetRepository: container.getRepository('budget'),
        categoryRepository: container.getRepository('category'),
        tagRepository: container.getRepository('tag'),
        dateService: container.getService('date_service')
    }
    await (new ApiUpdateBudgetController(updateBudgetAdapter)).execute(req, res)
})

router.delete('/v1/budgets/:id', async (req, res) => {
    await (new ApiDeleteBudgetController(container.getRepository('budget'))).execute(req, res)
})

router.post('/v1/auto-budgets', async (req, res) => {
    await (new ApiAutoBudgetController(container.getRepository('budget'), container.getService('date_service'))).execute(req, res)
})

export default router