import { Router } from "express";
import container from '../di_contenair'
import { ApiCreateAccountControler, ApiDeleteAccountController, ApiGetAccountAccountController, ApiGetAllAccountController, ApiUpdateAccountController } from "src/controllers/accounts";

const router = Router()

router.post('/v1/accounts', async (req, res) => {
    let api = new ApiCreateAccountControler(container.getRepository('account'))
    await api.execute(req, res)
})

router.get('/v1/accounts', async (req, res) => {
    let api = new ApiGetAllAccountController(container.getRepository('account'))
    await api.execute(req, res)
})

router.get('/v1/accounts/:id', async (req, res) => {
    let api = new ApiGetAccountAccountController(container.getRepository('account'))
    await api.execute(req, res)
})

router.put('/v1/accounts/:id', async (req, res) => {
    let api = new ApiUpdateAccountController(container.getRepository('account'))
    await api.execute(req, res)
})

// router.patch('/v1/accounts:id', async (req, res) => {

// })

router.delete('/v1/accounts/:id', async (req, res) => {
    let api = new ApiDeleteAccountController(container.getRepository('account'))
    await api.execute(req, res)
})

export default router 