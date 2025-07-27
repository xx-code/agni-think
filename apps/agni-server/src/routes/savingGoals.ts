import { Router } from "express";
import container from '../di_contenair';
import SaveGoalController from "src/controllers/saveGoals";

const usecases = container.saveGoalUseCase;

if (usecases === undefined)
    throw new Error("SaveGoal Usecases not declare"); 

const saveGoalRouter = new SaveGoalController(
    usecases.addSaveGoal,
    usecases.increaseSaveGoal,
    usecases.decreaseSaveGoal,
    usecases.getSaveGoal,
    usecases.getAllSaveGoal,
    usecases.updateSaveGoal,
    usecases.deleteSaveGoal
);

export default saveGoalRouter.getRoute();