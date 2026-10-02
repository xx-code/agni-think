package usecases.interfaces

interface IInnerUseCase<TInput, TOuput>: IUseCase<TInput, TOuput> {
   fun execInnerAsync(input: TInput): TOuput
}