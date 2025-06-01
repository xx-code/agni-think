import { DateService, GetUID } from "@core/adapters/libs";
import { Money } from "@core/domains/entities/money";
import { Record, TransactionType } from "@core/domains/entities/record";
import { Transaction } from "@core/domains/entities/transaction";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import ValidationError from "@core/errors/validationError";
import { ValueError } from "@core/errors/valueError";
import { GetCategoryUseCase } from "@core/interactions/category/getCategoryUseCase";
import { IAutoDeleteFreezeBalancePresenter, AutoDeleteFreezeBalanceUseCase } from "@core/interactions/freezerBalance/autoDeleteFreezeBalanceUseCase";
import { AddTransactionUseCase, RequestAddTransactionUseCase } from "@core/interactions/transaction/addTransactionUseCase";
import { DeleteTransactionUseCase } from "@core/interactions/transaction/deleteTransactionUseCase";
import { GetBalanceByUseCase, RequestGetBalanceBy } from "@core/interactions/transaction/getBalanceByUseCase";
import { GetPaginationTransaction, RequestGetPagination } from "@core/interactions/transaction/getPaginationTransactionUseCase";
import { TransfertTransactionUseCase, RequestTransfertTransactionUseCase } from "@core/interactions/transaction/transfertTransactionUseCase";
import { UpdateTransactionUseCase, RequestUpdateTransactionUseCase } from "@core/interactions/transaction/updateTransactionUseCase";
import { AccountRepository } from "@core/repositories/accountRepository";
import { CategoryRepository } from "@core/repositories/categoryRepository";
import { RecordRepository } from "@core/repositories/recordRepository";
import { TagRepository } from "@core/repositories/tagRepository";
import { TransactionFilter, TransactionRepository } from "@core/repositories/transactionRepository";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";

// Mock dependencies
jest.mock("../../repositories/transactionRepository");
jest.mock("../../repositories/recordRepository");
jest.mock("../../repositories/categoryRepository");
jest.mock("../../repositories/tagRepository");
jest.mock("../../repositories/accountRepository");
jest.mock("@/core/repositories/unitOfWorkRepository");
jest.mock("@/core/adapters/libs");

