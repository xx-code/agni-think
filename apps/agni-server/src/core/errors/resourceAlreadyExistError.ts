export class ResourceAlreadyExist extends Error {
    cause: string
    constructor(message: string) {
        super(message);
        this.name = 'ResourceAlreadyExist';
        this.cause = 'The resource already exist in data';
    }
}