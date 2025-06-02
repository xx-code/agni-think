import { AccountType } from '@core/domains/constants';
import { Router } from 'express';

const router = Router();

router.get('/v1/internal/account-type', (req, res) => {
    const typeAccounts = []
    for (const type in AccountType)
        typeAccounts.push(type)
    
    res.status(200).send(typeAccounts)
});

export default router;