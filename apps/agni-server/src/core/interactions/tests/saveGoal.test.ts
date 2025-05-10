import { DateService } from "@core/adapters/libs";
import { SAVING_CATEGORY_ID } from "@core/domains/constants";
import { Account } from "@core/domains/entities/account";
import { Money } from "@core/domains/entities/money";
import { Record, TransactionType } from "@core/domains/entities/record";
import { SaveGoal } from "@core/domains/entities/saveGoal";
import { Transaction } from "@core/domains/entities/transaction";
import ValidationError from "@core/errors/validationError";
import { AccountRepository } from "@core/repositories/accountRepository";
import { RecordRepository } from "@core/repositories/recordRepository";
import { SavingRepository } from "@core/repositories/savingRepository";
import { TransactionRepository } from "@core/repositories/transactionRepository";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { AddSaveGoalUseCase, RequestAddSaveGoalUseCase } from "../saveGoal/addSaveGoal";
import { DeleteSaveGoalUseCase, RequestDeleteSaveGoal } from "../saveGoal/deleteSaveGoal";
import { GetSaveGoalUseCase } from "../saveGoal/getSaveGoal";
import { IncreaseSaveGoalUseCase, RequestIncreaseSaveGoal } from "../saveGoal/increaseSaveGoal";
import { IUpdateSaveGoalPresenter, UpdateSaveGoalUseCase, RequestUpdateSaveGoalUseCase } from "../saveGoal/updateSaveGoal";


jest.mock("../../repositories/savingRepository");
jest.mock("../../repositories/accountRepository");
jest.mock("@/core/repositories/unitOfWorkRepository");

describe("AddSaveGoalUseCase", () => {
    let mockSavingRepo: jest.Mocked<SavingRepository>;
    let mockAccountRepo: jest.Mocked<AccountRepository>;
    let mockUnitOfWork: jest.Mocked<UnitOfWorkRepository>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };
    let useCase: AddSaveGoalUseCase;

    beforeEach(() => {
        mockSavingRepo = new SavingRepository() as jest.Mocked<SavingRepository>;
        mockAccountRepo = new AccountRepository() as jest.Mocked<AccountRepository>;
        mockUnitOfWork = new UnitOfWorkRepository() as jest.Mocked<UnitOfWorkRepository>;
        mockPresenter = { success: jest.fn(), fail: jest.fn() };

        useCase = new AddSaveGoalUseCase(mockSavingRepo, mockUnitOfWork, mockAccountRepo, mockPresenter);
    });

    it("should successfully add a save goal with items", async () => {
        const request: RequestAddSaveGoalUseCase = {
            target: 1000,
            title: "New Goal",
            description: "A description of the save goal.",
            items: [
                { title: "Item 1", price: 200, link: "http://example.com/item1", htmlToTrack: "<div></div>" },
                { title: "Item 2", price: 300, link: "http://example.com/item2", htmlToTrack: "<div></div>" },
            ]
        };

        const newAccount = {
            getId: () => "account1",
            setIsSaving: jest.fn()
        };

        const newSaveGoal = {
            getId: () => "saveGoal1",
        };

        mockAccountRepo.save.mockResolvedValueOnce(newAccount);
        mockSavingRepo.create.mockResolvedValueOnce(newSaveGoal);

        await useCase.execute(request);

        expect(mockUnitOfWork.start).toHaveBeenCalled();
        expect(mockAccountRepo.save).toHaveBeenCalledWith(expect.any(Account));
        expect(mockSavingRepo.create).toHaveBeenCalledWith(expect.any(SaveGoal));
        expect(mockUnitOfWork.commit).toHaveBeenCalled();
        expect(mockPresenter.success).toHaveBeenCalledWith(true);
    });

    it("should fail if account save fails", async () => {
        const request: RequestAddSaveGoalUseCase = {
            target: 1000,
            title: "New Goal",
            description: "A description of the save goal.",
            items: [
                { title: "Item 1", price: 200, link: "http://example.com/item1", htmlToTrack: "<div></div>" },
            ]
        };

        const error = new Error("Account save failed");
        mockAccountRepo.save.mockRejectedValueOnce(error);

        await useCase.execute(request);

        expect(mockUnitOfWork.start).toHaveBeenCalled();
        expect(mockUnitOfWork.rollback).toHaveBeenCalled();
        expect(mockPresenter.fail).toHaveBeenCalledWith(error);
    });

    it("should fail if save goal creation fails", async () => {
        const request: RequestAddSaveGoalUseCase = {
            target: 1000,
            title: "New Goal",
            description: "A description of the save goal.",
            items: [
                { title: "Item 1", price: 200, link: "http://example.com/item1", htmlToTrack: "<div></div>" },
            ]
        };

        const newAccount = {
            getId: () => "account1",
            setIsSaving: jest.fn()
        };

        const error = new Error("Save goal creation failed");
        mockAccountRepo.save.mockResolvedValueOnce(newAccount);
        mockSavingRepo.create.mockRejectedValueOnce(error);

        await useCase.execute(request);

        expect(mockUnitOfWork.start).toHaveBeenCalled();
        expect(mockUnitOfWork.rollback).toHaveBeenCalled();
        expect(mockPresenter.fail).toHaveBeenCalledWith(error);
    });

    it("should call rollback if any error occurs", async () => {
        const request: RequestAddSaveGoalUseCase = {
            target: 1000,
            title: "New Goal",
            description: "A description of the save goal.",
            items: [
                { title: "Item 1", price: 200, link: "http://example.com/item1", htmlToTrack: "<div></div>" },
            ]
        };

        const error = new Error("Unexpected error");
        mockAccountRepo.save.mockRejectedValueOnce(error);

        await useCase.execute(request);

        expect(mockUnitOfWork.start).toHaveBeenCalled();
        expect(mockUnitOfWork.rollback).toHaveBeenCalled();
        expect(mockPresenter.fail).toHaveBeenCalledWith(error);
    });
});

