import container from '../di_contenair'
import AccountRoute from "src/controllers/accounts";

const usecases = container.accountUseCase;

if (usecases === undefined)
    throw new Error("Accounts Usecases not declare"); 

const router = new AccountRoute(
    usecases.createAccount,
    usecases.updateAccount,
    usecases.getAccount,
    usecases.getAllAccount,
    usecases.deleteAccount,
)

export default router.getRoute()