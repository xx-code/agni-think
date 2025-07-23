import { Request, Response, Router } from "express";
import { CreationTagUseCase, RequestCreationTagUseCase } from "@core/interactions/tag/creationTagUseCase";
import { DeleteTagUseCase } from "@core/interactions/tag/deleteTagUseCase";
import { GetAllTagDto, GetAllTagUseCase } from "@core/interactions/tag/getAllTagsUseCase";
import { GetTagDto, GetTagUseCase } from "@core/interactions/tag/getTagUseCase";
import { ApiError, ApiResponse, initApiResponse } from "./type";
import { isEmpty } from "@core/domains/helpers";
import { RequestUpdateTagUseCase, UpdateTagUseCase } from "@core/interactions/tag/updateTagUseCase";
import { TagRepository } from "@core/repositories/tagRepository";
import { ApiController } from "./base";
import { IUsecase } from "@core/interactions/interfaces";
import { CreatedDto, ListDto } from "@core/dto/base";

export default class TagController implements ApiController {
    private CONTROLLER_NAME: string = 'tags';
    private route = Router();

    private createTag: IUsecase<RequestCreationTagUseCase, CreatedDto>;
    private updateTag: IUsecase<RequestUpdateTagUseCase, void>;
    private deleteTag: IUsecase<string, void>;
    private getAllTags: IUsecase<void, ListDto<GetAllTagDto>>;
    private getTag: IUsecase<string, GetTagDto>;

    constructor(
        createTag: IUsecase<RequestCreationTagUseCase, CreatedDto>,
        updateTag: IUsecase<RequestUpdateTagUseCase, void>,
        deleteTag: IUsecase<string, void>,
        getAllTags: IUsecase<void, ListDto<GetAllTagDto>>,
        getTag: IUsecase<string, GetTagDto>
    ) {
        this.createTag = createTag;
        this.updateTag = updateTag;
        this.deleteTag = deleteTag;
        this.getAllTags = getAllTags;
        this.getTag = getTag;

        this.setupRoutes()
    }

    setupRoutes() {
        this.route.post(`/${this.CONTROLLER_NAME}`, this.handleCreateTagUsecase);
        this.route.put(`/${this.CONTROLLER_NAME}/:id`, this.handleUpdateTagUsecase);
        this.route.get(`/${this.CONTROLLER_NAME}/:id`, this.handleGetTagUsecase);
        this.route.get(`/${this.CONTROLLER_NAME}`, this.handleGetAllTagsUsecase);
        this.route.delete(`/${this.CONTROLLER_NAME}/:id`, this.handleDeleteTagUsecase);
    };

    getRoute(){
        return this.route;
    };

    async handleCreateTagUsecase(req: Request, res: Response) {
        var created = await this.createTag.execute({
            color: req.body.color,
            value: req.body.value,
            isSystem: false
        });

        res.status(200).send(created);
    }

    async handleUpdateTagUsecase(req: Request, res: Response) {
        await this.updateTag.execute({
            id: req.params.id,
            color: req.body.color,
            value: req.body.value
        });

        res.status(201);
    }

    async handleDeleteTagUsecase(req: Request, res: Response) {
        await this.deleteTag.execute(req.params.id);

        res.status(201);
    }

    async handleGetTagUsecase(req: Request, res: Response) {
        var tag = await this.getTag.execute(req.params.id);

        res.status(200).send(tag);
    }
    
    async handleGetAllTagsUsecase(req: Request, res: Response) {
        var tags = await this.getAllTags.execute();

        res.status(200).send(tags);
    }
}