// Mocking repositories and dependencies
jest.mock("../../repositories/savingRepository");
jest.mock("../../repositories/accountRepository");
jest.mock("../../repositories/transactionRepository");
jest.mock("../../repositories/recordRepository");
jest.mock("@/core/repositories/unitOfWorkRepository");

describe("IncreaseSaveGoalUseCase", () => {
    let mockSavingRepo: jest.Mocked<SavingRepository>;
    let mockAccountRepo: jest.Mocked<AccountRepository>;
    let mockTransactionRepo: jest.Mocked<TransactionRepository>;
    let mockRecordRepo: jest.Mocked<RecordRepository>;
    let mockUnitOfWork: jest.Mocked<UnitOfWorkRepository>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };
    let mockDateService: jest.Mocked<DateService>;

    let useCase: IncreaseSaveGoalUseCase;

    beforeEach(() => {
        mockSavingRepo = new SavingRepository() as jest.Mocked<SavingRepository>;
        mockAccountRepo = new AccountRepository() as jest.Mocked<AccountRepository>;
        mockTransactionRepo = new TransactionRepository() as jest.Mocked<TransactionRepository>;
        mockRecordRepo = new RecordRepository() as jest.Mocked<RecordRepository>;
        mockUnitOfWork = new UnitOfWorkRepository() as jest.Mocked<UnitOfWorkRepository>;
        mockDateService = { getTodayWithTime: jest.fn().mockReturnValue("2025-03-09T12:00:00Z") } as jest.Mocked<DateService>;
        mockPresenter = { success: jest.fn(), fail: jest.fn() };

        useCase = new IncreaseSaveGoalUseCase(mockPresenter, mockAccountRepo, mockSavingRepo, mockTransactionRepo, mockDateService, mockRecordRepo, mockUnitOfWork);
    });

    it("should successfully increase save goal", async () => {
        const request: RequestIncreaseSaveGoal = {
            savingGoalRef: "savingGoal1",
            accountRef: "account1",
            increaseAmount: 500
        };

        const mockSavingGoal = {
            getAccountRef: () => "account2",
            getTitle: () => "My Goal"
        };

        const mockAccount = {
            getId: () => "account1"
        };

        const accountBalance = new Money(1000); // Account balance is sufficient
        mockSavingRepo.get.mockResolvedValueOnce(mockSavingGoal);
        mockAccountRepo.get.mockResolvedValueOnce(mockAccount);
        mockTransactionRepo.getAccountBalance.mockResolvedValueOnce(accountBalance);

        const newRecord = new Record("record1", new Money(500), "2025-03-09T12:00:00Z", TransactionType.DEBIT);
        mockRecordRepo.save.mockResolvedValueOnce(newRecord);

        const newTransaction = new Transaction("trans1", "account1", "record1", SAVING_CATEGORY_ID);
        mockTransactionRepo.save.mockResolvedValueOnce(newTransaction);

        await useCase.execute(request);

        expect(mockUnitOfWork.start).toHaveBeenCalled();
        expect(mockSavingRepo.get).toHaveBeenCalledWith(request.savingGoalRef);
        expect(mockAccountRepo.get).toHaveBeenCalledWith(request.accountRef);
        expect(mockTransactionRepo.getAccountBalance).toHaveBeenCalledWith(mockAccount.getId());
        expect(mockRecordRepo.save).toHaveBeenCalledTimes(2);
        expect(mockTransactionRepo.save).toHaveBeenCalledTimes(2);
        expect(mockUnitOfWork.commit).toHaveBeenCalled();
        expect(mockPresenter.success).toHaveBeenCalledWith(true);
    });

    it("should fail if account balance is insufficient", async () => {
        const request: RequestIncreaseSaveGoal = {
            savingGoalRef: "savingGoal1",
            accountRef: "account1",
            increaseAmount: 1500 // Insufficient balance
        };

        const mockSavingGoal = {
            getAccountRef: () => "account2",
            getTitle: () => "My Goal"
        };

        const mockAccount = {
            getId: () => "account1"
        };

        const accountBalance = new Money(1000); // Account balance is less than requested increase
        mockSavingRepo.get.mockResolvedValueOnce(mockSavingGoal);
        mockAccountRepo.get.mockResolvedValueOnce(mockAccount);
        mockTransactionRepo.getAccountBalance.mockResolvedValueOnce(accountBalance);

        await useCase.execute(request);

        expect(mockUnitOfWork.start).toHaveBeenCalled();
        expect(mockUnitOfWork.rollback).toHaveBeenCalled();
        expect(mockPresenter.fail).toHaveBeenCalledWith(new ValidationError('Price must be smaller than account'));
    });

    it("should fail if an error occurs during repository operations", async () => {
        const request: RequestIncreaseSaveGoal = {
            savingGoalRef: "savingGoal1",
            accountRef: "account1",
            increaseAmount: 500
        };

        const error = new Error("Database error");
        mockSavingRepo.get.mockRejectedValueOnce(error);

        await useCase.execute(request);

        expect(mockUnitOfWork.start).toHaveBeenCalled();
        expect(mockUnitOfWork.rollback).toHaveBeenCalled();
        expect(mockPresenter.fail).toHaveBeenCalledWith(error);
    });
})