describe("AddTransactionUseCase", () => {
    let mockTransactionRepo: jest.Mocked<TransactionRepository>;
    let mockRecordRepo: jest.Mocked<RecordRepository>;
    let mockCategoryRepo: jest.Mocked<CategoryRepository>;
    let mockTagRepo: jest.Mocked<TagRepository>;
    let mockAccountRepo: jest.Mocked<AccountRepository>;
    let mockUnitOfWorkRepo: jest.Mocked<UnitOfWorkRepository>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };
    let mockDateService: jest.Mocked<DateService>;

    let useCase: AddTransactionUseCase;

    beforeEach(() => {
        mockTransactionRepo = jest.mocked<TransactionRepository>({
            save: jest.fn(),
            get: jest.fn(),
            delete: jest.fn(),
            isTransactionExistById: jest.fn(),
            getPaginations: jest.fn(),
            getTransactions: jest.fn(),
            update: jest.fn() 
        });
         mockPresenter = { success: jest.fn(), fail: jest.fn() };
        mockDateService = { formatDateWithtime: jest.fn() } as any;

        useCase = new AddTransactionUseCase(
            {
                transactionRepository: mockTransactionRepo,
                recordRepository: mockRecordRepo,
                categoryRepository: mockCategoryRepo,
                tagRepository: mockTagRepo,
                accountRepository: mockAccountRepo,
                unitOfWork: mockUnitOfWorkRepo,
                dateService: mockDateService
            },
            mockPresenter
        );
    });

    it("should successfully add a transaction", async () => {
        const request: RequestAddTransactionUseCase = {
            accountRef: "account1",
            amount: 100,
            categoryRef: "category1",
            description: "Test transaction",
            date: "2025-03-10",
            tagRefs: ["tag1"],
            newTags: [],
            type: "expense"
        };

        mockAccountRepo.isExistById.mockResolvedValue(true);
        mockCategoryRepo.isCategoryExistById.mockResolvedValue(true);
        mockTagRepo.isTagExistByIds.mockResolvedValue(true);
        mockUnitOfWorkRepo.start.mockResolvedValue(undefined);
        mockUnitOfWorkRepo.commit.mockResolvedValue(undefined);
        mockDateService.formatDateWithtime.mockReturnValue("2025-03-10T00:00:00");

        await useCase.execute(request);

        expect(mockTransactionRepo.save).toHaveBeenCalled();
        expect(mockRecordRepo.save).toHaveBeenCalled();
        expect(mockUnitOfWorkRepo.commit).toHaveBeenCalled();
        expect(mockPresenter.success).toHaveBeenCalledWith(true);
    });

    it("should fail if account is not found", async () => {
        const request: RequestAddTransactionUseCase = {
            accountRef: "non-existing-account",
            amount: 100,
            categoryRef: "category1",
            description: "Test transaction",
            date: "2025-03-10",
            tagRefs: ["tag1"],
            newTags: [],
            type: "expense"
        };

        mockAccountRepo.isExistById.mockResolvedValue(false);

        await useCase.execute(request);

        expect(mockUnitOfWorkRepo.rollback).toHaveBeenCalled();
        expect(mockPresenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("Account not found"));
    });

    it("should fail if category is not found", async () => {
        const request: RequestAddTransactionUseCase = {
            accountRef: "account1",
            amount: 100,
            categoryRef: "non-existing-category",
            description: "Test transaction",
            date: "2025-03-10",
            tagRefs: ["tag1"],
            newTags: [],
            type: "expense"
        };

        mockAccountRepo.isExistById.mockResolvedValue(true);
        mockCategoryRepo.isCategoryExistById.mockResolvedValue(false);

        await useCase.execute(request);

        expect(mockUnitOfWorkRepo.rollback).toHaveBeenCalled();
        expect(mockPresenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("Category not found"));
    });

    it("should fail if tags are not found", async () => {
        const request: RequestAddTransactionUseCase = {
            accountRef: "account1",
            amount: 100,
            categoryRef: "category1",
            description: "Test transaction",
            date: "2025-03-10",
            tagRefs: ["non-existing-tag"],
            newTags: [],
            type: "expense"
        };

        mockAccountRepo.isExistById.mockResolvedValue(true);
        mockCategoryRepo.isCategoryExistById.mockResolvedValue(true);
        mockTagRepo.isTagExistByIds.mockResolvedValue(false);

        await useCase.execute(request);

        expect(mockUnitOfWorkRepo.rollback).toHaveBeenCalled();
        expect(mockPresenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("A tag are not found"));
    });

    it("should fail if an error occurs during transaction saving", async () => {
        const request: RequestAddTransactionUseCase = {
            accountRef: "account1",
            amount: 100,
            categoryRef: "category1",
            description: "Test transaction",
            date: "2025-03-10",
            tagRefs: ["tag1"],
            newTags: [],
            type: "expense"
        };

        mockAccountRepo.isExistById.mockResolvedValue(true);
        mockCategoryRepo.isCategoryExistById.mockResolvedValue(true);
        mockTagRepo.isTagExistByIds.mockResolvedValue(true);
        mockUnitOfWorkRepo.start.mockResolvedValue(undefined);
        mockTransactionRepo.save.mockRejectedValue(new Error("Transaction save error"));

        await useCase.execute(request);

        expect(mockUnitOfWorkRepo.rollback).toHaveBeenCalled();
        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error("Transaction save error"));
    });
});

// Mock dependencies
jest.mock("../../repositories/transactionRepository");
jest.mock("../../repositories/recordRepository");
jest.mock("@/core/repositories/unitOfWorkRepository");

