import { Account, ContributionAccount, ImportanceGoal, IntensityEmotionalDesir, ManagementAccount, Period, TransactionType } from '@core/domains/constants';
import { Router } from 'express';

const router = Router();

router.get('/v1/internal/account-type', (req, res) => {
    const typeAccounts: {id: string, value: string}[] = []
    Object.keys(Account).forEach(key => {
        const valueAcc = Account[key as keyof typeof Account]
        switch(valueAcc) {
            case Account.CHECKING:
                typeAccounts.push({id: 'Checking', value: 'Compte courant'})
                break;
            case Account.SAVING:
                typeAccounts.push({id: 'Saving', value: 'Epargne'})
                break;
            case Account.BROKING:
                typeAccounts.push({id: 'Broking', value: 'Investissement'})
                break;
            case Account.BUSINESS:
                typeAccounts.push({id: 'Business', value: 'Pro'})
                break
            case Account.CREDIT_CARD:
                typeAccounts.push({id: 'CreditCard', value: 'Carte Credit' })
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
    Object.keys(TransactionType).forEach(key => {
        const valueAcc = TransactionType[key as keyof typeof TransactionType]
        switch(valueAcc) {
            case TransactionType.FIXEDCOST:
                typeAccounts.push({id: 'FixedCost', value: 'Depense Fix'})
                break;
            case TransactionType.INCOME:
                typeAccounts.push({id: 'Income', value: 'Gains'})
                break;
            case TransactionType.OTHER:
                typeAccounts.push({id: 'Other', value: 'Autre'})
                break;
            case TransactionType.VARIABLECOST:
                typeAccounts.push({id: 'VariableCost', value: 'Depense Variable'})
                break
            default:
                break;
        }
    })
        
    res.status(200).send(typeAccounts)
});

router.get('/v1/internal/intensity-desir-type', (req, res) => {
    const types: {id: number, value: string}[] = []
    Object.keys(IntensityEmotionalDesir).forEach(key => {
        const valueAcc = IntensityEmotionalDesir[key as keyof typeof IntensityEmotionalDesir]
        switch(valueAcc) {
            case IntensityEmotionalDesir.INDIFFERENT:
                types.push({value: 'Indifferent', id: IntensityEmotionalDesir.INDIFFERENT})
                break;
            case IntensityEmotionalDesir.PLEASURE:
                types.push({value: 'Plaisir', id: IntensityEmotionalDesir.PLEASURE})
                break;
            case IntensityEmotionalDesir.DESIR:
                types.push({value: 'Desire', id: IntensityEmotionalDesir.DESIR})
                break;
            case IntensityEmotionalDesir.OBSESSION:
                types.push({value: 'Obsession', id: IntensityEmotionalDesir.OBSESSION})
                break
            case IntensityEmotionalDesir.FOMO:
                types.push({value: 'Fomo', id: IntensityEmotionalDesir.FOMO})
                break
            default:
                break;
        }
    })
        
    res.status(200).send(types)
});

router.get('/v1/internal/importance-type', (req, res) => {
    const types: {id: number, value: string}[] = []
    Object.keys(ImportanceGoal).forEach(key => {
        const valueAcc = ImportanceGoal[key as keyof typeof ImportanceGoal]
        switch(valueAcc) {
            case ImportanceGoal.INSIGNIFIANT:
                types.push({value: 'Insignifiant', id: ImportanceGoal.INSIGNIFIANT})
                break;
            case ImportanceGoal.NORMAL:
                types.push({value: 'Normal', id: ImportanceGoal.NORMAL})
                break;
            case ImportanceGoal.IMPORTANT:
                types.push({value: 'Important', id: ImportanceGoal.IMPORTANT})
                break;
            case ImportanceGoal.URGENT:
                types.push({value: 'Urgent', id: ImportanceGoal.URGENT})
                break
            default:
                break;
        }
    })
        
    res.status(200).send(types)
});

router.get('/v1/internal/contribution-type', (req, res) => {
    const types: {id: string, value: string}[] = []
    Object.keys(ContributionAccount).forEach(key => {
        const valueAcc = ContributionAccount[key as keyof typeof ContributionAccount]
        switch(valueAcc) {
            case ContributionAccount.REGISTERED:
                types.push({value: 'Enregisterer', id: ContributionAccount.REGISTERED})
                break;
            case ContributionAccount.UNREGISTERED:
                types.push({value: 'Unregister', id: ContributionAccount.UNREGISTERED})
                break;
            default:
                break;
        }
    })
        
    res.status(200).send(types)
});

router.get('/v1/internal/management-account-type', (req, res) => {
    const types: {id: string, value: string}[] = []
    Object.keys(ManagementAccount).forEach(key => {
        const valueAcc = ManagementAccount[key as keyof typeof ManagementAccount]
        switch(valueAcc) {
            case ManagementAccount.MANAGED:
                types.push({value: 'Manager', id: ManagementAccount.MANAGED})
                break;
            case ManagementAccount.ROBOT:
                types.push({value: 'Robot', id: ManagementAccount.ROBOT})
                break;
            case ManagementAccount.SELF_DIRECTED:
                types.push({value: 'Gerer', id: ManagementAccount.SELF_DIRECTED})
                break;
            default:
                break;
        }
    })
        
    res.status(200).send(types)
});



export default router;