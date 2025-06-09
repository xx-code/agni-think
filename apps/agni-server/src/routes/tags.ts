import { Router } from "express";
import { ApiCreateTagController, ApiDeleteTagController, ApiGetAllTagController, ApiGetTagController, ApiUpdateTagController } from "src/controllers/tags";
import container from '../di_contenair';

const router  = Router()

router.post('/v1/tags', async (req, res) => {   
    await (new ApiCreateTagController(container.getRepository('tag'))).execute(req, res)
})

router.get('/v1/tags/:id', async (req, res) => {   
    await (new ApiGetTagController(container.getRepository('tag'))).execute(req, res)
})

router.get('/v1/tags', async (req, res) => {   
    await (new ApiGetAllTagController(container.getRepository('tag'))).execute(req, res)
})

router.put('/v1/tags/:id', async (req, res) => {   
    await (new ApiUpdateTagController(container.getRepository('tag'))).execute(req, res)
})

router.delete('/v1/tags/:id', async (req, res) => {   
    await (new ApiDeleteTagController(container.getRepository('tag'))).execute(req, res)
})

export default router