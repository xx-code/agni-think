import { IObjectEquality } from '@core/domains/interface/equality';

export default abstract class ValueObject implements IObjectEquality {
    abstract isEqual(object: ValueObject): boolean;
    abstract toJson(): string;
    static fromJson(string: string): ValueObject { throw new Error("NO implemented")};
}