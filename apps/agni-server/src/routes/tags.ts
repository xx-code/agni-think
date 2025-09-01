import { QueryAllFetch } from "@core/dto/base";
import { RequestUpdateTagUseCase } from "@core/interactions/tag/updateTagUseCase";
import { Router, Request, Response } from "express";
import { body, matchedData, query, validationResult } from "express-validator";
import container from "src/di_contenair";

const router = Router();

router.post(`/v1/tags`, 
    body('value').notEmpty().isString(),
    body('color').optional().isHexColor(),
    async (req, res) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data = matchedData(req);
                var created = await container.tagUseCase?.createTag.execute({
                    color: data.color,
                    value: data.value,
                    isSystem: false
                });

                res.status(200).send(created);
                return;
            }

            res.status(400).send({ errors: result.array() });
        } catch(err) {
            res.status(400).send({ errors: [err] });
        }
    });

router.put(`/v1/tags/:id`, 
    body('value').optional().isString(),
    body('color').optional().isString(),
    async (req: Request, res: Response) => {
        try {
            const result = validationResult(req);
            if (result.isEmpty()) {
                const data: RequestUpdateTagUseCase = matchedData(req);
                data.id = req.params.id;
                await container.tagUseCase?.updateTag.execute(data);

                res.sendStatus(200);
                return;
            }
            
            res.status(400).send({ errors: result.array() });
        } catch(err) {
            res.status(400).send({ errors: [err] });
        } 
    });

router.get(`/v1/tags/:id`, async (req, res) => {
    try {
        var tag = await container.tagUseCase?.getTag.execute(req.params.id);
        res.status(200).send(tag);
    } catch(err) {
        res.status(400).send({ errors: [err] });
    }
});

router.get(
    `/v1/tags`,
    query('limit').isNumeric().toInt(),
    query('offset').isNumeric().toInt(),
    query('queryAll').optional().isBoolean().toBoolean(),
    async (req, res) => {
    try {
        const result = validationResult(req);

        if (!result.isEmpty()) {
            res.send({ errors: result.array() });
            return;
        }

        const request: QueryAllFetch = matchedData(req)

        var tags = await container.tagUseCase?.getAllTag.execute(request);
        res.status(200).send(tags);
    } catch(err) {
        res.status(400).send({ errors: [err] });
    }
});

router.delete(`/v1/tags/:id`, async (req, res) => {
    try {
        await container.tagUseCase?.deleteTag.execute(req.params.id);
        res.sendStatus(200);
    } catch(err) {
        res.status(400).send({ errors: [err] });
    }
});

export default router;