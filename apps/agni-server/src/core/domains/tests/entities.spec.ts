import { stringify } from "querystring";
import Entity, { TrackableProperty } from "../entities/entity";
import ValueObject from "../valueObjects/valueObject";

class MockValueObject extends ValueObject {
    public value: string;
    constructor(value: string) {
        super()
        this.value = value;
    }

    isEqual(other: MockValueObject): boolean {
        return this.value.toLowerCase().trim() === other.value.toLocaleLowerCase().trim();
    }
}


export class DumbEntity extends Entity {
  private name: TrackableProperty<string>;
  private status: TrackableProperty<MockValueObject>;

  constructor(id: string, name: string, status: MockValueObject) {
    super(id);

    this.name = new TrackableProperty(name, () => this.markHasChange());
    this.status = new TrackableProperty(status, () => this.markHasChange());
  }

  isEqual(object: DumbEntity): boolean {
    if (!super.isEqual(object))
        return false;

    if (!(this.name.get().toLocaleLowerCase().trim() === object.getName().toLocaleLowerCase().trim()))
        return false;

    if (!this.status.get().isEqual(object.getStatus()))
        return false;

    return true;
  }

  public setName(newName: string) {
    this.name.set(newName);
  }

  public getName(): string {
    return this.name.get();
  }

  public setStatus(newStatus: MockValueObject) {
    this.status.set(newStatus, (a, b) => !a.isEqual(b));
  }

  public getStatus(): MockValueObject {
    return this.status.get();
  }
}

describe('DumbEntity', () => {
  it('should initialize correctly', () => {
    const vo = new MockValueObject('ACTIVE');
    const entity = new DumbEntity('id-1', 'Alice', vo);

    expect(entity.getId()).toBe('id-1');
    expect(entity.getName()).toBe('Alice');
    expect(entity.getStatus().value).toBe('ACTIVE');
    expect(entity.hasChange()).toBe(false);
  });

  it('should mark change when name is changed', () => {
    const entity = new DumbEntity('id-1', 'Alice', new MockValueObject('ACTIVE'));
    entity.setName('Bob');

    expect(entity.getName()).toBe('Bob');
    expect(entity.hasChange()).toBe(true);
  });

  it('should mark change when status is changed', () => {
    const entity = new DumbEntity('id-1', 'Alice', new MockValueObject('ACTIVE'));
    entity.setStatus(new MockValueObject('INACTIVE'));

    expect(entity.getStatus().value).toBe('INACTIVE');
    expect(entity.hasChange()).toBe(true);
  });

  it('should not mark change when status is unchanged', () => {
    const status = new MockValueObject('ACTIVE');
    const entity = new DumbEntity('id-1', 'Alice', status);
    entity.setStatus(new MockValueObject('ACTIVE'));

    expect(entity.hasChange()).toBe(false);
  });

  it('should reset change state', () => {
    const entity = new DumbEntity('id-1', 'Alice', new MockValueObject('ACTIVE'));
    entity.setName('Bob');
    expect(entity.hasChange()).toBe(true);
    entity.resetChangeState();
    expect(entity.hasChange()).toBe(false);
  });

  it('should compare equality with another entity', () => {
    const e1 = new DumbEntity('id-1', 'Alice', new MockValueObject('ACTIVE'));
    const e2 = new DumbEntity('id-1', 'ALice', new MockValueObject('ACTIVE'));
    const e3 = new DumbEntity('id-2', 'Alice', new MockValueObject('ACTIVE'));

    expect(e1.isEqual(e2)).toBe(true);   // same ID
    expect(e1.isEqual(e3)).toBe(false);  // different ID
  });
});