describe("DeleteTransactionUseCase", () => {
    let mockTransactionRepo: jest.Mocked<TransactionRepository>;
    let mockRecordRepo: jest.Mocked<RecordRepository>;
    let mockUnitOfWorkRepo: jest.Mocked<UnitOfWorkRepository>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };

    let useCase: DeleteTransactionUseCase;

    beforeEach(() => {
        mockTransactionRepo = new TransactionRepository() as jest.Mocked<TransactionRepository>;
        mockRecordRepo = new RecordRepository() as jest.Mocked<RecordRepository>;
        mockUnitOfWorkRepo = new UnitOfWorkRepository() as jest.Mocked<UnitOfWorkRepository>;
        mockPresenter = { success: jest.fn(), fail: jest.fn() };

        useCase = new DeleteTransactionUseCase(
            mockTransactionRepo,
            mockRecordRepo,
            mockUnitOfWorkRepo,
            mockPresenter
        );
    });

    it("should successfully delete a transaction", async () => {
        const transactionId = "transaction1";
        const recordRef = "record1";

        const mockTransaction = new Transaction(transactionId, "account1", recordRef, "category1", ["tag1"]);
        mockTransactionRepo.get.mockResolvedValue(mockTransaction);
        mockTransactionRepo.isTransactionExistById.mockResolvedValue(true);
        mockRecordRepo.delete.mockResolvedValue(undefined);
        mockTransactionRepo.delete.mockResolvedValue(undefined);
        mockUnitOfWorkRepo.start.mockResolvedValue(undefined);
        mockUnitOfWorkRepo.commit.mockResolvedValue(undefined);

        await useCase.execute(transactionId);

        expect(mockUnitOfWorkRepo.start).toHaveBeenCalled();
        expect(mockRecordRepo.delete).toHaveBeenCalledWith(recordRef);
        expect(mockTransactionRepo.delete).toHaveBeenCalledWith(transactionId);
        expect(mockUnitOfWorkRepo.commit).toHaveBeenCalled();
        expect(mockPresenter.success).toHaveBeenCalledWith(true);
    });

    it("should fail if the transaction does not exist", async () => {
        const transactionId = "non-existing-transaction";

        mockTransactionRepo.isTransactionExistById.mockResolvedValue(false);

        await useCase.execute(transactionId);

        expect(mockUnitOfWorkRepo.rollback).toHaveBeenCalled();
        expect(mockPresenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("Transaction not found"));
    });

    it("should fail if there is an error during transaction deletion", async () => {
        const transactionId = "transaction1";
        const recordRef = "record1";

        const mockTransaction = new Transaction(transactionId, "account1", recordRef, "category1", ["tag1"]);
        mockTransactionRepo.get.mockResolvedValue(mockTransaction);
        mockTransactionRepo.isTransactionExistById.mockResolvedValue(true);
        mockRecordRepo.delete.mockRejectedValue(new Error("Error deleting record"));

        mockUnitOfWorkRepo.start.mockResolvedValue(undefined);
        mockUnitOfWorkRepo.rollback.mockResolvedValue(undefined);

        await useCase.execute(transactionId);

        expect(mockUnitOfWorkRepo.rollback).toHaveBeenCalled();
        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error("Error deleting record"));
    });

    it("should fail if there is an error during unit of work commit", async () => {
        const transactionId = "transaction1";
        const recordRef = "record1";

        const mockTransaction = new Transaction(transactionId, "account1", recordRef, "category1", ["tag1"]);
        mockTransactionRepo.get.mockResolvedValue(mockTransaction);
        mockTransactionRepo.isTransactionExistById.mockResolvedValue(true);
        mockRecordRepo.delete.mockResolvedValue(undefined);
        mockTransactionRepo.delete.mockResolvedValue(undefined);
        mockUnitOfWorkRepo.start.mockResolvedValue(undefined);
        mockUnitOfWorkRepo.commit.mockRejectedValue(new Error("Error during commit"));

        await useCase.execute(transactionId);

        expect(mockUnitOfWorkRepo.rollback).toHaveBeenCalled();
        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error("Error during commit"));
    });
});

// Mock dependencies
jest.mock("../../repositories/transactionRepository");

