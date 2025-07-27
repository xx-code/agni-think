import container from '../di_contenair'
import BudgetController from "src/controllers/budgets";

const usecases = container.budgetUseCase;

if (usecases === undefined)
    throw new Error("Budget Usecases not declare"); 

const budgetRouter = new BudgetController(
    usecases.createBudget,
    usecases.updateBudget,
    usecases.getBudget,
    usecases.getAllBudgets,
    usecases.deleteBudget
);

export default budgetRouter.getRoute()
