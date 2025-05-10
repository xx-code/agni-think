import { Router } from "express";
import { diContainer } from "src";
import { ApiCreateCategoryController, ApiDeleteCategoryController, ApiGetAllCategoriesController, ApiGetCategoryController, ApiUpdateCategoryController } from "src/controllers/categories";


const router = Router()

router.post('/v1/categories', async (req, res) => {
    await (new ApiCreateCategoryController(diContainer.categoryRepository!)).execute(req, res)
})

router.get('/v1/categories/:id', async (req, res) => {
    await (new ApiGetCategoryController(diContainer.categoryRepository!)).execute(req, res)
})

router.get('/v1/categories', async (req, res) => {
    await (new ApiGetAllCategoriesController(diContainer.categoryRepository!)).execute(req, res)
})

router.put('/v1/categories/:id', async (req, res) => {
    await (new ApiUpdateCategoryController(diContainer.categoryRepository!)).execute(req, res)
})

router.delete('/v1/categories/:id', async (req, res) => {
    await (new ApiDeleteCategoryController(diContainer.categoryRepository!)).execute(req, res)
})

export default router