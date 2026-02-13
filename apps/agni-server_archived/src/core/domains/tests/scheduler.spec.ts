// Scheduler.test.ts
import { describe, it, expect, beforeEach } from "vitest";
import { Scheduler, SchedulerRecurrence } from "../valueObjects/scheduleInfo";
import { Period } from "../constants";

describe("Scheduler", () => {
    const due_date = new Date("2025-01-01T00:00:00Z");
    const futureDate = new Date("2025-02-01T00:00:00Z");

    beforeEach(() => {
        // Setup
    });

    it("crée un scheduler avec une due_date uniquement", () => {
        const scheduler = new Scheduler(due_date);

        expect(scheduler.dueDate).toEqual(due_date);
        expect(scheduler.repeater).toBeUndefined();
    });

    it("crée un scheduler avec une due_date et repeater", () => {
        const repeater: SchedulerRecurrence = {
            period: Period.MONTH,
            interval: 1
        };
        const scheduler = new Scheduler(due_date, repeater);

        expect(scheduler.dueDate).toEqual(due_date);
        expect(scheduler.repeater).toEqual(repeater);
        expect(scheduler.repeater?.period).toBe(Period.MONTH);
        expect(scheduler.repeater?.interval).toBe(1);
    });

    it("isEqual compare correctement deux schedulers", () => {
        const repeater: SchedulerRecurrence = {
            period: Period.DAY,
            interval: 1
        };
        const scheduler1 = new Scheduler(due_date, repeater);
        const scheduler2 = new Scheduler(due_date, repeater);
        const scheduler3 = new Scheduler(futureDate, repeater);

        expect(scheduler1.isEqual(scheduler2)).toBe(true);
        expect(scheduler1.isEqual(scheduler3)).toBe(false);
    });

    it("isEqual retourne false si repeater est différent", () => {
        const repeater1: SchedulerRecurrence = {
            period: Period.DAY,
            interval: 1
        };
        const repeater2: SchedulerRecurrence = {
            period: Period.DAY,
            interval: 2
        };
        const scheduler1 = new Scheduler(due_date, repeater1);
        const scheduler2 = new Scheduler(due_date, repeater2);

        expect(scheduler1.isEqual(scheduler2)).toBe(false);
    });

    it("toJson sérialise correctement sans repeater", () => {
        const scheduler = new Scheduler(due_date);
        const json = scheduler.toJson();
        const parsed = JSON.parse(json);

        expect(parsed.due_date).toBeDefined();
        expect(parsed.repeater).toBeUndefined();
    });

    it("toJson sérialise correctement avec repeater", () => {
        const repeater: SchedulerRecurrence = {
            period: Period.WEEK,
            interval: 2
        };
        const scheduler = new Scheduler(due_date, repeater);
        const json = scheduler.toJson();
        const parsed = JSON.parse(json);

        expect(parsed.due_date).toBeDefined();
        expect(parsed.repeater.period).toBe(Period.WEEK);
        expect(parsed.repeater.interval).toBe(2);
    });

    it("fromJson reconstruit un scheduler sans repeater", () => {
        const scheduler = new Scheduler(due_date);
        const json = scheduler.toJson();

        const parsed = Scheduler.fromJson(JSON.parse(json));

        expect(parsed.dueDate.toISOString()).toBe(due_date.toISOString());
        expect(parsed.repeater).toBeUndefined();
    });

    it("fromJson reconstruit un scheduler avec repeater", () => {
        const repeater: SchedulerRecurrence = {
            period: Period.MONTH,
            interval: 3
        };
        const scheduler = new Scheduler(due_date, repeater);
        const json = scheduler.toJson();

        const parsed = Scheduler.fromJson(JSON.parse(json));

        expect(parsed.dueDate.toISOString()).toBe(due_date.toISOString());
        expect(parsed.repeater?.period).toBe(Period.MONTH);
        expect(parsed.repeater?.interval).toBe(3);
    });

    it("fromJson lance une erreur avec un JSON invalide", () => {
        const invalidJson = { invalid: "data" };

        expect(() => Scheduler.fromJson(invalidJson)).toThrow();
    });

    it("toJson / fromJson conserve les valeurs", () => {
        const repeater: SchedulerRecurrence = {
            period: Period.DAY,
            interval: 5
        };
        const scheduler = new Scheduler(due_date, repeater);
        const json = scheduler.toJson();
        const parsed = Scheduler.fromJson(JSON.parse(json));

        expect(parsed.isEqual(scheduler)).toBe(true);
    });
});
