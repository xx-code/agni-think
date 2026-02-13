export class RepositoryError extends Error {
    cause: string
    constructor(message: string) {
        super(message);
        this.name = 'There a error in the repository';
        this.cause = message;
    }
}