import { Request, Response, Router } from "express";
import { RequestCreationTagUseCase } from "@core/interactions/tag/creationTagUseCase";
import { GetAllTagDto } from "@core/interactions/tag/getAllTagsUseCase";
import { GetTagDto } from "@core/interactions/tag/getTagUseCase";
import { RequestUpdateTagUseCase } from "@core/interactions/tag/updateTagUseCase";
import { ApiController } from "./base";
import { IUsecase } from "@core/interactions/interfaces";
import { CreatedDto, ListDto } from "@core/dto/base";
import { body, matchedData, validationResult } from "express-validator";

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
        this.route.post(`/v1/${this.CONTROLLER_NAME}`, 
            body('value').notEmpty(),
            body('color').isEmpty().isHexColor(),
            this.handleCreateTagUsecase);

        this.route.put(`/v1/${this.CONTROLLER_NAME}/:id`, 
            body('value').isEmpty(),
            body('color').isEmpty().isHexColor(),
            this.handleUpdateTagUsecase);

        this.route.get(`/v1/${this.CONTROLLER_NAME}/:id`, 
            this.handleGetTagUsecase);

        this.route.get(`/v1/${this.CONTROLLER_NAME}`, 
            this.handleGetAllTagsUsecase);

        this.route.delete(`/v1/${this.CONTROLLER_NAME}/:id`, 
            this.handleDeleteTagUsecase);
    };

    getRoute(){
        return this.route;
    };

    async handleCreateTagUsecase(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data = matchedData(req);
            var created = await this.createTag.execute({
                color: data.color,
                value: data.value,
                isSystem: false
            });

            res.status(200).send(created);
        }

        res.send({ errors: result.array() });
    }

    async handleUpdateTagUsecase(req: Request, res: Response) {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestUpdateTagUseCase = matchedData(req);
            data.id = req.params.id;
            await this.updateTag.execute(data);

            res.status(201);
        }
        
        res.send({ errors: result.array() });
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