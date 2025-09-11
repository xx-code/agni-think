import { TransactionStatus, TransactionType } from "@core/domains/constants"
import Notification from "@core/domains/entities/notification"
import { PatrimonySnapshot } from "@core/domains/entities/patrimonySnapshot"
import { Transaction } from "@core/domains/entities/transaction"
import { QueryFilterAllRepository, RepositoryListResult } from "@core/repositories/dto"

export interface QueryFilterExtend<T>{
    isSatisty(entity: T): boolean
}

export default interface Repository<T> {
    create(entity: T): Promise<void>
    get(id: string): Promise<T|null>
    getAll(filters: QueryFilterAllRepository, filterExtend?: QueryFilterExtend<T>): Promise<RepositoryListResult<T>>
    getManyByIds(ids: string[]): Promise<T[]>
    update(entity: T): Promise<void>
    delete(id: string): Promise<void>
    existByName(name: string): Promise<boolean>
}

export class TransactionFilter implements QueryFilterExtend<Transaction> { 
    accounts?: Array<string>
    categories?: Array<string>
    budgets?: Array<string>
    tags?: Array<string>
    startDate?: Date
    endDate?: Date
    strictEndDate?: boolean
    strictStartDate?: boolean
    status?: TransactionStatus
    types?: TransactionType[]
    isFreeze?: boolean
    queryAll?: boolean

    isSatisty(entity: Transaction): boolean {
        if (this.accounts && !this.accounts.includes(entity.getAccountRef())) return false;
        if (this.categories && !this.categories.includes(entity.getCategoryRef())) return false;
        if (this.budgets && !entity.getBudgetRefs().some(b => this.budgets!.includes(b))) return false;
        if (this.tags && !entity.getTags().some(t => this.tags!.includes(t))) return false;
        if (this.startDate && entity.getUTCDate() < this.startDate) return false;
        if (this.endDate && entity.getUTCDate() > this.endDate) return false;
        if (this.status && entity.getStatus() !== this.status) return false;
        if (this.types && !this.types.includes(entity.getTransactionType())) return false;
        if (this.isFreeze !== undefined && entity.getIsFreeze() !== this.isFreeze) return false;
        // minPrice/maxPrice à adapter selon l'implémentation de Money
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
