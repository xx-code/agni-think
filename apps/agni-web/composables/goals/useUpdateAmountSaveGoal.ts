export type UpdateSaveGoalRequest = {
    saveGoalId: string, 
    isIncrease: boolean, 
    amount: number,
    accountId: string
}
export default async function useUpdateAmountSaveGoal(request: UpdateSaveGoalRequest): Promise<void> {
    if (request.isIncrease)
        await $fetch(`/api/save-goals/increase`, {
            method: "POST",
            body: {
                id: request.saveGoalId, 
                accountId: request.accountId,
                increaseAmount: request.amount 
            } 
        });    
    else 
        await $fetch(`/api/save-goals/decrease`, {
            method: "POST",
            body: {
                id: request.saveGoalId,
                accountId: request.accountId,
                decreaseAmount: request.amount
            }
        });
}