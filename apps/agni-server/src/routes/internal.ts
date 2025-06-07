import { AccountType, Period, TransactionMainCategory } from '@core/domains/constants';
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

router.get('/v1/internal/period-type', (req, res) => {
    const typeAccounts: {id: string, value: string}[] = []
    Object.keys(Period).forEach(key => {
        const valueAcc = Period[key as keyof typeof Period]
        switch(valueAcc) {
            case Period.DAY:
                typeAccounts.push({id: 'Day', value: 'Jour'})
                break;
            case Period.WEEK:
                typeAccounts.push({id: 'Week', value: 'Semaine'})
                break;
            case Period.MONTH:
                typeAccounts.push({id: 'Month', value: 'Mois'})
                break;
            case Period.YEAR:
                typeAccounts.push({id: 'Year', value: 'Année'})
                break
            default:
                break;
        }
    })
        
    res.status(200).send(typeAccounts)
});

router.get('/v1/internal/transaction-type', (req, res) => {
    const typeAccounts: {id: string, value: string}[] = []
    Object.keys(TransactionMainCategory).forEach(key => {
        const valueAcc = TransactionMainCategory[key as keyof typeof TransactionMainCategory]
        switch(valueAcc) {
            case TransactionMainCategory.FIXEDCOST:
                typeAccounts.push({id: 'FixedCost', value: 'Depense Fix'})
                break;
            case TransactionMainCategory.INCOME:
                typeAccounts.push({id: 'Income', value: 'Gains'})
                break;
            case TransactionMainCategory.OTHER:
                typeAccounts.push({id: 'Other', value: 'Autre'})
                break;
            case TransactionMainCategory.VARIABLECOST:
                typeAccounts.push({id: 'VariableCost', value: 'Depense Variable'})
                break
            default:
                break;
        }
    })
        
    res.status(200).send(typeAccounts)
});

export default router;