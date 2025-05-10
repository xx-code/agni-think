import { Router } from "express";
import { diContainer } from "src";
import { ApiCreateTagController, ApiDeleteTagController, ApiGetAllTagController, ApiGetTagController, ApiUpdateTagController } from "src/controllers/tags";

const router  = Router()

router.post('/v1/tags', async (req, res) => {   
    await (new ApiCreateTagController(diContainer.tagRepository!)).execute(req, res)
})

router.get('/v1/tags/:id', async (req, res) => {   
    await (new ApiGetTagController(diContainer.tagRepository!)).execute(req, res)
})

router.get('/v1/tags', async (req, res) => {   
    await (new ApiGetAllTagController(diContainer.tagRepository!)).execute(req, res)
})

router.put('/v1/tags/:id', async (req, res) => {   
    await (new ApiUpdateTagController(diContainer.tagRepository!)).execute(req, res)
})

router.delete('/v1/tags/:id', async (req, res) => {   
    await (new ApiDeleteTagController(diContainer.tagRepository!)).execute(req, res)
})

export default router