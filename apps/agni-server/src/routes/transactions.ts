import { Router } from "express";
import { ApiAutoDeleteFreezeTransactionController, ApiCreateFreezeTransactionController, ApiCreateTransactionController, ApiDeleteTransactionController, ApiGetBalanceController, ApiGetTransactionController, ApiPaginationTransactionController, ApiUpdateTransactionController } from "src/controllers/transactions";
import { IAddTransactionAdapter } from '@core/interactions/transaction/addTransactionUseCase';
import { IGetTransactionAdapter } from "@core/interactions/transaction/getTransactionUseCase";
import { IGetPaginationTransactionAdapter } from "@core/interactions/transaction/getPaginationTransactionUseCase";
import { IUpdateTransactionAdapter } from "@core/interactions/transaction/updateTransactionUseCase";
import { diContainer } from "src";

const router = Router()

const createTransactionAdapter: IAddTransactionAdapter = {
    accountRepository: diContainer.accountRepository!,
    categoryRepository: diContainer.categoryRepository!,
    recordRepository: diContainer.recordRepository!,
    tagRepository: diContainer.tagRepository!,
    transactionRepository: diContainer.transactionRepository!,
    unitOfWork: diContainer.unitOfWork!,
    dateService: diContainer.dateService!
}

const getTransactionAdapter: IGetTransactionAdapter = {
    transactionRepository: diContainer.transactionRepository!,
    categoryRepository: diContainer.categoryRepository!,
    tagRepository: diContainer.tagRepository!,
    recordRepository: diContainer.recordRepository!
}

const getPaginationTransaction: IGetPaginationTransactionAdapter = {
    transactionRepository: diContainer.transactionRepository!,
    accountRepository: diContainer.accountRepository!,
    categoryRepository: diContainer.categoryRepository!,
    tagRepository: diContainer.tagRepository!,
    recordRepository: diContainer.recordRepository!
}

const updatePaginationTransaction: IUpdateTransactionAdapter = {
    transactionRepository: diContainer.transactionRepository!,
    categoryRepository: diContainer.categoryRepository!,
    tagRepository: diContainer.tagRepository!,
    recordRepository: diContainer.recordRepository!,
    accountRepository: diContainer.accountRepository!,
    dateService: diContainer.dateService!,
    unitOfWork: diContainer.unitOfWork!
}

router.post('/v1/transactions', async (req, res) => {
    await (new ApiCreateTransactionController(createTransactionAdapter).execute(req, res))
})

router.get('/v1/transactions-balance/', async (req, res) => {
    await (new ApiGetBalanceController(diContainer.transactionRepository!, diContainer.recordRepository!)).execute(req, res)
})


router.get('/v1/transactions/:id', async (req, res) => {
    await (new ApiGetTransactionController(getTransactionAdapter)).execute(req, res)
})

router.get('/v1/transactions', async (req, res) => {
    await (new ApiPaginationTransactionController(getPaginationTransaction)).execute(req, res)
})


router.put('/v1/transactions/:id', async (req, res) => {
    await (new ApiUpdateTransactionController(updatePaginationTransaction)).execute(req, res)
})

router.delete('/v1/transactions/:id', async (req, res) => {
    await (new ApiDeleteTransactionController(diContainer.transactionRepository!, diContainer.recordRepository!, diContainer.unitOfWork!, diContainer.accountRepository!)).execute(req, res)
})

router.post('/v1/freeze-transactions', async (req, res) => {
    await (new ApiCreateFreezeTransactionController(diContainer.dateService!, diContainer.transactionRepository!, diContainer.accountRepository!, diContainer.categoryRepository!, diContainer.recordRepository!, diContainer.unitOfWork!)).execute(req, res)
})

router.post('/v1/freeze-transaciton/auto-delete-verification', async (req, res) => {
    await (new ApiAutoDeleteFreezeTransactionController(diContainer.accountRepository!, diContainer.transactionRepository!, diContainer.recordRepository!, diContainer.unitOfWork!, diContainer.dateService!)).execute(req, res)
})

export default router