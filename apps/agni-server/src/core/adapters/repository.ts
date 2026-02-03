import { TransactionStatus, TransactionType } from "@core/domains/constants"
import { Budget } from "@core/domains/entities/budget"
import { Holding } from "@core/domains/entities/holding"
import { HoldingTransaction } from "@core/domains/entities/holdingTransaction"
import Notification from "@core/domains/entities/notification"
import { PatrimonySnapshot } from "@core/domains/entities/patrimonySnapshot"
import { Record } from "@core/domains/entities/record"
import { SaveGoal } from "@core/domains/entities/saveGoal"
import { ScheduleTransaction } from "@core/domains/entities/scheduleTransaction"
import { Transaction } from "@core/domains/entities/transaction"
import { QueryFilterAllRepository, RepositoryListResult } from "@core/repositories/dto"

export interface QueryFilterExtend<T>{
    isSatisty(entity: T): boolean
}

export default interface Repository<T> {
    create(entity: T, trx?: any): Promise<void>
    get(id: string, trx?: any): Promise<T|null>
    getAll(filters: QueryFilterAllRepository, filterExtend?: QueryFilterExtend<T>, trx?: any): Promise<RepositoryListResult<T>>
    getManyByIds(ids: string[], trx?: any): Promise<T[]>
    update(entity: T, trx?: any): Promise<void>
    delete(id: string, trx?: any): Promise<void>
    existByName(name: string, trx?: any): Promise<boolean>
}

export type RepositoryDateComparator = {
    date: Date
    comparator: '<' | '<=' | '>' | '>=' | '='
}

export class TransactionFilter implements QueryFilterExtend<Transaction> { 
    accounts?: Array<string>
    status?: TransactionStatus
    types?: TransactionType[]
    startDate?: Date
    endDate?: Date
    strictEndDate?: boolean
    strictStartDate?: boolean
    isFreeze?: boolean

    isSatisty(entity: Transaction): boolean {
        if (this.accounts && !this.accounts.includes(entity.getAccountRef())) return false;
        if (this.status && entity.getStatus() !== this.status) return false;
        if (this.types && !this.types.includes(entity.getTransactionType())) return false;
        if (this.isFreeze !== undefined && entity.getIsFreeze() !== this.isFreeze) return false;
        if (this.startDate && entity.getDate() < this.startDate) return false;
        if (this.endDate && entity.getDate() > this.endDate) return false;
        // minPrice/maxPrice à adapter selon l'implémentation de Money
        return true;
    }
}

export class RecordFilter implements QueryFilterExtend<Record> { 
    transactionIds?: Array<string>
    categories?: Array<string>
    budgets?: Array<string>
    tags?: Array<string>

    isSatisty(entity: Record): boolean {
        if (this.transactionIds && !this.transactionIds.includes(entity.getTransactionId())) return false;
        if (this.categories && !this.categories.includes(entity.getCategoryRef())) return false;
        if (this.budgets && !entity.getBudgetRefs().some(b => this.budgets!.includes(b))) return false;
        if (this.tags && !entity.getTags().some(t => this.tags!.includes(t))) return false;
        // minPrice/maxPrice à adapter selon l'implémentation de Money
        return true;
    }
}

export interface TransactionRecordCountReader {
    count(queryTransaction: QueryFilterExtend<Transaction>, queryRecord: QueryFilterExtend<Record>): Promise<number> 
}

export class ScheduleTransactionFilter implements QueryFilterExtend<ScheduleTransaction> {
    schedulerDueDate?: RepositoryDateComparator

    isSatisty(entity: ScheduleTransaction): boolean {
        if (this.schedulerDueDate) {
            var isValid = true
            switch(this.schedulerDueDate.comparator) {
                case "<": 
                    isValid = this.schedulerDueDate.date < entity.getSchedule().dueDate 
                    break
                case "<=": 
                    isValid = this.schedulerDueDate.date <= entity.getSchedule().dueDate
                    break
                case ">": 
                    isValid = this.schedulerDueDate.date > entity.getSchedule().dueDate
                    break
                case ">=": 
                    isValid = this.schedulerDueDate.date >= entity.getSchedule().dueDate
                    break
                case "=": 
                    isValid = this.schedulerDueDate.date == entity.getSchedule().dueDate
                    break
            }
            if (!isValid) return false;
        }

        return true;
    } 
}

export class BudgetFilter implements QueryFilterExtend<Budget> {
    schedulerDueDate?: RepositoryDateComparator
    isArchive?: boolean

    isSatisty(entity: Budget): boolean {
        if (this.schedulerDueDate) {
            var isValid = true
            switch(this.schedulerDueDate.comparator) {
                case "<": 
                    isValid = this.schedulerDueDate.date < entity.getSchedule().dueDate 
                    break
                case "<=": 
                    isValid = this.schedulerDueDate.date <= entity.getSchedule().dueDate
                    break
                case ">": 
                    isValid = this.schedulerDueDate.date > entity.getSchedule().dueDate
                    break
                case ">=": 
                    isValid = this.schedulerDueDate.date >= entity.getSchedule().dueDate
                    break
                case "=": 
                    isValid = this.schedulerDueDate.date == entity.getSchedule().dueDate
                    break
            }
            if (!isValid) return false;
        }

        if (this.isArchive !== undefined &&  this.isArchive !== entity.getIsArchive()) return false

        return true;
    } 
}

export class PatrimonySnapshotFilter implements QueryFilterExtend<PatrimonySnapshot> { 
    patrimonyIds?: Array<string> 
    startDate?: Date
    endDate?: Date

    isSatisty(entity: PatrimonySnapshot): boolean {
        if (this.patrimonyIds && !this.patrimonyIds.includes(entity.getPatrimonyId())) return false
        if (this.startDate && entity.getDate() < this.startDate) return false;
        if (this.endDate && entity.getDate() > this.endDate) return false;

        return  true;
    }
}

export class NotificaitonExtendFilter implements QueryFilterExtend<Notification> {
    isRead?: boolean

    isSatisty(entity: Notification): boolean {
        if (this.isRead !== undefined)
            return entity.getIsRead() === this.isRead
        return true
    } 
}

export class SaveGoalExtendFilter implements QueryFilterExtend<SaveGoal> { 
    accountId?: string

    isSatisty(entity: SaveGoal): boolean {
        if (this.accountId !== undefined)
            return entity.getAccountId() == this.accountId

        return true
    }
}

export class HoldingExtendFilter implements QueryFilterExtend<Holding> {
    accountId?: string

    isSatisty(entity: Holding): boolean {
        if (this.accountId !== undefined)
            return entity.getAccountId() == this.accountId

        return true
    }
}

export class HoldingTransactionExtendFilter implements QueryFilterExtend<HoldingTransaction> {
    holdingId?: string

    isSatisty(entity: HoldingTransaction): boolean {
        if (this.holdingId !== undefined)
            return entity.getHoldingId() == this.holdingId

        return true
    }
}