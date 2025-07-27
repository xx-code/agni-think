import ScheduleTransationController from 'src/controllers/scheduleTransaction';
import container from '../di_contenair';

const usecases = container.scheduleTransactionUseCase;

if (usecases === undefined)
    throw new Error("Schedule transaction Usecases not declare"); 

const categoryRouter = new ScheduleTransationController(
    usecases.applyScheduleTransaction,
    usecases.createScheduleTransaction,
    usecases.deleteScheduleTransaction,
    usecases.getAllScheduleTransaction,
    usecases.getScheduleTransaction,
    usecases.updateScheduleTransaction
);

export default categoryRouter.getRoute()