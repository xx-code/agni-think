import TransactionController from 'src/controllers/transactions';
import container from '../di_contenair';

const usecases = container.transactionUseCase;

if (usecases === undefined)
    throw new Error("Transaction Usecases not declare");

const transactionRouter = new TransactionController(
    usecases.createTransaction,
    usecases.updateTransaction,
    usecases.deleteTransaction,
    usecases.getBalanceBy,
    usecases.getTransaction,
    usecases.getPaginition,
    usecases.transfertTransaction,
    usecases.freezeTransaction,
    usecases.autoFreezeTransaction
);

export default transactionRouter.getRoute()