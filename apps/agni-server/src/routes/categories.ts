import { Router } from "express";
import container from '../di_contenair';
import { ApiCreateCategoryController, ApiDeleteCategoryController, ApiGetAllCategoriesController, ApiGetCategoryController, ApiUpdateCategoryController } from "src/controllers/categories";

const router = Router()

router.post('/v1/categories', async (req, res) => {
    await (new ApiCreateCategoryController(container.getRepository('category'))).execute(req, res)
})

router.get('/v1/categories/:id', async (req, res) => {
    await (new ApiGetCategoryController(container.getRepository('category'))).execute(req, res)
})

router.get('/v1/categories', async (req, res) => {
    await (new ApiGetAllCategoriesController(container.getRepository('category'))).execute(req, res)
})

router.put('/v1/categories/:id', async (req, res) => {
    await (new ApiUpdateCategoryController(container.getRepository('category'))).execute(req, res)
})

router.delete('/v1/categories/:id', async (req, res) => {
    await (new ApiDeleteCategoryController(container.getRepository('category'))).execute(req, res)
})

export default router