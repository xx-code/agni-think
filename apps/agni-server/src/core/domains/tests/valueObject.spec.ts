import { describe, it, expect, vi, beforeEach } from 'vitest';
import ValueObject from '../valueObjects/valueObject';
import { ValueObjectCollection } from '../valueObjects/collection';

// Simule un ValueObject simple
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

describe('ValueObjectCollection', () => {
  let collection: ValueObjectCollection<MockValueObject>;
  let onChangeMock: ReturnType<typeof vi.fn>;

  const vo = (val: string) => new MockValueObject(val);

  beforeEach(() => {
    onChangeMock = vi.fn();
    collection = new ValueObjectCollection([vo('a'), vo('b')], onChangeMock);
  });

  it('should return initial values', () => {
    const values = collection.get();
    expect(values).toHaveLength(2);
    expect(values.map(v => v.value)).toEqual(['a', 'b']);
  });

  it('should add a value and trigger onChange', () => {
    collection.add(vo('c'));
    const values = collection.get();
    expect(values.map(v => v.value)).toContain('c');
    expect(collection.__added_object.map(v => v.value)).toContain('c');
    expect(onChangeMock).toHaveBeenCalled();
  });

  it('should throw if added value already exists', () => {
    expect(() => collection.add(vo('a'))).toThrowError('VALUE_ALREADY_EXIST');
  });

  it('should delete a value and trigger onChange', () => {
    collection.delete(vo('a'));
    const values = collection.get();
    expect(values.map(v => v.value)).not.toContain('a');
    expect(collection.__deleted_object.map(v => v.value)).toContain('a');
    expect(onChangeMock).toHaveBeenCalled();
  });

  it('should throw if trying to delete unknown value', () => {
    expect(() => collection.delete(vo('x'))).toThrowError('VALUE_NOT_FOUND');
  });

  it('should update a value and track the change', () => {
    const newVo = vo('a'); // same key, assume update for the test
    collection.update(newVo);

    expect(collection.__added_object).toContain(newVo);
    expect(collection.__deleted_object.map(v => v.value)).toContain('a');
    expect(onChangeMock).toHaveBeenCalled();
  });

  it('should throw if trying to update non-existing value', () => {
    expect(() => collection.update(vo('x'))).toThrowError('VALUE_NOT_FOUND');
  });

  it('should detect changes in set()', () => {
    collection.set([vo('b'), vo('c')]);

    expect(collection.get().map(v => v.value)).toEqual(['b', 'c']);
    expect(collection.__added_object.map(v => v.value)).toContain('c');
    expect(collection.__deleted_object.map(v => v.value)).toContain('a');
    expect(onChangeMock).toHaveBeenCalled();
  });

  it('should not trigger onChange if set has same values', () => {
    collection.set([vo('a'), vo('b')]);
    expect(onChangeMock).not.toHaveBeenCalled();
  });

  it('should clear changes', () => {
    collection.add(vo('x'));
    collection.delete(vo('a'));
    collection.clearChanges();
    expect(collection.__added_object).toEqual([]);
    expect(collection.__deleted_object).toEqual([]);
  });

  it('should return correct changes via getChanges()', () => {
    collection.add(vo('x'));
    collection.delete(vo('a'));

    const changes = collection.getChanges();
    expect(changes.added.map(v => v.value)).toContain('x');
    expect(changes.deleted.map(v => v.value)).toContain('a');
  });
});