// Mocking repositories and dependencies
jest.mock("../../repositories/savingRepository");
jest.mock("../../repositories/accountRepository");
jest.mock("../../repositories/transactionRepository");
jest.mock("@/core/repositories/unitOfWorkRepository");

describe("DeleteSaveGoalUseCase", () => {
    let mockSavingRepo: jest.Mocked<SavingRepository>;
    let mockAccountRepo: jest.Mocked<AccountRepository>;
    let mockTransactionRepo: jest.Mocked<TransactionRepository>;
    let mockUnitOfWork: jest.Mocked<UnitOfWorkRepository>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };

    let useCase: DeleteSaveGoalUseCase;

    beforeEach(() => {
        mockSavingRepo = new SavingRepository() as jest.Mocked<SavingRepository>;
        mockAccountRepo = new AccountRepository() as jest.Mocked<AccountRepository>;
        mockTransactionRepo = new TransactionRepository() as jest.Mocked<TransactionRepository>;
        mockUnitOfWork = new UnitOfWorkRepository() as jest.Mocked<UnitOfWorkRepository>;
        mockPresenter = { success: jest.fn(), fail: jest.fn() };

        useCase = new DeleteSaveGoalUseCase({
            savingRepository: mockSavingRepo,
            accountRepository: mockAccountRepo,
            transactionRepository: mockTransactionRepo,
            unitOfWorkRepository: mockUnitOfWork
        }, mockPresenter);
    });

    it("should successfully delete a save goal and transfer transactions", async () => {
        const request: RequestDeleteSaveGoal = {
            saveGoalRef: "goal1",
            accountTranfertRef: "account2"
        };

        const mockSavingGoal = {
            getId: () => "goal1",
            getAccountRef: () => "account1",
        };

        const mockAccountTransfer = {
            getId: () => "account2"
        };

        const mockTransaction = new Transaction("trans1", "account1", "record1", SAVING_CATEGORY_ID);

        mockSavingRepo.get.mockResolvedValueOnce(mockSavingGoal);
        mockAccountRepo.get.mockResolvedValueOnce(mockAccountTransfer);
        mockTransactionRepo.getPaginations.mockResolvedValueOnce({
            transactions: [mockTransaction]
        });

        mockTransactionRepo.update.mockResolvedValueOnce(mockTransaction);
        mockAccountRepo.delete.mockResolvedValueOnce(null);
        mockSavingRepo.delete.mockResolvedValueOnce(null);

        await useCase.execute(request);

        expect(mockUnitOfWork.start).toHaveBeenCalled();
        expect(mockTransactionRepo.getPaginations).toHaveBeenCalledWith(-1, 1, null, {
            accounts: ["account1"],
            categories: [],
            tags: [],
            startDate: '',
            endDate: '',
            minPrice: null,
            maxPrice: null,
            type: null
        });
        expect(mockTransactionRepo.update).toHaveBeenCalledTimes(1);
        expect(mockAccountRepo.delete).toHaveBeenCalledWith("account1");
        expect(mockSavingRepo.delete).toHaveBeenCalledWith("goal1");
        expect(mockUnitOfWork.commit).toHaveBeenCalled();
        expect(mockPresenter.success).toHaveBeenCalledWith(true);
    });

    it("should fail if an error occurs during transaction transfer", async () => {
        const request: RequestDeleteSaveGoal = {
            saveGoalRef: "goal1",
            accountTranfertRef: "account2"
        };

        const mockSavingGoal = {
            getId: () => "goal1",
            getAccountRef: () => "account1",
        };

        const mockAccountTransfer = {
            getId: () => "account2"
        };

        const mockTransaction = new Transaction("trans1", "account1", "record1", SAVING_CATEGORY_ID);

        mockSavingRepo.get.mockResolvedValueOnce(mockSavingGoal);
        mockAccountRepo.get.mockResolvedValueOnce(mockAccountTransfer);
        mockTransactionRepo.getPaginations.mockResolvedValueOnce({
            transactions: [mockTransaction]
        });

        mockTransactionRepo.update.mockRejectedValueOnce(new Error("Transaction transfer error"));

        await useCase.execute(request);

        expect(mockUnitOfWork.start).toHaveBeenCalled();
        expect(mockUnitOfWork.rollback).toHaveBeenCalled();
        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error("Transaction transfer error"));
    });

    it("should fail if an error occurs during account deletion", async () => {
        const request: RequestDeleteSaveGoal = {
            saveGoalRef: "goal1",
            accountTranfertRef: "account2"
        };

        const mockSavingGoal = {
            getId: () => "goal1",
            getAccountRef: () => "account1",
        };

        const mockAccountTransfer = {
            getId: () => "account2"
        };

        const mockTransaction = new Transaction("trans1", "account1", "record1", SAVING_CATEGORY_ID);

        mockSavingRepo.get.mockResolvedValueOnce(mockSavingGoal);
        mockAccountRepo.get.mockResolvedValueOnce(mockAccountTransfer);
        mockTransactionRepo.getPaginations.mockResolvedValueOnce({
            transactions: [mockTransaction]
        });

        mockTransactionRepo.update.mockResolvedValueOnce(mockTransaction);
        mockAccountRepo.delete.mockRejectedValueOnce(new Error("Account deletion error"));

        await useCase.execute(request);

        expect(mockUnitOfWork.start).toHaveBeenCalled();
        expect(mockUnitOfWork.rollback).toHaveBeenCalled();
        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error("Account deletion error"));
    });

    it("should fail if an error occurs during saving goal deletion", async () => {
        const request: RequestDeleteSaveGoal = {
            saveGoalRef: "goal1",
            accountTranfertRef: "account2"
        };

        const mockSavingGoal = {
            getId: () => "goal1",
            getAccountRef: () => "account1",
        };

        const mockAccountTransfer = {
            getId: () => "account2"
        };

        const mockTransaction = new Transaction("trans1", "account1", "record1", SAVING_CATEGORY_ID);

        mockSavingRepo.get.mockResolvedValueOnce(mockSavingGoal);
        mockAccountRepo.get.mockResolvedValueOnce(mockAccountTransfer);
        mockTransactionRepo.getPaginations.mockResolvedValueOnce({
            transactions: [mockTransaction]
        });

        mockTransactionRepo.update.mockResolvedValueOnce(mockTransaction);
        mockAccountRepo.delete.mockResolvedValueOnce(null);
        mockSavingRepo.delete.mockRejectedValueOnce(new Error("Save goal deletion error"));

        await useCase.execute(request);

        expect(mockUnitOfWork.start).toHaveBeenCalled();
        expect(mockUnitOfWork.rollback).toHaveBeenCalled();
        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error("Save goal deletion error"));
    });
});

