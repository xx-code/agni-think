// Scheduler.test.ts
import { describe, it, expect, vi, beforeEach } from "vitest";
import { Scheduler } from "../valueObjects/scheduleInfo";
import { Period } from "../constants";
import { MomentDateService } from "../entities/libs";

// On mock MomentDateService pour pas dépendre du temps réel
vi.mock("./MomentDateService", () => ({
  MomentDateService: {
    getUTCDateAddition: vi.fn(),
    compareDate: vi.fn(),
    compareDateWithDate: vi.fn(),
  },
}));

describe("Scheduler", () => {
    const startDate = new Date("2025-01-01T00:00:00Z");
    const endDate = new Date("2025-01-10T00:00:00Z");

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("doit lancer une erreur si ni periodTime ni endingDate n'est fourni", () => {
        expect(() => new Scheduler(Period.DAY, startDate)).toThrow(
        "SCHEDULER_WITH_PERIOD_UNDETERMINED_HAVE_NOT_END_DATE"
        );
    });

    it("crée un scheduler avec une période et periodTime", () => {
        const startDate = new Date("2024-12-30T05:00:00.000Z");

        // Use vi.spyOn to spy on the method.
        const getUTCDateAdditionSpy = vi.spyOn(MomentDateService, 'getUTCDateAddition');

        // Now, use vi.mocked to cast the spy to a mock type and use mockReturnValue.
        vi.mocked(getUTCDateAdditionSpy).mockReturnValue(
            new Date("2025-01-02T00:00:00Z")
        );        const scheduler = new Scheduler(Period.DAY, startDate, 1);

        expect(scheduler.getPeriod()).toBe(Period.DAY);
        expect(scheduler.getPeriodTime()).toBe(1);
        expect(scheduler.getStartedDate()).toEqual(startDate);
        expect(scheduler.getUpdatedDate()).toEqual(new Date("2025-01-02T00:00:00Z"));
    });

    it("crée un scheduler avec une date de fin", () => {
        const scheduler = new Scheduler(Period.DAY, startDate, undefined, endDate);

        expect(scheduler.getEndingDate()).toEqual(endDate);
        expect(scheduler.getUpdatedDate()).toEqual(endDate);
    });

    it("updateScheduler ajuste la date correctement", () => {
        const startDate = new Date("2025-01-05T00:00:00Z");

        // Use vi.spyOn to spy on the method.
        const getUTCDateAdditionSpy = vi.spyOn(MomentDateService, 'getUTCDateAddition');

        // Now, use vi.mocked to cast the spy to a mock type and use mockReturnValue.
        vi.mocked(getUTCDateAdditionSpy).mockReturnValue(
            new Date("2025-01-05T00:00:00Z")
        );       

        const scheduler = new Scheduler(Period.DAY, startDate, 1);
        scheduler.updateSheduler(Period.DAY, startDate, new Date("2025-01-02T00:00:00Z"), 4, endDate);

        expect(scheduler.getUpdatedDate()).toEqual(new Date("2025-01-05T00:00:00Z"));
    });

    it("isDue retourne true si la date courante >= updatedDate", () => {
        const startDate = new Date("2024-12-30T05:00:00.000Z");

        // Use vi.spyOn to spy on the method.
        const getUTCDateAdditionSpy = vi.spyOn(MomentDateService, 'getUTCDateAddition');
        const compareDateWithDate = vi.spyOn(MomentDateService, 'compareDateWithDate');

        // Now, use vi.mocked to cast the spy to a mock type and use mockReturnValue.
        vi.mocked(getUTCDateAdditionSpy).mockReturnValue(
            new Date("2025-01-02T00:00:00Z")
        );       
        vi.mocked(compareDateWithDate).mockReturnValue(1)

        const scheduler = new Scheduler(Period.DAY, startDate, 1);

        expect(scheduler.isDue()).toBe(true);
    });

    it("toJson / fromJson conserve les valeurs", () => {
        const scheduler = new Scheduler(Period.DAY, startDate, 2, endDate);
        const json = scheduler.toJson();

        const parsed = Scheduler.fromJson(JSON.parse(json));

        expect(parsed.getPeriod()).toBe(scheduler.getPeriod());
        expect(parsed.getPeriodTime()).toBe(scheduler.getPeriodTime());
        expect(parsed.getEndingDate()?.toISOString()).toBe(
        scheduler.getEndingDate()?.toISOString()
        );
    });
});
