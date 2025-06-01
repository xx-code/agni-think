import { Budget } from "@core/domains/entities/budget";
import { BudgetRepository } from "@core/repositories/budgetRepository";


export class MockBudgetRepository implements BudgetRepository {
    private budgets: Map<string, Budget> = new Map();

    async isBudgetExistByIds(ids: string[]): Promise<boolean> {
        return ids.every(id => this.budgets.has(id));
    }

    async isBudgetExistById(id: string): Promise<boolean> {
        return this.budgets.has(id);
    }

    async isBudgetExistByName(title: string): Promise<boolean> {
        for (const budget of this.budgets.values()) {
            if (budget.getTitle() === title) {
                return true;
            }
        }
        return false;
    }

    async save(budget: Budget): Promise<void> {
        this.budgets.set(budget.getId(), budget);
    }

    async get(id: string): Promise<Budget> {
        const budget = this.budgets.get(id);
        if (!budget) {
            throw new Error(`Budget with id ${id} not found`);
        }
        return budget;
    }

    async getAll(): Promise<Budget[]> {
        return Array.from(this.budgets.values());
    }

    async delete(id: string): Promise<void> {
        this.budgets.delete(id);
    }

    async toggleArchived(id: string, doArchive: boolean): Promise<void> {
        const budget = this.budgets.get(id);
        if (!budget) {
            throw new Error(`Budget with id ${id} not found`);
        }
        budget.setIsArchive(doArchive);
        this.budgets.set(id, budget);
    }

    async update(budget: Budget): Promise<void> {
        if (!this.budgets.has(budget.getId())) {
            throw new Error(`Budget with id ${budget.getId()} not found`);
        }
        this.budgets.set(budget.getId(), budget);
    }
}