// Mocking the repositories and dependencies
jest.mock("../../repositories/savingRepository");
jest.mock("../../repositories/transactionRepository");

describe("GetAllSaveGoal", () => {
    let mockSavingRepo: jest.Mocked<SavingRepository>;
    let mockTransactionRepo: jest.Mocked<TransactionRepository>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };

    let useCase: GetAllSaveGoal;

    beforeEach(() => {
        mockSavingRepo = new SavingRepository() as jest.Mocked<SavingRepository>;
        mockTransactionRepo = new TransactionRepository() as jest.Mocked<TransactionRepository>;
        mockPresenter = { success: jest.fn(), fail: jest.fn() };

        useCase = new GetAllSaveGoal(mockPresenter, mockTransactionRepo, mockSavingRepo);
    });

    it("should successfully get all save goals and map them to the correct response", async () => {
        // Sample mock data
        const mockAccount = new Account("account1", "Account 1");
        const mockSaveGoal = new SaveGoal(
            "goal1",
            "Save for new phone",
            mockAccount.getId(),
            new Money(1000),
            [
                {
                    id: "item1",
                    title: "Phone",
                    price: new Money(500),
                    link: "https://example.com/phone",
                    htmlToTrack: "phone-track"
                }
            ],
            "Save money for new phone"
        );

        // Mock repository methods
        mockSavingRepo.getAll.mockResolvedValueOnce([mockSaveGoal]);
        mockTransactionRepo.getAccountBalance.mockResolvedValueOnce(new Money(250));

        // Execute use case
        await useCase.execute();

        // Validate presenter success method is called with the correct response
        expect(mockPresenter.success).toHaveBeenCalledWith([
            {
                id: "goal1",
                title: "Save for new phone",
                description: "Save money for new phone",
                balance: 250,
                target: 1000,
                items: [
                    {
                        id: "item1",
                        title: "Phone",
                        price: 500,
                        link: "https://example.com/phone",
                        htmlToTrack: "phone-track"
                    }
                ]
            }
        ]);
    });

    it("should call fail when there is an error in fetching save goals", async () => {
        // Simulate an error in fetching save goals
        mockSavingRepo.getAll.mockRejectedValueOnce(new Error("Failed to fetch save goals"));

        // Execute use case
        await useCase.execute();

        // Validate presenter fail method is called
        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error("Failed to fetch save goals"));
    });

    it("should handle errors if getAccountBalance fails", async () => {
        const mockAccount = new Account("account1", "Account 1");
        const mockSaveGoal = new SaveGoal(
            "goal1",
            "Save for new phone",
            mockAccount.getId(),
            new Money(1000),
            [
                {
                    id: "item1",
                    title: "Phone",
                    price: new Money(500),
                    link: "https://example.com/phone",
                    htmlToTrack: "phone-track"
                }
            ],
            "Save money for new phone"
        );

        mockSavingRepo.getAll.mockResolvedValueOnce([mockSaveGoal]);
        mockTransactionRepo.getAccountBalance.mockRejectedValueOnce(new Error("Failed to fetch account balance"));

        await useCase.execute();

        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error("Failed to fetch account balance"));
    });

    it("should handle empty save goals list", async () => {
        // Simulate an empty list of save goals
        mockSavingRepo.getAll.mockResolvedValueOnce([]);

        // Execute use case
        await useCase.execute();

        // Validate presenter success method is called with an empty list
        expect(mockPresenter.success).toHaveBeenCalledWith([]);
    });
});

