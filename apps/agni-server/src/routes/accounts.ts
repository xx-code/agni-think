import { Router } from "express";
import { diContainer } from "src";
import { ApiCreateAccountControler, ApiDeleteAccountController, ApiGetAccountAccountController, ApiGetAllAccountController, ApiUpdateAccountController } from "src/controllers/accounts";



const router = Router()

router.post('/v1/accounts', async (req, res) => {
    let api = new ApiCreateAccountControler(diContainer.accountRepository!)
    await api.execute(req, res)
})

router.get('/v1/accounts', async (req, res) => {
    let api = new ApiGetAllAccountController(diContainer.accountRepository!)
    await api.execute(req, res)
})

router.get('/v1/accounts/:id', async (req, res) => {
    let api = new ApiGetAccountAccountController(diContainer.accountRepository!)
    await api.execute(req, res)
})

router.put('/v1/accounts/:id', async (req, res) => {
    let api = new ApiUpdateAccountController(diContainer.accountRepository!)
    await api.execute(req, res)
})

// router.patch('/v1/accounts:id', async (req, res) => {

// })

router.delete('/v1/accounts/:id', async (req, res) => {
    let api = new ApiDeleteAccountController(diContainer.accountRepository!)
    await api.execute(req, res)
})

export default router 