export interface IUsecase<TInput, TOutput> {
    execute(request: TInput): Promise<TOutput> 
}