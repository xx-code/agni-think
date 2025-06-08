import { Router } from "express";
import { ApiAutoDeleteFreezeTransactionController, ApiCreateFreezeTransactionController, ApiCreateTransactionController, ApiDeleteTransactionController, ApiGetBalanceController, ApiGetTransactionController, ApiPaginationTransactionController, ApiTransfertTransactionController, ApiUpdateTransactionController } from "src/controllers/transactions";
import { IAddTransactionAdapter } from '@core/interactions/transaction/addTransactionUseCase';
import { IGetTransactionAdapter } from "@core/interactions/transaction/getTransactionUseCase";
import { IGetPaginationTransactionAdapter } from "@core/interactions/transaction/getPaginationTransactionUseCase";
import { IUpdateTransactionAdapter } from "@core/interactions/transaction/updateTransactionUseCase";
import { ITransfertTransactionAdapter } from "@core/interactions/transaction/transfertTransactionUseCase";
import container from '../di_contenair';

const router = Router()

router.post('/v1/transactions', async (req, res) => {
    const createTransactionAdapter: IAddTransactionAdapter = {
        accountRepository: container.getRepository('account'),
        budgetRepository: container.getRepository('budget')!,
        categoryRepository: container.getRepository('category'),
        recordRepository: container.getRepository('record')!,
        tagRepository: container.getRepository('tag'),
        transactionRepository: container.getRepository('transaction'),
        unitOfWork: container.getRepository('unit_of_work'),
        dateService: container.getService('date_service')
    }
    await (new ApiCreateTransactionController(createTransactionAdapter).execute(req, res))
})

router.get('/v1/transactions-balance/', async (req, res) => {
    await (new ApiGetBalanceController(container.getRepository('transaction'), container.getRepository('record')!)).execute(req, res)
})


router.get('/v1/transactions/:id', async (req, res) => {
    const getTransactionAdapter: IGetTransactionAdapter = {
        transactionRepository: container.getRepository('transaction'),
        categoryRepository: container.getRepository('category'),
        tagRepository: container.getRepository('tag'),
        recordRepository: container.getRepository('record')!
    }
    await (new ApiGetTransactionController(getTransactionAdapter)).execute(req, res)
})

router.get('/v1/transactions', async (req, res) => {
    const getPaginationTransaction: IGetPaginationTransactionAdapter = {
        transactionRepository: container.getRepository('transaction'),
        accountRepository: container.getRepository('account'),
        budgetRepository: container.getRepository('budget')!,
        categoryRepository: container.getRepository('category'),
        tagRepository: container.getRepository('tag'),
        recordRepository: container.getRepository('record')!
    }
    await (new ApiPaginationTransactionController(getPaginationTransaction)).execute(req, res)
})


router.put('/v1/transactions/:id', async (req, res) => {
    const updatePaginationTransaction: IUpdateTransactionAdapter = {
        transactionRepository: container.getRepository('transaction'),
        categoryRepository: container.getRepository('category'),
        budgetRepository: container.getRepository('budget')!,
        tagRepository: container.getRepository('tag'),
        recordRepository: container.getRepository('record')!,
        accountRepository: container.getRepository('account'),
        dateService: container.getService('date_service'),
        unitOfWork: container.getRepository('unit_of_work')
    }
    await (new ApiUpdateTransactionController(updatePaginationTransaction)).execute(req, res)
})

router.delete('/v1/transactions/:id', async (req, res) => {
    await (new ApiDeleteTransactionController(container.getRepository('transaction'), container.getRepository('record')!, container.getRepository('unit_of_work'), container.getRepository('account'))).execute(req, res)
})

router.post('/v1/freeze-transaction', async (req, res) => {
    await (new ApiCreateFreezeTransactionController(container.getService('date_service'), container.getRepository('transaction'), container.getRepository('account'), container.getRepository('category'), container.getRepository('record')!, container.getRepository('unit_of_work'))).execute(req, res)
})

router.post('/v1/transfert-transaction', async (req, res) => {
    const transfertAdapter: ITransfertTransactionAdapter = {
        transactionRepository: container.getRepository('transaction'),
        recordRepository: container.getRepository('record')!,
        accountRepository: container.getRepository('account'),
        categoryRepository: container.getRepository('category'),
        dateService: container.getService('date_service'),
        unitOfWork: container.getRepository('unit_of_work')
    }
    await (new ApiTransfertTransactionController(transfertAdapter)).execute(req, res)
})

router.post('/v1/freeze-transaciton/auto-delete-verification', async (req, res) => {
    await (new ApiAutoDeleteFreezeTransactionController(container.getRepository('account'), container.getRepository('transaction'), container.getRepository('record')!, container.getRepository('unit_of_work'), container.getService('date_service'))).execute(req, res)
})

export default router