jest.mock("../../repositories/savingRepository");
jest.mock("../../repositories/transactionRepository");

describe("GetSaveGoalUseCase", () => {
    let mockSavingRepo: jest.Mocked<SavingRepository>;
    let mockTransactionRepo: jest.Mocked<TransactionRepository>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };

    let useCase: GetSaveGoalUseCase;

    beforeEach(() => {
        mockSavingRepo = new SavingRepository() as jest.Mocked<SavingRepository>;
        mockTransactionRepo = new TransactionRepository() as jest.Mocked<TransactionRepository>;
        mockPresenter = { success: jest.fn(), fail: jest.fn() };

        useCase = new GetSaveGoalUseCase(mockPresenter, mockTransactionRepo, mockSavingRepo);
    });

    it("should successfully get a save goal and map it to the correct response", async () => {
        // Sample mock data
        const mockAccount = new Account("account1", "Account 1");
        const mockSaveGoal = new SaveGoal(
            "goal1",
            "Save for new phone",
            mockAccount.getId(),
            new Money(1000),
            [
                {
                    id: "item1",
                    title: "Phone",
                    price: new Money(500),
                    link: "https://example.com/phone",
                    htmlToTrack: "phone-track"
                }
            ],
            "Save money for new phone"
        );

        // Mock repository methods
        mockSavingRepo.get.mockResolvedValueOnce(mockSaveGoal);
        mockTransactionRepo.getAccountBalance.mockResolvedValueOnce(new Money(250));

        // Execute use case
        await useCase.execute("goal1");

        // Validate presenter success method is called with the correct response
        expect(mockPresenter.success).toHaveBeenCalledWith({
            id: "goal1",
            title: "Save for new phone",
            description: "Save money for new phone",
            balance: 250,
            target: 1000,
            items: [
                {
                    id: "item1",
                    title: "Phone",
                    link: "https://example.com/phone",
                    price: 500,
                    htmlToTrack: "phone-track"
                }
            ]
        });
    });

    it("should call fail when there is an error in fetching the save goal", async () => {
        // Simulate an error in fetching save goal
        mockSavingRepo.get.mockRejectedValueOnce(new Error("Failed to fetch save goal"));

        // Execute use case
        await useCase.execute("goal1");

        // Validate presenter fail method is called
        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error("Failed to fetch save goal"));
    });

    it("should handle error when fetching account balance fails", async () => {
        const mockAccount = new Account("account1", "Account 1");
        const mockSaveGoal = new SaveGoal(
            "goal1",
            "Save for new phone",
            mockAccount.getId(),
            new Money(1000),
            [
                {
                    id: "item1",
                    title: "Phone",
                    price: new Money(500),
                    link: "https://example.com/phone",
                    htmlToTrack: "phone-track"
                }
            ],
            "Save money for new phone"
        );

        // Mock repository methods
        mockSavingRepo.get.mockResolvedValueOnce(mockSaveGoal);
        mockTransactionRepo.getAccountBalance.mockRejectedValueOnce(new Error("Failed to fetch account balance"));

        // Execute use case
        await useCase.execute("goal1");

        // Validate presenter fail method is called
        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error("Failed to fetch account balance"));
    });
});

