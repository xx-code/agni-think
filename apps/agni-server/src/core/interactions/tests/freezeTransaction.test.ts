import { DateService, GetUID } from "@core/adapters/libs";
import { Money } from "@core/domains/entities/money";
import { Record } from "@core/domains/entities/record";
import { Transaction } from "@core/domains/entities/transaction";
import ValidationError from "@core/errors/validationError";
import { IAddFreezeBalancePresenter, AddFreezeBalanceUseCase, RequestNewFreezeBalance } from "@core/interactions/freezerBalance/addFreezeBalanceUseCase";
import { IAutoDeleteFreezeBalancePresenter, AutoDeleteFreezeBalanceUseCase } from "@core/interactions/freezerBalance/autoDeleteFreezeBalanceUseCase";
import { AccountRepository } from "@core/repositories/accountRepository";
import { CategoryRepository } from "@core/repositories/categoryRepository";
import { RecordRepository } from "@core/repositories/recordRepository";
import { TransactionRepository } from "@core/repositories/transactionRepository";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";

jest.mock('../../repositories/transactionRepository');
jest.mock('../../repositories/recordRepository');
jest.mock('../../repositories/categoryRepository');
jest.mock('../../repositories/accountRepository');
jest.mock('../../repositories/unitOfWorkRepository');
jest.mock('@/core/adapters/libs');

describe('AddFreezeBalanceUseCase', () => {
    let presenter: IAddFreezeBalancePresenter;
    let transactionRepo: TransactionRepository;
    let recordRepo: RecordRepository;
    let categoryRepo: CategoryRepository;
    let accountRepo: AccountRepository;
    let unitOfWork: UnitOfWorkRepository;
    let useCase: AddFreezeBalanceUseCase;

    beforeEach(() => {
        presenter = {
            success: jest.fn(),
            fail: jest.fn(),
        };

        transactionRepo = new TransactionRepository();
        recordRepo = new RecordRepository();
        categoryRepo = new CategoryRepository();
        accountRepo = new AccountRepository();
        unitOfWork = new UnitOfWorkRepository();

        useCase = new AddFreezeBalanceUseCase(
            transactionRepo,
            presenter,
            accountRepo,
            categoryRepo,
            recordRepo,
            unitOfWork
        );
    });

    it('should successfully freeze the balance when all conditions are met', async () => {
        // Arrange
        const request: RequestNewFreezeBalance = {
            accountRef: 'account123',
            endDate: '2025-03-01',
            amount: 100,
        };

        // Mock the repository methods
        accountRepo.isExistById = jest.fn().mockResolvedValue(true);
        categoryRepo.get = jest.fn().mockResolvedValue(null); // No category found, we will create a new one
        recordRepo.save = jest.fn().mockResolvedValue(true);
        transactionRepo.save = jest.fn().mockResolvedValue(true);
        unitOfWork.start = jest.fn();
        unitOfWork.commit = jest.fn();
        unitOfWork.rollback = jest.fn();

        // Act
        await useCase.execute(request);

        // Assert
        expect(accountRepo.isExistById).toHaveBeenCalledWith('account123');
        expect(categoryRepo.get).toHaveBeenCalledWith('freeze');
        expect(categoryRepo.save).toHaveBeenCalled();
        expect(recordRepo.save).toHaveBeenCalled();
        expect(transactionRepo.save).toHaveBeenCalled();
        expect(unitOfWork.commit).toHaveBeenCalled();
        expect(presenter.success).toHaveBeenCalledWith(true);
    });

    it('should fail when the account does not exist', async () => {
        // Arrange
        const request: RequestNewFreezeBalance = {
            accountRef: 'nonexistent_account',
            endDate: '2025-03-01',
            amount: 100,
        };

        // Mock account existence check
        accountRepo.isExistById = jest.fn().mockResolvedValue(false);

        // Act
        await useCase.execute(request);

        // Assert
        expect(accountRepo.isExistById).toHaveBeenCalledWith('nonexistent_account');
        expect(presenter.fail).toHaveBeenCalledWith(new ValidationError('Account don\'t existe'));
    });

    it('should fail when an error occurs during the transaction process', async () => {
        // Arrange
        const request: RequestNewFreezeBalance = {
            accountRef: 'account123',
            endDate: '2025-03-01',
            amount: 100,
        };

        // Mock repository methods
        accountRepo.isExistById = jest.fn().mockResolvedValue(true);
        categoryRepo.get = jest.fn().mockResolvedValue(null); // No category found
        recordRepo.save = jest.fn().mockRejectedValue(new Error('Database error')); // Simulate error in saving record

        // Act
        await useCase.execute(request);

        // Assert
        expect(accountRepo.isExistById).toHaveBeenCalledWith('account123');
        expect(categoryRepo.get).toHaveBeenCalledWith('freeze');
        expect(recordRepo.save).toHaveBeenCalled();
        expect(transactionRepo.save).not.toHaveBeenCalled();
        expect(unitOfWork.rollback).toHaveBeenCalled();
        expect(presenter.fail).toHaveBeenCalledWith(new Error('Database error'));
    });
});

