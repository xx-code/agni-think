export class ValueError extends Error {
    cause: string
    constructor(message: string) {
        super(message);
        this.name = 'ValueError';
        this.cause = 'Error in domain verify mapping or interaction creation';
    }
}