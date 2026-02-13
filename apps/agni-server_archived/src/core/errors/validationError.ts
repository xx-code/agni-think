export default class ValidationError extends Error {
    public code: number|undefined;
    cause: string
    constructor(message: string, code:number|undefined = undefined) {
        super(message)
        this.name = "ValidationError";
        this.cause = message;
        this.code = code;
    }
}