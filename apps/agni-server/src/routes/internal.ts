import { AccountType } from '@core/domains/constants';
import { Router } from 'express';

const router = Router();

router.get('/v1/internal/account-type', (req, res) => {
    const typeAccounts: {id: string, value: string}[] = []
    Object.keys(AccountType).forEach(key => {
        const valueAcc = AccountType[key as keyof typeof AccountType]
        switch(valueAcc) {
            case AccountType.CHECKING:
                typeAccounts.push({id: 'Checking', value: 'Compte courant'})
                break;
            case AccountType.SAVING:
                typeAccounts.push({id: 'Saving', value: 'Epargne'})
                break;
            case AccountType.BROKING:
                typeAccounts.push({id: 'Broking', value: 'Investissement'})
                break;
            case AccountType.BUSINESS:
                typeAccounts.push({id: 'Business', value: 'Pro'})
                break
            default:
                break;
        }
    })
        
    res.status(200).send(typeAccounts)
});

export default router;