import { Period } from "../../constants";
import { Budget } from "../budget";
import { ValueError } from "../../../errors/valueError"

describe("Budget Class", () => {
    let budget: Budget;

    beforeEach(() => {
        budget = new Budget();
    });

    test("should initialize with default values", () => {
        expect(budget.getId()).toBe("");
        expect(budget.getTitle()).toBe("");
        expect(budget.getIsArchive()).toBe(false);
        expect(budget.getTarget()).toBe(0);
        expect(budget.getPeriod()).toBe(null);
        expect(budget.getPeriodTime()).toBe(0);
        expect(budget.hasChange()).toBe(false);
    });

    test("should update ID correctly", () => {
        budget.setId("123");
        expect(budget.getId()).toBe("123");
    });

    test("should update title and track changes", () => {
        budget.setTitle("New Budget");
        expect(budget.getTitle()).toBe("New Budget");
        expect(budget.hasChange()).toBe(true);
    });

    test("should update target and track changes", () => {
        budget.setTarget(500);
        expect(budget.getTarget()).toBe(500);
        expect(budget.hasChange()).toBe(true);
    });

    test("should update isArchived correctly", () => {
        budget.setIsArchive(true);
        expect(budget.getIsArchive()).toBe(true);
        expect(budget.hasChange()).toBe(true);
    });

    test("should add and remove categories correctly", () => {
        budget.setCategories(["Food", "Entertainment"]);
        expect(budget.hasChange()).toBe(true);

        expect(() => budget.addCategory("Food")).toThrow(ValueError);
        budget.deleteCategory("Food");
        expect(budget.hasChange()).toBe(true);
    });

    test("should update a set of categories correctly", () => {
        budget.setCategories(["Food", "Entertainment"])
        
        budget.setCategories(["Food", "Boobs"])
        expect(budget.__add_event_category[0]).toBe("Boobs")
        expect(budget.__delete_event_category[0]).toBe("Entertainment")
    })

    test("should add and remove tags correctly", () => {
        budget.setTags(["Urgent", "Work"]);
        expect(budget.hasChange()).toBe(true);

        expect(() => budget.addTag("Urgent")).toThrow(ValueError);
        budget.deleteTag("Urgent");
        expect(budget.hasChange()).toBe(true);
    });

    test("should set period and period time correctly", () => {
        budget.setPeriod(Period.MONTH);
        expect(budget.getPeriod()).toBe(Period.MONTH);
        expect(budget.hasChange()).toBe(true);

        budget.setPeriodTime(30);
        expect(budget.getPeriodTime()).toBe(30);
        expect(budget.hasChange()).toBe(true);
    });
});
