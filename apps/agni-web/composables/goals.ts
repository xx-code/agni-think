export type GoalRowType = {
    id: string,
    title: string,
    amount: number,
    targetAmount: number
}

export const useListGoals = (): Ref<GoalRowType[]> => { 
    const budgets: Ref<GoalRowType[]> = ref(
        [
            {
                id: '1',
                title: 'Eco pour nouvelle voitre',
                amount: 1245.94,
                targetAmount: 13556
            },
            {
                id: '2',
                title: 'New Mackbook Pro',
                amount: 452,
                targetAmount: 1946
            },
            {
                id: '3',
                title: 'Nouvelle jordan',
                amount: 124,
                targetAmount: 245
            },
            {
                id: '4',
                title: 'La rolex',
                amount: 89,
                targetAmount: 11045
            }
        ]
    ) 

    return budgets 
}