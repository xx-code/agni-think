export interface IAgent<InputType, OutputType> {
    process(input: InputType): Promise<OutputType>
}