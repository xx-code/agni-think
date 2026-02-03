
export interface IUsecase<TInput, TOutput> {
    execute(request: TInput, trx?: any): Promise<TOutput> 
}