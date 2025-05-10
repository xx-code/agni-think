import CardBudget from "@/app/components/cardWithProgressBar";
import { Budget } from "@/models/budget";

type Props = {
    budgets: Budget[],
    onDelete: (id: string) => void
    onUpdate: (id: string) => void
}

export default function ListBudget({budgets, onDelete, onUpdate}: Props) {
    return (
        <div style={{display: 'flex', flexWrap: 'wrap'}}>
            {
                budgets.map((budget, index) => {
                    return (
                        <CardBudget 
                            key={index}
                            title={budget.name}
                            description=""
                            targetPrice={budget.target} 
                            currentPrice={budget.balance} 
                            onUpdate={() => onUpdate(budget.budgetId)} 
                            onDelete={() => onDelete(budget.budgetId)}
                        />
                    )
                } )
            }
        </div>
    )
}