jest.mock("../../repositories/savingRepository");
jest.mock("../../repositories/accountRepository");
jest.mock("../../repositories/transactionRepository");
jest.mock("../../repositories/recordRepository");
jest.mock("@/core/adapters/libs");
jest.mock("@/core/repositories/unitOfWorkRepository");

describe("IncreaseSaveGoalUseCase", () => {
    let mockSavingRepo: jest.Mocked<SavingRepository>;
    let mockAccountRepo: jest.Mocked<AccountRepository>;
    let mockTransactionRepo: jest.Mocked<TransactionRepository>;
    let mockRecordRepo: jest.Mocked<RecordRepository>;
    let mockDateService: jest.Mocked<DateService>;
    let mockUnitOfWork: jest.Mocked<UnitOfWorkRepository>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };

    let useCase: IncreaseSaveGoalUseCase;

    beforeEach(() => {
        mockSavingRepo = new SavingRepository() as jest.Mocked<SavingRepository>;
        mockAccountRepo = new AccountRepository() as jest.Mocked<AccountRepository>;
        mockTransactionRepo = new TransactionRepository() as jest.Mocked<TransactionRepository>;
        mockRecordRepo = new RecordRepository() as jest.Mocked<RecordRepository>;
        mockDateService = new DateService() as jest.Mocked<DateService>;
        mockUnitOfWork = new UnitOfWorkRepository() as jest.Mocked<UnitOfWorkRepository>;

        mockPresenter = { success: jest.fn(), fail: jest.fn() };

        useCase = new IncreaseSaveGoalUseCase(
            mockPresenter,
            mockAccountRepo,
            mockSavingRepo,
            mockTransactionRepo,
            mockDateService,
            mockRecordRepo,
            mockUnitOfWork
        );
    });

    it("should successfully increase the save goal", async () => {
        // Sample mock data
        const mockAccount = new Account("account1", "Account 1");
        const mockSavingGoal = new SavingGoal(
            "goal1",
            "Save for new phone",
            mockAccount.getId(),
            new Money(1000),
            [],
            "Save money for new phone"
        );
        
        // Mock repository methods
        mockSavingRepo.get.mockResolvedValueOnce(mockSavingGoal);
        mockAccountRepo.get.mockResolvedValueOnce(mockAccount);
        mockTransactionRepo.getAccountBalance.mockResolvedValueOnce(new Money(1500));
        mockDateService.getTodayWithTime.mockReturnValueOnce(new Date());

        // Mock the creation of records and transactions
        const recordId = "recordId";
        const transactionId = "transactionId";
        mockRecordRepo.save.mockResolvedValueOnce(undefined);
        mockTransactionRepo.save.mockResolvedValueOnce(undefined);

        // Sample request
        const request: RequestIncreaseSaveGoal = {
            savingGoalRef: "goal1",
            accountRef: "account1",
            increaseAmount: 500
        };

        // Execute use case
        await useCase.execute(request);

        // Validate success method is called
        expect(mockPresenter.success).toHaveBeenCalledWith(true);

        // Validate repository interactions
        expect(mockSavingRepo.get).toHaveBeenCalledWith("goal1");
        expect(mockAccountRepo.get).toHaveBeenCalledWith("account1");
        expect(mockTransactionRepo.getAccountBalance).toHaveBeenCalledWith("account1");
        expect(mockRecordRepo.save).toHaveBeenCalledTimes(2);
        expect(mockTransactionRepo.save).toHaveBeenCalledTimes(2);
        expect(mockUnitOfWork.commit).toHaveBeenCalled();
    });

    it("should fail when account balance is less than the increase amount", async () => {
        const mockAccount = new Account("account1", "Account 1");
        const mockSavingGoal = new SavingGoal(
            "goal1",
            "Save for new phone",
            mockAccount.getId(),
            new Money(1000),
            [],
            "Save money for new phone"
        );

        // Mock repository methods
        mockSavingRepo.get.mockResolvedValueOnce(mockSavingGoal);
        mockAccountRepo.get.mockResolvedValueOnce(mockAccount);
        mockTransactionRepo.getAccountBalance.mockResolvedValueOnce(new Money(400));

        // Sample request
        const request: RequestIncreaseSaveGoal = {
            savingGoalRef: "goal1",
            accountRef: "account1",
            increaseAmount: 500
        };

        // Execute use case
        await useCase.execute(request);

        // Validate fail method is called with ValidationError
        expect(mockPresenter.fail).toHaveBeenCalledWith(new ValidationError('Price must be smaller than account'));
    });

    it("should call fail when there is an error in executing the use case", async () => {
        // Simulate an error in fetching saving goal
        mockSavingRepo.get.mockRejectedValueOnce(new Error("Saving goal not found"));

        const request: RequestIncreaseSaveGoal = {
            savingGoalRef: "goal1",
            accountRef: "account1",
            increaseAmount: 500
        };

        // Execute use case
        await useCase.execute(request);

        // Validate fail method is called with the correct error
        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error("Saving goal not found"));
    });
});