describe("GetBalanceByUseCase", () => {
    let mockTransactionRepo: jest.Mocked<TransactionRepository>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };

    let useCase: GetBalanceByUseCase;

    beforeEach(() => {
        mockTransactionRepo = new TransactionRepository() as jest.Mocked<TransactionRepository>;
        mockPresenter = { success: jest.fn(), fail: jest.fn() };

        useCase = new GetBalanceByUseCase(mockTransactionRepo, mockPresenter);
    });

    it("should successfully retrieve balance with valid date range", async () => {
        const request: RequestGetBalanceBy = {
            accountsIds: ["account1"],
            tagsIds: ["tag1"],
            categoriesIds: ["category1"],
            dateStart: "2023-01-01",
            dateEnd: "2023-01-31",
            type: "income",
            minPrice: 100,
            maxPrice: 1000,
        };

        const mockBalance = new Money(500);
        mockTransactionRepo.getBalance.mockResolvedValue(mockBalance);

        await useCase.execute(request);

        expect(mockTransactionRepo.getBalance).toHaveBeenCalledWith({
            accounts: ["account1"],
            categories: ["category1"],
            tags: ["tag1"],
            startDate: "2023-01-01",
            endDate: "2023-01-31",
            type: "income",
            minPrice: new Money(100),
            maxPrice: new Money(1000),
        });
        expect(mockPresenter.success).toHaveBeenCalledWith(500);
    });

    it("should throw ValidationError when dateStart is greater than dateEnd", async () => {
        const request: RequestGetBalanceBy = {
            accountsIds: ["account1"],
            tagsIds: ["tag1"],
            categoriesIds: ["category1"],
            dateStart: "2023-02-01",
            dateEnd: "2023-01-01", // Invalid date range
            type: "income",
            minPrice: 100,
            maxPrice: 1000,
        };

        await useCase.execute(request);

        expect(mockPresenter.fail).toHaveBeenCalledWith(new ValidationError("Date start must be less than date end"));
    });

    it("should successfully retrieve balance with no date range", async () => {
        const request: RequestGetBalanceBy = {
            accountsIds: ["account1"],
            tagsIds: ["tag1"],
            categoriesIds: ["category1"],
            dateStart: "",
            dateEnd: "",
            type: "expense",
            minPrice: 200,
            maxPrice: 2000,
        };

        const mockBalance = new Money(1000);
        mockTransactionRepo.getBalance.mockResolvedValue(mockBalance);

        await useCase.execute(request);

        expect(mockTransactionRepo.getBalance).toHaveBeenCalledWith({
            accounts: ["account1"],
            categories: ["category1"],
            tags: ["tag1"],
            startDate: "",
            endDate: "",
            type: "expense",
            minPrice: new Money(200),
            maxPrice: new Money(2000),
        });
        expect(mockPresenter.success).toHaveBeenCalledWith(1000);
    });

    it("should handle filter parameters correctly (minPrice, maxPrice)", async () => {
        const request: RequestGetBalanceBy = {
            accountsIds: ["account1"],
            tagsIds: ["tag1"],
            categoriesIds: ["category1"],
            dateStart: "2023-01-01",
            dateEnd: "2023-01-31",
            type: "income",
            minPrice: 50,
            maxPrice: 500,
        };

        const mockBalance = new Money(300);
        mockTransactionRepo.getBalance.mockResolvedValue(mockBalance);

        await useCase.execute(request);

        expect(mockTransactionRepo.getBalance).toHaveBeenCalledWith({
            accounts: ["account1"],
            categories: ["category1"],
            tags: ["tag1"],
            startDate: "2023-01-01",
            endDate: "2023-01-31",
            type: "income",
            minPrice: new Money(50),
            maxPrice: new Money(500),
        });
        expect(mockPresenter.success).toHaveBeenCalledWith(300);
    });

    it("should fail if there's an error in retrieving balance", async () => {
        const request: RequestGetBalanceBy = {
            accountsIds: ["account1"],
            tagsIds: ["tag1"],
            categoriesIds: ["category1"],
            dateStart: "2023-01-01",
            dateEnd: "2023-01-31",
            type: "income",
            minPrice: 100,
            maxPrice: 1000,
        };

        const mockError = new Error("Database error");
        mockTransactionRepo.getBalance.mockRejectedValue(mockError);

        await useCase.execute(request);

        expect(mockPresenter.fail).toHaveBeenCalledWith(mockError);
    });
});

