import container from '../di_contenair';
import { CategoryController } from "src/controllers/categories";

const usecases = container.categoryUseCase;

if (usecases === undefined)
    throw new Error("Accounts Usecases not declare"); 

const categoryRouter = new CategoryController(
    usecases.createCategory,
    usecases.updateCategory,
    usecases.getCategory,
    usecases.getAllCategory,
    usecases.deleteCategory,
)

export default categoryRouter.getRoute()