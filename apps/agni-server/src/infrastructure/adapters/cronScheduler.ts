import { TaskScheduler, TaskTimer } from "@core/adapters/libs";
import container from "src/di_contenair";
import cron from 'node-cron';

export class CronScheduler implements TaskScheduler { 
    async runTask(timer: string, task?: () => Promise<void>, taskName?: string): Promise<void> {
        if (task)
            await task()
        cron.schedule(timer, async () => {
            if (!task) {
                console.log(`Task not defined`)
                return 
            }
            console.log(`[server Task]: ${taskName || ''} - started`);
            await task();
            console.log(`[server Task]: ${taskName || ''} - end`);
        }) ;
    }
}

export class ApplyScheduleTransactionCronScheduler {
    execute(timer: string) {
        const cronScheduler = new CronScheduler();
        cronScheduler.runTask(
            timer, 
            async () => await container.scheduleTransactionUseCase?.applyScheduleTransaction.execute(),
            "Schedule Transaction"
        );
    } 
}

export class AutoDeletreFreezeTransactionCronScheduler {
    execute(timer: string) {
        const cronScheduler = new CronScheduler();
        cronScheduler.runTask(
            timer,
            async () => await container.transactionUseCase?.autoFreezeTransaction.execute(),
            "Freeze transaction"
        )
    }
}