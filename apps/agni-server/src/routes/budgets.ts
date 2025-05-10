import { ICreationBudgetAdapter } from "@core/interactions/budgets/creationBudgetUseCase";
import { IGetAllBudgetAdapter } from "@core/interactions/budgets/getAllBudgetUseCase";
import { IGetBudgetAdpater } from "@core/interactions/budgets/getBudgetUseCase";
import { IUpdateBudgetAdapter } from "@core/interactions/budgets/updateBudgetUseCase";
import { Router } from "express";
import { ApiAutoBudgetController, ApiCreateBudgetController, ApiDeleteBudgetController, ApiGetAllBudgetController, ApiGetBudgetController, ApiUpdateBudgetController } from "src/controllers/budgets";
import { diContainer } from "src";

const router = Router()

let createBudgetAdapter: ICreationBudgetAdapter = {
    budgetRepository: diContainer.budgetRepository!,
    categoryRepository: diContainer.categoryRepository!,
    tagRepository: diContainer.tagRepository!,
    dateService: diContainer.dateService!
}

let getBudgetAdapter: IGetBudgetAdpater = {
    budgetRepository: diContainer.budgetRepository!,
    transactionRepository: diContainer.transactionRepository!,
    categoryRepository: diContainer.categoryRepository!,
    recordRepository: diContainer.recordRepository!,
    tagRepository: diContainer.tagRepository!
}

let getAllBudgetAdapter: IGetAllBudgetAdapter = {
    budgetRepository: diContainer.budgetRepository!,
    categoryRepository: diContainer.categoryRepository!,
    tagRepository: diContainer.tagRepository!,
    recordRepository: diContainer.recordRepository!,
    transactionRepository: diContainer.transactionRepository!
}

let updateBudgetAdapter: IUpdateBudgetAdapter = {
    budgetRepository: diContainer.budgetRepository!,
    categoryRepository: diContainer.categoryRepository!,
    tagRepository: diContainer.tagRepository!,
    dateService: diContainer.dateService!
}



router.post('/v1/budgets', async (req, res) => {
    await (new ApiCreateBudgetController(createBudgetAdapter, diContainer.dateService!)).execute(req, res)
})

router.get('/v1/budgets', async (req, res) => {
    await (new ApiGetAllBudgetController(getAllBudgetAdapter)).execute(req, res)
})

router.get('/v1/budgets/:id', async (req, res) => {
    await (new ApiGetBudgetController(getBudgetAdapter)).execute(req, res)
})

router.put('/v1/budgets/:id', async (req, res) => {
    await (new ApiUpdateBudgetController(updateBudgetAdapter)).execute(req, res)
})

router.delete('/v1/budgets/:id', async (req, res) => {
    await (new ApiDeleteBudgetController(diContainer.budgetRepository!)).execute(req, res)
})

router.post('/v1/auto-budgets', async (req, res) => {
    await (new ApiAutoBudgetController(diContainer.budgetRepository!, diContainer.dateService!)).execute(req, res)
})


export default router