escribe("GetPaginationTransaction", () => {
    let mockTransactionRepo: jest.Mocked<TransactionRepository>;
    let mockAccountRepo: jest.Mocked<AccountRepository>;
    let mockCategoryRepo: jest.Mocked<CategoryRepository>;
    let mockTagRepo: jest.Mocked<TagRepository>;
    let mockRecordRepo: jest.Mocked<RecordRepository>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };

    let useCase: GetPaginationTransaction;

    beforeEach(() => {
        mockTransactionRepo = new TransactionRepository() as jest.Mocked<TransactionRepository>;
        mockAccountRepo = new AccountRepository() as jest.Mocked<AccountRepository>;
        mockCategoryRepo = new CategoryRepository() as jest.Mocked<CategoryRepository>;
        mockTagRepo = new TagRepository() as jest.Mocked<TagRepository>;
        mockRecordRepo = new RecordRepository() as jest.Mocked<RecordRepository>;

        mockPresenter = { success: jest.fn(), fail: jest.fn() };

        const adapter = {
            transactionRepository: mockTransactionRepo,
            accountRepository: mockAccountRepo,
            categoryRepository: mockCategoryRepo,
            tagRepository: mockTagRepo,
            recordRepository: mockRecordRepo,
        };

        useCase = new GetPaginationTransaction(adapter, mockPresenter);
    });

    it("should return correct pagination response with valid request", async () => {
        const request: RequestGetPagination = {
            page: 1,
            size: 10,
            sortBy: "date",
            sortSense: "desc",
            accountFilter: ["account1"],
            categoryFilter: ["category1"],
            tagFilter: ["tag1"],
            dateStart: "2023-01-01",
            dateEnd: "2023-01-31",
            type: "income",
            minPrice: 100,
            maxPrice: 1000,
        };

        const mockTransactions = [
            { 
                getId: () => "transaction1", 
                getAccountRef: () => "account1", 
                getCategoryRef: () => "category1", 
                getTags: () => ["tag1"],
                getRecordRef: () => "record1",
            },
        ];

        const mockCategory = { getId: () => "category1", getTitle: () => "Food", getColor: () => "red", getIconId: () => "icon1" };
        const mockTag = { getId: () => "tag1", getValue: () => "Groceries", getColor: () => "green" };
        const mockRecord = { getAmount: () => new Money(500), getDate: () => "2023-01-01", getDescription: () => "Grocery shopping", getType: () => "expense" };

        mockTransactionRepo.getPaginations.mockResolvedValue({ transactions: mockTransactions, maxPage: 1 });
        mockCategoryRepo.get.mockResolvedValue(mockCategory);
        mockTagRepo.get.mockResolvedValue(mockTag);
        mockRecordRepo.get.mockResolvedValue(mockRecord);

        await useCase.execute(request);

        expect(mockPresenter.success).toHaveBeenCalledWith({
            transactions: [
                {
                    accountId: "account1",
                    transactionId: "transaction1",
                    amount: 500,
                    date: "2023-01-01",
                    description: "Grocery shopping",
                    type: "expense",
                    category: { id: "category1", title: "Food", icon: "icon1", color: "red" },
                    tags: [{ id: "tag1", value: "Groceries", color: "green" }],
                },
            ],
            maxPages: 1,
        });
    });

    it("should fail when page is less than or equal to 0", async () => {
        const request: RequestGetPagination = {
            page: 0,
            size: 10,
            sortBy: "date",
            sortSense: "desc",
            accountFilter: ["account1"],
            categoryFilter: ["category1"],
            tagFilter: ["tag1"],
            dateStart: "2023-01-01",
            dateEnd: "2023-01-31",
            type: "income",
            minPrice: 100,
            maxPrice: 1000,
        };

        await useCase.execute(request);

        expect(mockPresenter.fail).toHaveBeenCalledWith(new ValidationError("Page request must be greather than 0"));
    });

    it("should fail when size is less than or equal to 0", async () => {
        const request: RequestGetPagination = {
            page: 1,
            size: 0,
            sortBy: "date",
            sortSense: "desc",
            accountFilter: ["account1"],
            categoryFilter: ["category1"],
            tagFilter: ["tag1"],
            dateStart: "2023-01-01",
            dateEnd: "2023-01-31",
            type: "income",
            minPrice: 100,
            maxPrice: 1000,
        };

        await useCase.execute(request);

        expect(mockPresenter.fail).toHaveBeenCalledWith(new ValidationError("Size must be greather than 0"));
    });

    it("should fail when account filter contains invalid accounts", async () => {
        const request: RequestGetPagination = {
            page: 1,
            size: 10,
            sortBy: "date",
            sortSense: "desc",
            accountFilter: ["invalid_account"],
            categoryFilter: [],
            tagFilter: [],
            dateStart: "2023-01-01",
            dateEnd: "2023-01-31",
            type: "income",
            minPrice: 100,
            maxPrice: 1000,
        };

        mockAccountRepo.isExistByIds.mockResolvedValue(false);

        await useCase.execute(request);

        expect(mockPresenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("an account to filter not valid"));
    });

    it("should fail when category filter contains invalid categories", async () => {
        const request: RequestGetPagination = {
            page: 1,
            size: 10,
            sortBy: "date",
            sortSense: "desc",
            accountFilter: [],
            categoryFilter: ["invalid_category"],
            tagFilter: [],
            dateStart: "2023-01-01",
            dateEnd: "2023-01-31",
            type: "income",
            minPrice: 100,
            maxPrice: 1000,
        };

        mockCategoryRepo.isCategoryExistByIds.mockResolvedValue(false);

        await useCase.execute(request);

        expect(mockPresenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("an category to filter not valid"));
    });

    it("should fail when dateStart is later than dateEnd", async () => {
        const request: RequestGetPagination = {
            page: 1,
            size: 10,
            sortBy: "date",
            sortSense: "desc",
            accountFilter: [],
            categoryFilter: [],
            tagFilter: [],
            dateStart: "2023-02-01",
            dateEnd: "2023-01-01", // Invalid date range
            type: "income",
            minPrice: 100,
            maxPrice: 1000,
        };

        await useCase.execute(request);

        expect(mockPresenter.fail).toHaveBeenCalledWith(new ValidationError("Date start must be less than date end"));
    });

    it("should fail when repository throws an error", async () => {
        const request: RequestGetPagination = {
            page: 1,
            size: 10,
            sortBy: "date",
            sortSense: "desc",
            accountFilter: ["account1"],
            categoryFilter: ["category1"],
            tagFilter: ["tag1"],
            dateStart: "2023-01-01",
            dateEnd: "2023-01-31",
            type: "income",
            minPrice: 100,
            maxPrice: 1000,
        };

        const mockError = new Error("Database error");
        mockTransactionRepo.getPaginations.mockRejectedValue(mockError);

        await useCase.execute(request);

        expect(mockPresenter.fail).toHaveBeenCalledWith(mockError);
    });
});

