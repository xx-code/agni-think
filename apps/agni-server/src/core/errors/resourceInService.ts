export default class ResourceInService extends Error {
    cause: string
    constructor(message: string) {
        super(message);
        this.name = 'ResourceInService';
        this.cause = 'The resource in use';
    }
}