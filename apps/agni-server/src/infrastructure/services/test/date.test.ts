import moment from 'moment';
import { MomentDateService } from '../date';
import { Period } from '@core/domains/constants';

describe('MomentDateService', () => {
    let dateService: MomentDateService;

    beforeEach(() => {
        dateService = new MomentDateService();
    });

    test('formatDate should correctly format a valid date', () => {
        expect(dateService.formatDate('2025-04-05')).toBe('2025-04-05');
    });

    test('formatDate should throw error for an invalid date', () => {
        expect(() => dateService.formatDate('invalid-date')).toThrowError('invalid-date is not valid');
    });

    test('formatDateWithtime should correctly format a valid date with time', () => {
        const date = '2025-04-05T14:30';
        expect(dateService.formatDateWithtime(date)).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/);
    });

    test('getTodayWithTime should return current date and time in correct format', () => {
        expect(dateService.getTodayWithTime()).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/);
    });

    test('getToday should return current date in correct format', () => {
        expect(dateService.getToday()).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    });

    test('getDateAddition should correctly add periods to a date', () => {
        expect(dateService.getDateAddition('2025-04-05', Period.DAY, 5)).toBe(moment('2025-04-05').add(5, 'day').format('YYYY-MM-DD'));
        expect(dateService.getDateAddition('2025-04-05', Period.MONTH, 2)).toBe(moment('2025-04-05').add(2, 'month').format('YYYY-MM-DD'));
        expect(dateService.getDateAddition('2025-04-05', Period.WEEK, 3)).toBe(moment('2025-04-05').add(3, 'week').format('YYYY-MM-DD'));
        expect(dateService.getDateAddition('2025-04-05', Period.YEAR, 1)).toBe(moment('2025-04-05').add(1, 'year').format('YYYY-MM-DD'));
    });

    test('compareDate should return correct comparison values', () => {
        expect(dateService.compareDate('2025-04-05', '2025-04-06')).toBe(-1);
        expect(dateService.compareDate('2025-04-06', '2025-04-05')).toBe(1);
        expect(dateService.compareDate('2025-04-05', '2025-04-05')).toBe(0);
    });
});