jest.mock("../../repositories/categoryRepository");

describe("GetCategoryUseCase", () => {
    let mockCategoryRepo: jest.Mocked<CategoryRepository>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };
    let useCase: GetCategoryUseCase;

    beforeEach(() => {
        mockCategoryRepo = new CategoryRepository() as jest.Mocked<CategoryRepository>;
        mockPresenter = { success: jest.fn(), fail: jest.fn() };

        useCase = new GetCategoryUseCase(mockCategoryRepo, mockPresenter);
    });

    it("should return category data when a valid ID is provided", async () => {
        const categoryId = "category1";
        const mockCategory = {
            getId: () => categoryId,
            getIconId: () => "icon1",
            getTitle: () => "Food",
            getColor: () => "red",
        };

        mockCategoryRepo.get.mockResolvedValue(mockCategory);

        await useCase.execute(categoryId);

        expect(mockPresenter.success).toHaveBeenCalledWith({
            category_id: categoryId,
            icon: "icon1",
            title: "Food",
            color: "red",
        });
    });

    it("should return error when the category is not found", async () => {
        const categoryId = "invalidCategoryId";

        mockCategoryRepo.get.mockRejectedValue(new ResourceNotFoundError("Category not found"));

        await useCase.execute(categoryId);

        expect(mockPresenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("Category not found"));
    });

    it("should return error when there is a repository issue", async () => {
        const categoryId = "category1";
        const mockError = new Error("Database error");

        mockCategoryRepo.get.mockRejectedValue(mockError);

        await useCase.execute(categoryId);

        expect(mockPresenter.fail).toHaveBeenCalledWith(mockError);
    });
});

// Mocking repositories and dependencies
jest.mock("../../repositories/accountRepository");
jest.mock("../../repositories/transactionRepository");
jest.mock("../../repositories/recordRepository");
jest.mock("@/core/repositories/unitOfWorkRepository");