jest.mock("../../repositories/savingRepository");
jest.mock("@/core/adapters/libs");

describe('UpdateSaveGoalUseCase', () => {
    let presenter: IUpdateSaveGoalPresenter;
    let savingRepo: SavingRepository;
    let useCase: UpdateSaveGoalUseCase;

    beforeEach(() => {
        presenter = {
            success: jest.fn(),
            fail: jest.fn(),
        };
        savingRepo = new SavingRepository();
        useCase = new UpdateSaveGoalUseCase(presenter, savingRepo);
    });

    it('should update save goal successfully', async () => {
        // Arrange
        const savingGoalRef = 'goal123';
        const request: RequestUpdateSaveGoalUseCase = {
            savingGoalRef,
            target: 500,
            title: 'New Goal Title',
            description: 'Updated description',
            items: [
                {
                    id: 'item123',
                    title: 'Item 1',
                    link: 'http://example.com/item1',
                    htmlToTrack: '<html></html>',
                    price: 100,
                },
                {
                    id: 'item456',
                    title: 'Item 2',
                    link: 'http://example.com/item2',
                    htmlToTrack: '<html></html>',
                    price: 200,
                }
            ]
        };

        const saveGoal = new SaveGoal(savingGoalRef);
        saveGoal.setTitle('Old Goal Title');
        saveGoal.setDescription('Old description');
        saveGoal.setItems([]);

        // Mock savingRepo methods
        savingRepo.get = jest.fn().mockResolvedValue(saveGoal);
        savingRepo.update = jest.fn().mockResolvedValue(true);

        // Act
        await useCase.execute(request);

        // Assert
        expect(savingRepo.get).toHaveBeenCalledWith(savingGoalRef);
        expect(saveGoal.getTitle()).toBe('New Goal Title');
        expect(saveGoal.getDescription()).toBe('Updated description');
        expect(saveGoal.getItems()).toHaveLength(2);
        expect(saveGoal.getItems()[0].price.getAmount()).toBe(100);
        expect(saveGoal.getItems()[1].price.getAmount()).toBe(200);
        expect(savingRepo.update).toHaveBeenCalledWith(saveGoal);
        expect(presenter.success).toHaveBeenCalledWith(true);
    });

    it('should fail when there is an error in updating save goal', async () => {
        // Arrange
        const savingGoalRef = 'goal123';
        const request: RequestUpdateSaveGoalUseCase = {
            savingGoalRef,
            target: 500,
            title: 'New Goal Title',
            description: 'Updated description',
            items: [
                {
                    id: 'item123',
                    title: 'Item 1',
                    link: 'http://example.com/item1',
                    htmlToTrack: '<html></html>',
                    price: 100,
                }
            ]
        };

        const saveGoal = new SaveGoal(savingGoalRef);
        saveGoal.setTitle('Old Goal Title');
        saveGoal.setDescription('Old description');
        saveGoal.setItems([]);

        // Mock savingRepo methods
        savingRepo.get = jest.fn().mockResolvedValue(saveGoal);
        savingRepo.update = jest.fn().mockRejectedValue(new Error('Database error'));

        // Act
        await useCase.execute(request);

        // Assert
        expect(savingRepo.get).toHaveBeenCalledWith(savingGoalRef);
        expect(presenter.fail).toHaveBeenCalledWith(new Error('Database error'));
    });
});