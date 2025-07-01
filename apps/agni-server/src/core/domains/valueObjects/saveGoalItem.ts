import { Money } from "../entities/money";
import { isStringDifferent } from "../helpers";
import ValueObject from "./valueObject";

export default class SaveGoalItem extends ValueObject {
    public title: string = ''
    public link: string = ''
    public price: Money = new Money(0)
    public htmlToTrack: string = ''

    isEqual(object: SaveGoalItem): boolean {
        if (isStringDifferent(this.title, object.title))
            return false

        if (isStringDifferent(this.link, object.link))
            return false

        if (this.price.getAmount() != object.price.getAmount())
            return false

        if (this.htmlToTrack != object.htmlToTrack)
            return false

        return true
    }

    toJson(): string {
        throw new Error("Method not implemented.");
    }

    fromJson(string: string): SaveGoalItem {
        throw new Error("Method not implemented.");
    }
} 