describe("TransfertTransactionUseCase", () => {
    let mockAccountRepo: jest.Mocked<AccountRepository>;
    let mockTransactionRepo: jest.Mocked<TransactionRepository>;
    let mockRecordRepo: jest.Mocked<RecordRepository>;
    let mockUnitOfWork: jest.Mocked<UnitOfWorkRepository>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };
    let useCase: TransfertTransactionUseCase;

    beforeEach(() => {
        mockAccountRepo = new AccountRepository() as jest.Mocked<AccountRepository>;
        mockTransactionRepo = new TransactionRepository() as jest.Mocked<TransactionRepository>;
        mockRecordRepo = new RecordRepository() as jest.Mocked<RecordRepository>;
        mockUnitOfWork = new UnitOfWorkRepository() as jest.Mocked<UnitOfWorkRepository>;
        mockPresenter = { success: jest.fn(), fail: jest.fn() };

        useCase = new TransfertTransactionUseCase(
            {
                transactionRepository: mockTransactionRepo,
                recordRepository: mockRecordRepo,
                accountRepository: mockAccountRepo,
                unitOfWork: mockUnitOfWork,
            },
            mockPresenter
        );
    });

    it("should successfully transfer money between accounts", async () => {
        const request: RequestTransfertTransactionUseCase = {
            accountRefFrom: "account1",
            accountRefTo: "account2",
            date: "2025-03-10",
            amount: 100,
        };

        const mockAccountFrom = {
            getId: () => "account1",
            getTitle: () => "Account 1",
        };
        const mockAccountTo = {
            getId: () => "account2",
            getTitle: () => "Account 2",
        };

        mockAccountRepo.get.mockResolvedValueOnce(mockAccountFrom);
        mockAccountRepo.get.mockResolvedValueOnce(mockAccountTo);

        const mockBalance = new Money(500);
        mockTransactionRepo.getBalance.mockResolvedValueOnce(mockBalance);

        // Mock the saving of records and transactions
        const mockRecordFrom = new Record("uid1", new Money(100), "2025-03-10", TransactionType.DEBIT);
        const mockRecordTo = new Record("uid2", new Money(100), "2025-03-10", TransactionType.CREDIT);

        mockRecordRepo.save.mockResolvedValueOnce(mockRecordFrom);
        mockRecordRepo.save.mockResolvedValueOnce(mockRecordTo);

        const mockTransactionFrom = new Transaction("uid3", "account1", "uid1", "categoryId");
        const mockTransactionTo = new Transaction("uid4", "account2", "uid2", "categoryId");

        mockTransactionRepo.save.mockResolvedValueOnce(mockTransactionFrom);
        mockTransactionRepo.save.mockResolvedValueOnce(mockTransactionTo);

        await useCase.execute(request);

        expect(mockPresenter.success).toHaveBeenCalledWith(true);
        expect(mockRecordRepo.save).toHaveBeenCalledTimes(2); // Saving 2 records
        expect(mockTransactionRepo.save).toHaveBeenCalledTimes(2); // Saving 2 transactions
    });

    it("should fail when there are insufficient funds", async () => {
        const request: RequestTransfertTransactionUseCase = {
            accountRefFrom: "account1",
            accountRefTo: "account2",
            date: "2025-03-10",
            amount: 600, // Exceeds available balance
        };

        const mockAccountFrom = {
            getId: () => "account1",
            getTitle: () => "Account 1",
        };
        const mockAccountTo = {
            getId: () => "account2",
            getTitle: () => "Account 2",
        };

        mockAccountRepo.get.mockResolvedValueOnce(mockAccountFrom);
        mockAccountRepo.get.mockResolvedValueOnce(mockAccountTo);

        const mockBalance = new Money(500); // Not enough balance
        mockTransactionRepo.getBalance.mockResolvedValueOnce(mockBalance);

        await useCase.execute(request);

        expect(mockPresenter.fail).toHaveBeenCalledWith(new ValueError('Price must be less than balance from 0'));
    });

    it("should fail when one of the accounts does not exist", async () => {
        const request: RequestTransfertTransactionUseCase = {
            accountRefFrom: "nonexistentAccount",
            accountRefTo: "account2",
            date: "2025-03-10",
            amount: 100,
        };

        mockAccountRepo.get.mockRejectedValueOnce(new ResourceNotFoundError("Account not found"));

        await useCase.execute(request);

        expect(mockPresenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("Account not found"));
    });

    it("should fail when there is an error in the transaction repository", async () => {
        const request: RequestTransfertTransactionUseCase = {
            accountRefFrom: "account1",
            accountRefTo: "account2",
            date: "2025-03-10",
            amount: 100,
        };

        const mockAccountFrom = {
            getId: () => "account1",
            getTitle: () => "Account 1",
        };
        const mockAccountTo = {
            getId: () => "account2",
            getTitle: () => "Account 2",
        };

        mockAccountRepo.get.mockResolvedValueOnce(mockAccountFrom);
        mockAccountRepo.get.mockResolvedValueOnce(mockAccountTo);

        const mockBalance = new Money(500);
        mockTransactionRepo.getBalance.mockResolvedValueOnce(mockBalance);

        // Simulating an error while saving a record
        mockRecordRepo.save.mockRejectedValueOnce(new Error("Error saving record"));

        await useCase.execute(request);

        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error("Error saving record"));
    });
});

jest.mock("../../repositories/accountRepository");
jest.mock("../../repositories/tagRepository");
jest.mock("../../repositories/categoryRepository");
jest.mock("../../repositories/transactionRepository");
jest.mock("../../repositories/recordRepository");
jest.mock("@/core/repositories/unitOfWorkRepository");
jest.mock("@/core/adapters/libs");

