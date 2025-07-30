import { Request, Response, Router } from "express";
import { RequestCreationCategoryUseCase } from "@core/interactions/category/creationCategoryUseCase";
import { GetAllCategoryDto } from "@core/interactions/category/getAllCategoryUseCase";
import { GetCategoryDto } from "@core/interactions/category/getCategoryUseCase";
import { RequestUpdateCategoryUseCase } from "@core/interactions/category/updateCategoryUseCase";
import { IUsecase } from "@core/interactions/interfaces";
import { CreatedDto, ListDto } from "@core/dto/base";
import { ApiController } from "./base";
import { body, matchedData, validationResult } from 'express-validator';

export class CategoryController implements ApiController {
    private route = Router() 

    private createCategory: IUsecase<RequestCreationCategoryUseCase, CreatedDto>
    private updateCategory: IUsecase<RequestUpdateCategoryUseCase, void>
    private getCategory: IUsecase<string, GetCategoryDto>
    private getAllCategories: IUsecase<void, ListDto<GetAllCategoryDto>>
    private deleteCategory: IUsecase<string, void>
    
    constructor(
        createCategory: IUsecase<RequestCreationCategoryUseCase, CreatedDto>,
        updateCategory: IUsecase<RequestUpdateCategoryUseCase, void>,
        getCategory: IUsecase<string, GetCategoryDto>,
        getAllCategories: IUsecase<void, ListDto<GetAllCategoryDto>>,
        deleteCategory: IUsecase<string, void>
    ) {
        this.createCategory = createCategory
        this.updateCategory = updateCategory
        this.getCategory = getCategory
        this.getAllCategories = getAllCategories
        this.deleteCategory = deleteCategory

        this.setupRoutes()
    }

    setupRoutes() {
        this.route.post('/category', 
            body('title').notEmpty(),
            body('icon').notEmpty(),
            body('color').isEmpty().isHexColor(), 
            this.handleCreateCategory);
            
        this.route.put('/category/:id', 
            body('title').isEmpty(),
            body('icon').isEmpty(),
            body('color').isEmpty().isHexColor(),
            this.handleUpdateCategory)

        this.route.get('/category/:id', 
            this.handleGetCategory);
            
        this.route.get('/category', 
            this.handleGetAllCategory);

        this.route.delete('/category/:id', 
            this.handleDeleteCategory);
    }

    getRoute() {
        return this.route
    }

    private async handleCreateCategory(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestCreationCategoryUseCase = matchedData(req);
            data.isSystem = false;
            var created = await this.createCategory.execute(data);

            res.status(200).json(created)
        }

        res.send({errors: result.array()});
    }

    private async handleUpdateCategory(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestUpdateCategoryUseCase = matchedData(req);
            data.id = req.params.id;
            await this.updateCategory.execute(data);

            res.status(201);
        } 

        res.send({ errors: result.array() });
    }

    private async handleGetCategory(req: Request, res: Response) {
        var category = await this.getCategory.execute(req.params.id)
        res.status(200).json(category)
    }

    private async handleGetAllCategory(req: Request, res: Response) {
        var allCategories = await this.getAllCategories.execute()
        res.status(200).json(allCategories)
    }

    private async handleDeleteCategory(req: Request, res: Response) {
        await this.deleteCategory.execute(req.params.id)
        res.status(201)
    }
}
