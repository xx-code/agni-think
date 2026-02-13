export type UpdateSaveGoalRequest = {
    saveGoalId: string, 
    isIncrease: boolean, 
    amount: number,
    accountId: string
}
export default async function useUpdateAmountSaveGoal(request: UpdateSaveGoalRequest): Promise<void> {
    if (request.isIncrease)
        await $fetch(`/api/saving-goals/${request.saveGoalId}/increase`, {
            method: "PUT",
            body: {
                id: request.saveGoalId, 
                accountId: request.accountId,
                amount: request.amount 
            } 
        });    
    else 
        await $fetch(`/api/saving-goals/${request.saveGoalId}/decrease`, {
            method: "PUT",
            body: {
                id: request.saveGoalId,
                accountId: request.accountId,
                amount: request.amount
            }
        });
}