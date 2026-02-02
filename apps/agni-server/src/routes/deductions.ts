import { QueryFilter } from '@core/dto/base';
import { RequestCreateDeductionTypeDto } from '@core/interactions/deduction/createDeductionType';
import { RequestUpdateDeductionTypeDto } from '@core/interactions/deduction/updateDeductionType';
import { Request, Router } from 'express';
import { body, matchedData, validationResult } from 'express-validator';
import container from '../di_contenair';

const router = Router();

router.post(
    '/v1/deduction-types',
    body('title').isString(),
    body('description').optional().isString(),
    body('base').notEmpty().isString(),
    body('mode').notEmpty().isString(),
    async (req, res) => {
    try {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestCreateDeductionTypeDto = matchedData(req);
            var created = await container.deductionTypeUseCase?.createDeductionType.execute(data);

            res.status(200).json(created)
            return;
        }

        res.status(400).send({errors: result.array()});
    } catch(err) {
        console.log(err)
        res.status(400).send({errors: [err]});
    }
});

// router.post(
//     '/v1/deductions',
//     body('typeId').notEmpty().isString(), 
//     body('rate').notEmpty().isNumeric(),
//     async (req, res) => {
//     try {
//         const result = validationResult(req);
//         if (result.isEmpty()) {
//             const data: RequestCreateDeductionDto = matchedData(req);
//             var created = await container.deductionUseCase?.createDeduction.execute(data);

//             res.status(200).json(created)
//             return;
//         }

//         res.status(400).send({errors: result.array()});
//     } catch(err) {
//         console.log(err)
//         res.status(400).send({errors: [err]});
//     }
// });

router.put(
    '/v1/deduction-types/:id',
    body('title').optional().isString(),
    body('description').optional().isString(),
    async (req: Request, res) => {
    try {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: RequestUpdateDeductionTypeDto = matchedData(req);
            const id = req.params.id as string
            data.id = id
            await container.deductionTypeUseCase?.updateDeductionType.execute(data);

            res.status(200)
            return;
        }

        res.status(400).send({errors: result.array()});
    } catch(err) {
        console.log(err)
        res.status(400).send({errors: [err]});
    }
});

// router.put(
//     '/v1/deductions',
//     body('rate').optional().isNumeric(),
//     async (req, res) => {
//     try {
//         const result = validationResult(req);
//         if (result.isEmpty()) {
//             const data: RequestUpdateDeductionDto = matchedData(req);
//             await container.deductionUseCase?.updateDeduction.execute(data);

//             res.status(200)
//             return;
//         }

//         res.status(400).send({errors: result.array()});
//     } catch(err) {
//         console.log(err)
//         res.status(400).send({errors: [err]});
//     }
// });

router.get(
    '/v1/deduction-types',
    async (req, res) => {
    try {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const data: QueryFilter = matchedData(req);
            var resData = await container.deductionTypeUseCase?.getAllDeductionTypes.execute(data);

            res.status(200).json(resData)
            return;
        }

        res.status(400).send({errors: result.array()});
    } catch(err) {
        console.log(err)
        res.status(400).send({errors: [err]});
    }
});

router.get(
    '/v1/deduction-types/:id',
    async (req, res) => {
    try {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const id = req.params.id as string
            var resData = await container.deductionTypeUseCase?.getDeductionType.execute(id);

            res.status(200).json(resData)
            return;
        }

        res.status(400).send({errors: result.array()});
    } catch(err) {
        console.log(err)
        res.status(400).send({errors: [err]});
    }
});

// router.get(
//     '/v1/deductions',
//     async (req, res) => {
//     try {
//         const result = validationResult(req);
//         if (result.isEmpty()) {
//             const data: QueryFilter = matchedData(req);
//             var resData = await container.deductionUseCase?.getAllDeductions.execute(data);

//             res.status(200).json(resData)
//             return;
//         }

//         res.status(400).send({errors: result.array()});
//     } catch(err) {
//         console.log(err)
//         res.status(400).send({errors: [err]});
//     }
// });

// router.get(
//     '/v1/deductions/:id',
//     async (req, res) => {
//     try {
//         const result = validationResult(req);
//         if (result.isEmpty()) {
//             const id = req.params.id as string
//             var resData = await container.deductionUseCase?.getDeduction.execute(id);

//             res.status(200).json(resData)
//             return;
//         }

//         res.status(400).send({errors: result.array()});
//     } catch(err) {
//         console.log(err)
//         res.status(400).send({errors: [err]});
//     }
// });

router.delete(
    '/v1/deduction-types/:id',
    async (req, res) => {
    try {
        const result = validationResult(req);
        if (result.isEmpty()) {
            const id = req.params.id as string
            await container.deductionTypeUseCase?.deleteDeductionType.execute(id);

            res.status(200)
            return;
        }

        res.status(400).send({errors: result.array()});
    } catch(err) {
        console.log(err)
        res.status(400).send({errors: [err]});
    }
});

// router.delete(
//     '/v1/deductions/:id',
//     async (req, res) => {
//     try {
//         const result = validationResult(req);
//         if (result.isEmpty()) {
//             const id = req.params.id as string
//             await container.deductionUseCase?.deleteDeduction.execute(id);

//             res.status(200)
//             return;
//         }

//         res.status(400).send({errors: result.array()});
//     } catch(err) {
//         console.log(err)
//         res.status(400).send({errors: [err]});
//     }
// });


export default router;