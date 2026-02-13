import { describe, it, expect, vi } from "vitest";
import { MomentDateService } from "../entities/libs";
import { Period } from "../constants";

describe("MomentDateService", () => {
    const baseDate = new Date(2025, 0, 1, 0, 0, 0);

    it("periodMatcherToMoment retourne le bon format", () => {
        expect(MomentDateService.periodMatcherToMoment(Period.DAY)).toBe("days");
        expect(MomentDateService.periodMatcherToMoment(Period.WEEK)).toBe("weeks");
        expect(MomentDateService.periodMatcherToMoment(Period.MONTH)).toBe("months");
        expect(MomentDateService.periodMatcherToMoment(Period.YEAR)).toBe("years");
    });

    it("formatDate formate une date ISO en Date", () => {
        const result = MomentDateService.formatDate("2025-01-15");
        expect(result.toISOString()).toBe("2025-01-15T00:00:00.000Z");
    });

    it("formatDateStr retourne une string formatée", () => {
        const result = MomentDateService.formatDateStr("2025-01-15");
        expect(result).toBe("2025-01-15");
    });

    // it("formatDateWithtime conserve l'heure", () => {
    //     const result = MomentDateService.formatDateWithtime("2025-01-01T12:30");
    //     expect(result.toISOString()).toBe("2025-01-01T12:30:00.000Z");
    // });

    it("getToday retourne la date du jour sans heure", () => {
        const today = MomentDateService.getToday();
        const iso = today.toISOString().split("T")[0];
        const expected = new Date().toISOString().split("T")[0];
        expect(iso).toBe(expected);
    });

    it("getTodayWithTime retourne la date du jour avec l'heure", () => {
        const today = MomentDateService.getTodayWithTime();
        expect(today).toBeInstanceOf(Date);
    });

    it("getUTCDateAddition ajoute la période correctement", () => {
        const result = MomentDateService.getUTCDateAddition(baseDate, Period.DAY, 5);
        const date = new Date()
        const verify = new Date(date.getFullYear(), date.getMonth(), date.getDate() + 5)
        expect(result.toISOString()).toBe(verify.toISOString());
    });

    it("getUTCDateSubstraction soustrait la période correctement", () => {
        const result = MomentDateService.getUTCDateSubstraction(baseDate, Period.DAY, 2);
        const date = new Date()
        const verify = new Date(date.getFullYear(), date.getMonth(), date.getDate() - 2)
        expect(result.toISOString()).toBe('2024-12-30T05:00:00.000Z');
    });

    it("compareDate compare correctement deux dates", () => {
        const d1 = new Date("2025-01-01T00:00:00Z");
        const d2 = new Date("2025-01-02T00:00:00Z");

        expect(MomentDateService.compareDate(d1, d2)).toBe(-1);
        expect(MomentDateService.compareDate(d2, d1)).toBe(1);
        expect(MomentDateService.compareDate(d1, d1)).toBe(0);
    });

    it("compareDateWithDate compare correctement deux dates UTC", () => {
        const d1 = new Date("2025-01-01T00:00:00Z");
        const d2 = new Date("2025-01-02T00:00:00Z");

        expect(MomentDateService.compareDateWithDate(d1, d2)).toBe(-1);
        expect(MomentDateService.compareDateWithDate(d2, d1)).toBe(1);
        expect(MomentDateService.compareDateWithDate(d1, d1)).toBe(0);
    });

    it("getUTCDateByPeriod retourne start et end corrects", () => {
        const result = MomentDateService.getUTCDateByPeriod(
        new Date("2025-01-15"),
        Period.MONTH,
        1);
        expect(result.startDate.toISOString()).toBe("2025-01-01T00:00:00.000Z");
        expect(result.endDate.toISOString()).toBe("2025-01-31T00:00:00.000Z");
    });

});