describe("UpdateTransactionUseCase", () => {
    let mockAccountRepo: jest.Mocked<AccountRepository>;
    let mockTagRepo: jest.Mocked<TagRepository>;
    let mockCategoryRepo: jest.Mocked<CategoryRepository>;
    let mockTransactionRepo: jest.Mocked<TransactionRepository>;
    let mockRecordRepo: jest.Mocked<RecordRepository>;
    let mockUnitOfWork: jest.Mocked<UnitOfWorkRepository>;
    let mockDateService: jest.Mocked<DateService>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };
    let useCase: UpdateTransactionUseCase;

    beforeEach(() => {
        mockAccountRepo = new AccountRepository() as jest.Mocked<AccountRepository>;
        mockTagRepo = new TagRepository() as jest.Mocked<TagRepository>;
        mockCategoryRepo = new CategoryRepository() as jest.Mocked<CategoryRepository>;
        mockTransactionRepo = new TransactionRepository() as jest.Mocked<TransactionRepository>;
        mockRecordRepo = new RecordRepository() as jest.Mocked<RecordRepository>;
        mockUnitOfWork = new UnitOfWorkRepository() as jest.Mocked<UnitOfWorkRepository>;
        mockDateService = new DateService() as jest.Mocked<DateService>;
        mockPresenter = { success: jest.fn(), fail: jest.fn() };

        useCase = new UpdateTransactionUseCase(
            {
                transactionRepository: mockTransactionRepo,
                categoryRepository: mockCategoryRepo,
                tagRepository: mockTagRepo,
                recordRepository: mockRecordRepo,
                accountRepository: mockAccountRepo,
                dateService: mockDateService,
                unitOfWork: mockUnitOfWork,
            },
            mockPresenter
        );
    });

    it("should successfully update the transaction and record", async () => {
        const request: RequestUpdateTransactionUseCase = {
            id: "transaction1",
            accountRef: "account1",
            tagsRef: ["tag1"],
            newTagsRef: ["tag2"],
            categoryRef: "category1",
            type: "DEBIT",
            description: "Updated description",
            date: "2025-03-10",
            amount: 150,
        };

        const mockTransaction = {
            getRecordRef: () => "record1",
            setAccountRef: jest.fn(),
            setCategoryRef: jest.fn(),
            setTags: jest.fn(),
            hasChange: jest.fn().mockReturnValue(true),
            getId: () => "transaction1",
        };

        const mockRecord = {
            setAmount: jest.fn(),
            setDescription: jest.fn(),
            setType: jest.fn(),
            setDate: jest.fn(),
            hasChange: jest.fn().mockReturnValue(true),
            getId: () => "record1",
        };

        mockTransactionRepo.get.mockResolvedValueOnce(mockTransaction);
        mockRecordRepo.get.mockResolvedValueOnce(mockRecord);
        mockAccountRepo.isExistById.mockResolvedValueOnce(true);
        mockCategoryRepo.isCategoryExistById.mockResolvedValueOnce(true);
        mockTagRepo.isTagExistByIds.mockResolvedValueOnce(true);
        mockDateService.formatDateWithtime.mockReturnValue("2025-03-10");

        await useCase.execute(request);

        expect(mockRecordRepo.update).toHaveBeenCalledWith(mockRecord);
        expect(mockTransactionRepo.update).toHaveBeenCalledWith(mockTransaction);
        expect(mockPresenter.success).toHaveBeenCalledWith(true);
    });

    it("should fail if the account does not exist", async () => {
        const request: RequestUpdateTransactionUseCase = {
            id: "transaction1",
            accountRef: "invalidAccount",
            tagsRef: ["tag1"],
            newTagsRef: ["tag2"],
            categoryRef: "category1",
            type: "DEBIT",
            description: "Updated description",
            date: "2025-03-10",
            amount: 150,
        };

        mockAccountRepo.isExistById.mockResolvedValueOnce(false); // Simulate non-existent account

        await useCase.execute(request);

        expect(mockPresenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("Account not found"));
    });

    it("should fail if the category does not exist", async () => {
        const request: RequestUpdateTransactionUseCase = {
            id: "transaction1",
            accountRef: "account1",
            tagsRef: ["tag1"],
            newTagsRef: ["tag2"],
            categoryRef: "invalidCategory",
            type: "DEBIT",
            description: "Updated description",
            date: "2025-03-10",
            amount: 150,
        };

        mockCategoryRepo.isCategoryExistById.mockResolvedValueOnce(false); // Simulate non-existent category

        await useCase.execute(request);

        expect(mockPresenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("Category not found"));
    });

    it("should fail if any tag does not exist", async () => {
        const request: RequestUpdateTransactionUseCase = {
            id: "transaction1",
            accountRef: "account1",
            tagsRef: ["tag1"],
            newTagsRef: ["tag2"],
            categoryRef: "category1",
            type: "DEBIT",
            description: "Updated description",
            date: "2025-03-10",
            amount: 150,
        };

        mockTagRepo.isTagExistByIds.mockResolvedValueOnce(false); // Simulate non-existent tag

        await useCase.execute(request);

        expect(mockPresenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("a tag not found"));
    });

    it("should fail when an error occurs during the update", async () => {
        const request: RequestUpdateTransactionUseCase = {
            id: "transaction1",
            accountRef: "account1",
            tagsRef: ["tag1"],
            newTagsRef: ["tag2"],
            categoryRef: "category1",
            type: "DEBIT",
            description: "Updated description",
            date: "2025-03-10",
            amount: 150,
        };

        mockTransactionRepo.get.mockRejectedValueOnce(new Error("Error in transaction repository"));

        await useCase.execute(request);

        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error("Error in transaction repository"));
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