jest.mock('../../repositories/transactionRepository');
jest.mock('../../repositories/recordRepository');
jest.mock('../../repositories/unitOfWorkRepository');
jest.mock('@/core/adapters/dateService');
jest.mock('@/core/adapters/libs');

describe('AutoDeleteFreezeBalanceUseCase', () => {
    let presenter: IAutoDeleteFreezeBalancePresenter;
    let transactionRepo: TransactionRepository;
    let recordRepo: RecordRepository;
    let unitOfWork: UnitOfWorkRepository;
    let dateService: DateService;
    let useCase: AutoDeleteFreezeBalanceUseCase;

    beforeEach(() => {
        presenter = {
            success: jest.fn(),
            fail: jest.fn(),
        };

        transactionRepo = new TransactionRepository();
        recordRepo = new RecordRepository();
        unitOfWork = new UnitOfWorkRepository();
        dateService = new DateService();

        useCase = new AutoDeleteFreezeBalanceUseCase(
            transactionRepo,
            recordRepo,
            unitOfWork,
            dateService,
            presenter
        );
    });

    it('should successfully delete freeze transactions and records', async () => {
        // Arrange
        const recordId = GetUID();
        const transactionId = GetUID();
        const today = dateService.getToday();

        const transaction = new Transaction(transactionId, 'account123', recordId, 'freeze');
        const record = new Record(recordId, new Money(100), today, 'DEBIT');

        // Mock the repository methods
        transactionRepo.getPaginations = jest.fn().mockResolvedValue({
            transactions: [transaction],
        });
        recordRepo.get = jest.fn().mockResolvedValue(record);
        recordRepo.delete = jest.fn().mockResolvedValue(true);
        transactionRepo.delete = jest.fn().mockResolvedValue(true);
        unitOfWork.start = jest.fn();
        unitOfWork.commit = jest.fn();
        unitOfWork.rollback = jest.fn();
        dateService.compareDate = jest.fn().mockReturnValue(0); // today >= record date

        // Act
        await useCase.execute();

        // Assert
        expect(transactionRepo.getPaginations).toHaveBeenCalledWith(
            -1, 1, null, { categories: ['freeze'], accounts: [], tags: [], startDate: '', endDate: '', type: null, minPrice: null, maxPrice: null }
        );
        expect(recordRepo.get).toHaveBeenCalledWith(recordId);
        expect(recordRepo.delete).toHaveBeenCalledWith(recordId);
        expect(transactionRepo.delete).toHaveBeenCalledWith(transactionId);
        expect(unitOfWork.commit).toHaveBeenCalled();
        expect(presenter.success).toHaveBeenCalledWith('done');
    });

    it('should fail when there is an error during record or transaction deletion', async () => {
        // Arrange
        const recordId = GetUID();
        const transactionId = GetUID();
        const today = dateService.getToday();

        const transaction = new Transaction(transactionId, 'account123', recordId, 'freeze');
        const record = new Record(recordId, new Money(100), today, 'DEBIT');

        // Mock the repository methods
        transactionRepo.getPaginations = jest.fn().mockResolvedValue({
            transactions: [transaction],
        });
        recordRepo.get = jest.fn().mockResolvedValue(record);
        recordRepo.delete = jest.fn().mockRejectedValue(new Error('Delete error'));
        transactionRepo.delete = jest.fn().mockResolvedValue(true);
        unitOfWork.start = jest.fn();
        unitOfWork.commit = jest.fn();
        unitOfWork.rollback = jest.fn();
        dateService.compareDate = jest.fn().mockReturnValue(0); // today >= record date

        // Act
        await useCase.execute();

        // Assert
        expect(transactionRepo.getPaginations).toHaveBeenCalledWith(
            -1, 1, null, { categories: ['freeze'], accounts: [], tags: [], startDate: '', endDate: '', type: null, minPrice: null, maxPrice: null }
        );
        expect(recordRepo.get).toHaveBeenCalledWith(recordId);
        expect(recordRepo.delete).toHaveBeenCalledWith(recordId);
        expect(transactionRepo.delete).not.toHaveBeenCalled(); // transaction delete shouldn't be called since record deletion failed
        expect(unitOfWork.rollback).toHaveBeenCalled();
        expect(presenter.fail).toHaveBeenCalledWith(new Error('Delete error'));
    });

    it('should handle empty transactions list', async () => {
        // Arrange
        transactionRepo.getPaginations = jest.fn().mockResolvedValue({
            transactions: [],
        });

        // Act
        await useCase.execute();

        // Assert
        expect(transactionRepo.getPaginations).toHaveBeenCalledWith(
            -1, 1, null, { categories: ['freeze'], accounts: [], tags: [], startDate: '', endDate: '', type: null, minPrice: null, maxPrice: null }
        );
        expect(presenter.success).toHaveBeenCalledWith('done'); // No transactions to delete, so success still called
    });
});