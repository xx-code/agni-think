import { body, matchedData, validationResult } from 'express-validator';
import container from '../di_contenair';
import { Router, Response, Request } from 'express';
import { RequestCreationCategoryUseCase } from '@core/interactions/category/creationCategoryUseCase';
import { RequestUpdateCategoryUseCase } from '@core/interactions/category/updateCategoryUseCase';

const router = Router();

router.post('/v1/categories', 
    body('title').notEmpty().isString(),
    body('icon').notEmpty().isString(),
    body('color').optional().isHexColor(), 
    async (req, res) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestCreationCategoryUseCase = matchedData(req);
                data.isSystem = false;
                var created = await container.categoryUseCase?.createCategory.execute(data);

                res.status(200).json(created)
                return;
            }

            res.send({errors: result.array()});
        } catch(err) {
            res.status(400).send({errors: [err]});
        }
    });
    
router.put('/v1/categories/:id', 
    body('title').optional().isString(),
    body('icon').optional().isString(),
    body('color').optional().isHexColor(),
    async (req: Request, res: Response) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestUpdateCategoryUseCase = matchedData(req);
                data.id = req.params.id;
                await container.categoryUseCase?.updateCategory.execute(data);

                res.sendStatus(200);
                return;
            } 

            res.send({ errors: result.array() });
        } catch(err) {
            res.status(400).send({errors: [err]});
        }
    });

router.get('/v1/categories/:id', async (req, res) => {
    try {
        var category = await container.categoryUseCase?.getCategory.execute(req.params.id)
        res.status(200).json(category)
    } catch(err) {
        res.status(400).send({ errors: [err] });
    }
});
    
router.get('/v1/categories', async (req, res) => {
    try {
        var allCategories = await container.categoryUseCase?.getAllCategory.execute()
        res.status(200).json(allCategories)
    } catch(err) {
        res.status(400).send({ errors: [err]})
    }
});

router.delete('/v1/categories/:id', async (req, res) => {
    try {
        await container.categoryUseCase?.deleteCategory.execute(req.params.id)
        res.sendStatus(200);   
    } catch (err) {
        res.status(400).send({ errors: [err]})
    }
});

export default router;