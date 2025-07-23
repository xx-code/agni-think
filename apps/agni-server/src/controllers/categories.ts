import { Request, Response, Router } from "express";
import { RequestCreationCategoryUseCase } from "@core/interactions/category/creationCategoryUseCase";
import { GetAllCategoryDto } from "@core/interactions/category/getAllCategoryUseCase";
import { GetCategoryDto } from "@core/interactions/category/getCategoryUseCase";
import { RequestUpdateCategoryUseCase } from "@core/interactions/category/updateCategoryUseCase";
import { IUsecase } from "@core/interactions/interfaces";
import { CreatedDto, ListDto } from "@core/dto/base";
import { ApiController } from "./base";

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
        this.route.post('/category', this.handleCreateCategory)
        this.route.put('/category/:id', this.handleUpdateCategory)
        this.route.get('/category/:id', this.handleGetCategory)
        this.route.get('/category', this.handleGetAllCategory)
        this.route.delete('/category/:id', this.handleDeleteCategory)
    }

    getRoute() {
        return this.route
    }

    private async handleCreateCategory(req: Request, res: Response) {
        var created = await this.createCategory.execute({
            title: req.body.title,
            icon: req.body.icon,
            color: req.body.color,
            isSystem: false
        })

        res.status(200).json(created)
    }

    private async handleUpdateCategory(req: Request, res: Response) {
        await this.updateCategory.execute({
            id: req.params.id,
            title: req.body.title,
            color: req.body.color,
            icon: req.body.icon 
        })

        res.status(201)
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
