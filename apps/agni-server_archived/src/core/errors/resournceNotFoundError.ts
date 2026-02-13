export class ResourceNotFoundError extends Error {
    cause: string
    constructor(message: string) {
        super(message);
        this.name = 'ResourceNotFoundError';
        this.cause = message;
    }
}