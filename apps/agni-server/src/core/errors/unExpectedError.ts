export default class UnExpectedError  extends Error {
    cause: string
    constructor(message: string) {
        super(message);
        this.name = 'UnExpectedError';
        this.cause = message;
    }
}