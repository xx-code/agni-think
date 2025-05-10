import { AccountType } from "@core/domains/constants";
import { Account } from "../account";

describe("Account Class", () => {
    let account: Account;

    beforeEach(() => {
        account = new Account("123", "Test Account", AccountType.CHECKING);
    });

    test("should initialize with given id and title", () => {
        expect(account.getId()).toBe("123");
        expect(account.getTitle()).toBe("Test Account");
        expect(account.getIsSaving()).toBe(false);
        expect(account.hasChange()).toBe(false);
    });

    test("should update id correctly", () => {
        account.setId("456");
        expect(account.getId()).toBe("456");
    });

    test("should update title and track changes", () => {
        expect(account.hasChange()).toBe(false);
        account.setTitle("New Title");
        expect(account.getTitle()).toBe("New Title");
        expect(account.hasChange()).toBe(true);
    });

    test("should not mark change if the same title is set", () => {
        account.setTitle("Test Account");
        expect(account.hasChange()).toBe(false);
    });

    test("should update isSaving correctly", () => {
        account.setIsSaving(true);
        expect(account.getIsSaving()).toBe(true);
        account.setIsSaving(false);
        expect(account.getIsSaving()).toBe(false);
    });
});
