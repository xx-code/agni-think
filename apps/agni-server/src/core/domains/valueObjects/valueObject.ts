import { IObjectEquality } from '@core/domains/interface/equality';

export default abstract class ValueObject implements IObjectEquality {
    abstract isEqual(object: ValueObject): boolean;
    abstract toJson(): string;
    abstract fromJson(string: string): ValueObject;
}