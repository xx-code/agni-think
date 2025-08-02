import { TaskScheduler, TaskTimer } from "@core/adapters/libs";
import container from "src/di_contenair";
import cron from 'node-cron';

export class CronScheduler implements TaskScheduler { 
    runTask(timer: TaskTimer, task?: () => Promise<void>, taskName?: string): void {
        cron.schedule(this.cronBuildTimer(timer), async () => {
            if (!task) {
                console.log(`Task not defined`)
                return 
            }
            console.log(`[server Task]: ${taskName || ''} - started`);
            task();
            console.log(`[server Task]: ${taskName || ''} - end`);
        }) ;
    }

    private cronBuildTimer(timer: TaskTimer): string {
        if (!timer.seconde && !timer.minute && !timer.hour &&
            !timer.month && timer.dayOfMonth && timer.dayOfweek)
            throw new Error("Timer not set for the task");

        return `${timer.seconde || '*'} ${timer.minute || '*'} ${timer.hour || '*'} ${timer.dayOfMonth || '*'} ${timer.month || '*'} ${timer.dayOfweek || '*'}`
    }
}

export class ApplyScheduleTransactionCronScheduler {
    execute(timer: TaskTimer) {
        const cronScheduler = new CronScheduler();
        cronScheduler.runTask(
            timer, 
            async () => await container.scheduleTransactionUseCase?.applyScheduleTransaction.execute(),
            "Schedule Transaction"
        );
    } 
}