import { DateService } from "@core/adapters/libs";
import { Budget } from "@core/domains/entities/budget";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { AutoUpdateBudgetUseCase } from "@core/interactions/budgets/autoUpdateBudgetUseCase";
import { ICreationBudgetUseCaseResponse, ICreationBudgetAdapter, CreationBudgetUseCase } from "@core/interactions/budgets/creationBudgetUseCase";
import { IDeleteBudgetUseCaseResponse, DeleteBudgetUseCase } from "@core/interactions/budgets/deleteBudgetUseCase";
import { IGetAllBudgetUseCaseResponse, GetAllBudgetUseCase, BudgetOutput } from "@core/interactions/budgets/getAllBudgetUseCase";
import { IGetBudgetUseCaseResponse, GetBudgetUseCase } from "@core/interactions/budgets/getBudgetUseCase";
import { IUpdateBudgetUseCasePresenter, RequestUpdateBudget, UpdateBudgetUseCase } from "@core/interactions/budgets/updateBudgetUseCase";
import { BudgetRepository } from "@core/repositories/budgetRepository";
import { CategoryRepository } from "@core/repositories/categoryRepository";
import { TagRepository } from "@core/repositories/tagRepository";
import { TransactionRepository } from "@core/repositories/transactionRepository";


describe("CreationBudgetUseCase", () => {
    let budgetRepo: BudgetRepository;
    let categoryRepo: CategoryRepository;
    let tagRepo: TagRepository;
    let dateService: DateService;
    let presenter: ICreationBudgetUseCaseResponse;
    let adapters: ICreationBudgetAdapter;

    beforeEach(() => {
        budgetRepo = {
            isBudgetExistByName: jest.fn(),
            save: jest.fn(),
        };

        categoryRepo = {
            isCategoryExistByIds: jest.fn(),
        };

        tagRepo = {
            isTagExistByIds: jest.fn(),
        };

        dateService = {
            formatDate: jest.fn().mockImplementation(date => date),
            getDateAddition: jest.fn(),
        };

        presenter = {
            success: jest.fn(),
            fail: jest.fn(),
        };

        adapters = {
            budgetRepository: budgetRepo,
            categoryRepository: categoryRepo,
            tagRepository: tagRepo,
            dateService: dateService,
        };
    });

    test("should create a budget successfully", async () => {
        (budgetRepo.isBudgetExistByName as jest.Mock).mockResolvedValue(false);
        (categoryRepo.isCategoryExistByIds as jest.Mock).mockResolvedValue(true);
        (tagRepo.isTagExistByIds as jest.Mock).mockResolvedValue(true);
        (budgetRepo.save as jest.Mock).mockResolvedValue(undefined);

        const useCase = new CreationBudgetUseCase(adapters, dateService, presenter);

        await useCase.execute({
            title: "My Budget",
            target: 1000,
            period: "monthly",
            periodTime: 1,
            dateStart: "2025-03-01",
            dateEnd: "2025-03-31",
            categoriesRef: ["cat1"],
            tagRefs: ["tag1"],
            newTagRefs: [],
        });

        expect(budgetRepo.isBudgetExistByName).toHaveBeenCalledWith("My Budget");
        expect(categoryRepo.isCategoryExistByIds).toHaveBeenCalledWith(["cat1"]);
        expect(tagRepo.isTagExistByIds).toHaveBeenCalledWith(["tag1"]);
        expect(budgetRepo.save).toHaveBeenCalled();
        expect(presenter.success).toHaveBeenCalledWith(true);
    });

    test("should fail if budget title already exists", async () => {
        (budgetRepo.isBudgetExistByName as jest.Mock).mockResolvedValue(true);

        const useCase = new CreationBudgetUseCase(adapters, dateService, presenter);

        await useCase.execute({
            title: "Existing Budget",
            target: 500,
            period: "weekly",
            periodTime: 1,
            dateStart: "2025-03-01",
            dateEnd: "2025-03-07",
            categoriesRef: ["cat1"],
            tagRefs: ["tag1"],
            newTagRefs: [],
        });

        expect(budgetRepo.isBudgetExistByName).toHaveBeenCalledWith("Existing Budget");
        expect(presenter.success).toHaveBeenCalledWith(false);
        expect(presenter.fail).toHaveBeenCalledWith(new ResourceAlreadyExist("Budget already exist"));
    });

    test("should fail if a category does not exist", async () => {
        (budgetRepo.isBudgetExistByName as jest.Mock).mockResolvedValue(false);
        (categoryRepo.isCategoryExistByIds as jest.Mock).mockResolvedValue(false);

        const useCase = new CreationBudgetUseCase(adapters, dateService, presenter);

        await useCase.execute({
            title: "New Budget",
            target: 300,
            period: "yearly",
            periodTime: 1,
            dateStart: "2025-01-01",
            dateEnd: "2025-12-31",
            categoriesRef: ["invalid_cat"],
            tagRefs: ["tag1"],
            newTagRefs: [],
        });

        expect(categoryRepo.isCategoryExistByIds).toHaveBeenCalledWith(["invalid_cat"]);
        expect(presenter.success).toHaveBeenCalledWith(false);
        expect(presenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("Category not exist"));
    });

    test("should fail if a tag does not exist", async () => {
        (budgetRepo.isBudgetExistByName as jest.Mock).mockResolvedValue(false);
        (categoryRepo.isCategoryExistByIds as jest.Mock).mockResolvedValue(true);
        (tagRepo.isTagExistByIds as jest.Mock).mockResolvedValue(false);

        const useCase = new CreationBudgetUseCase(adapters, dateService, presenter);

        await useCase.execute({
            title: "Budget with Tags",
            target: 200,
            period: "monthly",
            periodTime: 1,
            dateStart: "2025-03-01",
            dateEnd: "2025-03-31",
            categoriesRef: ["cat1"],
            tagRefs: ["invalid_tag"],
            newTagRefs: [],
        });

        expect(tagRepo.isTagExistByIds).toHaveBeenCalledWith(["invalid_tag"]);
        expect(presenter.success).toHaveBeenCalledWith(false);
        expect(presenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("an tag not found"));
    });
});

describe("DeleteBudgetUseCase", () => {
    let budgetRepo: BudgetRepository;
    let presenter: IDeleteBudgetUseCaseResponse;

    beforeEach(() => {
        budgetRepo = {
            isBudgetExistById: jest.fn(),
            delete: jest.fn(),
        };

        presenter = {
            success: jest.fn(),
            fail: jest.fn(),
        };
    });

    test("should delete a budget successfully", async () => {
        (budgetRepo.isBudgetExistById as jest.Mock).mockResolvedValue(true);
        (budgetRepo.delete as jest.Mock).mockResolvedValue(undefined);

        const useCase = new DeleteBudgetUseCase(budgetRepo, presenter);

        await useCase.execute("budgetId");

        expect(budgetRepo.isBudgetExistById).toHaveBeenCalledWith("budgetId");
        expect(budgetRepo.delete).toHaveBeenCalledWith("budgetId");
        expect(presenter.success).toHaveBeenCalledWith(true);
    });

    test("should fail if budget does not exist", async () => {
        (budgetRepo.isBudgetExistById as jest.Mock).mockResolvedValue(false);

        const useCase = new DeleteBudgetUseCase(budgetRepo, presenter);

        await useCase.execute("invalidBudgetId");

        expect(budgetRepo.isBudgetExistById).toHaveBeenCalledWith("invalidBudgetId");
        expect(presenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("Budget not found"));
        expect(presenter.success).toHaveBeenCalledWith(false);
    });

    test("should handle any other errors", async () => {
        const error = new Error("Unexpected error");
        (budgetRepo.isBudgetExistById as jest.Mock).mockRejectedValue(error);

        const useCase = new DeleteBudgetUseCase(budgetRepo, presenter);

        await useCase.execute("budgetId");

        expect(budgetRepo.isBudgetExistById).toHaveBeenCalledWith("budgetId");
        expect(presenter.fail).toHaveBeenCalledWith(error);
        expect(presenter.success).toHaveBeenCalledWith(false);
    });
})


describe("GetAllBudgetUseCase", () => {
    let budgetRepo: BudgetRepository;
    let transactionRepo: TransactionRepository;
    let categoryRepo: CategoryRepository;
    let tagRepo: TagRepository;
    let presenter: IGetAllBudgetUseCaseResponse;

    beforeEach(() => {
        budgetRepo = {
            getAll: jest.fn(),
        };
        transactionRepo = {
            getBalance: jest.fn(),
        };
        categoryRepo = {
            get: jest.fn(),
        };
        tagRepo = {
            get: jest.fn(),
        };
        presenter = {
            success: jest.fn(),
            fail: jest.fn(),
        };
    });

    test("should fetch and return all budgets successfully", async () => {
        const budgetsMock = [
            {
                getId: jest.fn().mockReturnValue("budget1"),
                getTitle: jest.fn().mockReturnValue("Budget 1"),
                getCategories: jest.fn().mockReturnValue(["cat1", "cat2"]),
                getTags: jest.fn().mockReturnValue(["tag1"]),
                getPeriod: jest.fn().mockReturnValue("monthly"),
                getPeriodTime: jest.fn().mockReturnValue(1),
                getTarget: jest.fn().mockReturnValue(1000),
                getDateStart: jest.fn().mockReturnValue("2025-01-01"),
                getDateEnd: jest.fn().mockReturnValue("2025-12-31"),
                getDateUpdate: jest.fn().mockReturnValue("2025-01-01"),
            },
        ];

        const categoriesMock = [
            {
                getId: jest.fn().mockReturnValue("cat1"),
                getTitle: jest.fn().mockReturnValue("Category 1"),
                getIconId: jest.fn().mockReturnValue("icon1"),
                getColor: jest.fn().mockReturnValue("red"),
            },
            {
                getId: jest.fn().mockReturnValue("cat2"),
                getTitle: jest.fn().mockReturnValue("Category 2"),
                getIconId: jest.fn().mockReturnValue("icon2"),
                getColor: jest.fn().mockReturnValue("blue"),
            },
        ];

        const tagsMock = [
            {
                getId: jest.fn().mockReturnValue("tag1"),
                getValue: jest.fn().mockReturnValue("Tag 1"),
                getColor: jest.fn().mockReturnValue("green"),
            },
        ];

        const balanceMock = {
            getAmount: jest.fn().mockReturnValue(500),
        };

        // Mocking method calls
        (budgetRepo.getAll as jest.Mock).mockResolvedValue(budgetsMock);
        (categoryRepo.get as jest.Mock).mockResolvedValueOnce(categoriesMock[0]).mockResolvedValueOnce(categoriesMock[1]);
        (tagRepo.get as jest.Mock).mockResolvedValueOnce(tagsMock[0]);
        (transactionRepo.getBalance as jest.Mock).mockResolvedValue(balanceMock);

        const useCase = new GetAllBudgetUseCase(
            {
                budgetRepository: budgetRepo,
                categoryRepository: categoryRepo,
                tagRepository: tagRepo,
                transactionRepository: transactionRepo,
            },
            presenter
        );

        await useCase.execute();

        const expectedResponse: BudgetOutput[] = [
            {
                id: "budget1",
                title: "Budget 1",
                target: 1000,
                categories: [
                    { id: "cat1", title: "Category 1", icon: "icon1", color: "red" },
                    { id: "cat2", title: "Category 2", icon: "icon2", color: "blue" },
                ],
                tags: [
                    { id: "tag1", title: "Tag 1", color: "green" },
                ],
                period: "monthly",
                periodTime: 1,
                currentBalance: 500,
                startDate: "2025-01-01",
                updateDate: "2025-01-01",
                endDate: "2025-12-31",
            },
        ];

        expect(presenter.success).toHaveBeenCalledWith(expectedResponse);
        expect(presenter.fail).not.toHaveBeenCalled();
    });

    test("should handle errors and call fail", async () => {
        (budgetRepo.getAll as jest.Mock).mockRejectedValue(new Error("Failed to fetch budgets"));

        const useCase = new GetAllBudgetUseCase(
            {
                budgetRepository: budgetRepo,
                categoryRepository: categoryRepo,
                tagRepository: tagRepo,
                transactionRepository: transactionRepo,
            },
            presenter
        );

        await useCase.execute();

        expect(presenter.fail).toHaveBeenCalledWith(new Error("Failed to fetch budgets"));
        expect(presenter.success).not.toHaveBeenCalled();
    });
});

describe("GetBudgetUseCase", () => {
    let budgetRepo: BudgetRepository;
    let transactionRepo: TransactionRepository;
    let categoryRepo: CategoryRepository;
    let tagRepo: TagRepository;
    let presenter: IGetBudgetUseCaseResponse;

    beforeEach(() => {
        budgetRepo = {
            get: jest.fn(),
        };
        transactionRepo = {
            getBalance: jest.fn(),
        };
        categoryRepo = {
            get: jest.fn(),
        };
        tagRepo = {
            get: jest.fn(),
        };
        presenter = {
            success: jest.fn(),
            fail: jest.fn(),
        };
    });

    test("should fetch and return a single budget successfully", async () => {
        const budgetMock = {
            getId: jest.fn().mockReturnValue("budget1"),
            getTitle: jest.fn().mockReturnValue("Budget 1"),
            getCategories: jest.fn().mockReturnValue(["cat1", "cat2"]),
            getTags: jest.fn().mockReturnValue(["tag1"]),
            getPeriod: jest.fn().mockReturnValue("monthly"),
            getPeriodTime: jest.fn().mockReturnValue(1),
            getTarget: jest.fn().mockReturnValue(1000),
            getDateStart: jest.fn().mockReturnValue("2025-01-01"),
            getDateEnd: jest.fn().mockReturnValue("2025-12-31"),
            getDateUpdate: jest.fn().mockReturnValue("2025-01-01"),
        };

        const categoriesMock = [
            {
                getId: jest.fn().mockReturnValue("cat1"),
                getTitle: jest.fn().mockReturnValue("Category 1"),
                getIconId: jest.fn().mockReturnValue("icon1"),
                getColor: jest.fn().mockReturnValue("red"),
            },
            {
                getId: jest.fn().mockReturnValue("cat2"),
                getTitle: jest.fn().mockReturnValue("Category 2"),
                getIconId: jest.fn().mockReturnValue("icon2"),
                getColor: jest.fn().mockReturnValue("blue"),
            },
        ];

        const tagsMock = [
            {
                getId: jest.fn().mockReturnValue("tag1"),
                getValue: jest.fn().mockReturnValue("Tag 1"),
                getColor: jest.fn().mockReturnValue("green"),
            },
        ];

        const balanceMock = {
            getAmount: jest.fn().mockReturnValue(500),
        };

        // Mocking method calls
        (budgetRepo.get as jest.Mock).mockResolvedValue(budgetMock);
        (categoryRepo.get as jest.Mock).mockResolvedValueOnce(categoriesMock[0]).mockResolvedValueOnce(categoriesMock[1]);
        (tagRepo.get as jest.Mock).mockResolvedValueOnce(tagsMock[0]);
        (transactionRepo.getBalance as jest.Mock).mockResolvedValue(balanceMock);

        const useCase = new GetBudgetUseCase(
            {
                budgetRepository: budgetRepo,
                categoryRepository: categoryRepo,
                tagRepository: tagRepo,
                transactionRepository: transactionRepo,
            },
            presenter
        );

        await useCase.execute("budget1");

        const expectedResponse: BudgetOutput = {
            id: "budget1",
            title: "Budget 1",
            target: 1000,
            categories: [
                { id: "cat1", title: "Category 1", icon: "icon1", color: "red" },
                { id: "cat2", title: "Category 2", icon: "icon2", color: "blue" },
            ],
            tags: [
                { id: "tag1", title: "Tag 1", color: "green" },
            ],
            period: "monthly",
            periodTime: 1,
            currentBalance: 500,
            startDate: "2025-01-01",
            updateDate: "2025-01-01",
            endDate: "2025-12-31",
        };

        expect(presenter.success).toHaveBeenCalledWith(expectedResponse);
        expect(presenter.fail).not.toHaveBeenCalled();
    });

    test("should handle errors and call fail", async () => {
        (budgetRepo.get as jest.Mock).mockRejectedValue(new Error("Budget not found"));

        const useCase = new GetBudgetUseCase(
            {
                budgetRepository: budgetRepo,
                categoryRepository: categoryRepo,
                tagRepository: tagRepo,
                transactionRepository: transactionRepo,
            },
            presenter
        );

        await useCase.execute("budget1");

        expect(presenter.fail).toHaveBeenCalledWith(new Error("Budget not found"));
        expect(presenter.success).not.toHaveBeenCalled();
    });
});


describe("UpdateBudgetUseCase", () => {
    let budgetRepo: BudgetRepository;
    let categoryRepo: CategoryRepository;
    let tagRepo: TagRepository;
    let dateService: DateService;
    let presenter: IUpdateBudgetUseCasePresenter;

    beforeEach(() => {
        budgetRepo = {
            get: jest.fn(),
            update: jest.fn(),
        };
        categoryRepo = {
            isCategoryExistByIds: jest.fn(),
        };
        tagRepo = {
            isTagExistByIds: jest.fn(),
        };
        dateService = {
            formatDate: jest.fn().mockReturnValue("2025-01-01"),
            getDateAddition: jest.fn(),
        };
        presenter = {
            success: jest.fn(),
            fail: jest.fn(),
        };
    });

    test("should update a budget successfully", async () => {
        const request: RequestUpdateBudget = {
            id: "budget1",
            title: "Updated Budget",
            target: 2000,
            dateStart: "2025-01-01",
            dateEnd: "2025-12-31",
            period: "monthly",
            periodTime: 1,
            tagsRef: ["tag1", "tag2"],
            categoriesRef: ["cat1", "cat2"],
        };

        const budgetMock = {
            getId: jest.fn().mockReturnValue("budget1"),
            setTarget: jest.fn(),
            setDateStart: jest.fn(),
            setCategories: jest.fn(),
            setTags: jest.fn(),
            setPeriod: jest.fn(),
            setPeriodTime: jest.fn(),
            setDateEnd: jest.fn(),
            hasChange: jest.fn().mockReturnValue(true),
        };

        (budgetRepo.get as jest.Mock).mockResolvedValue(budgetMock);
        (categoryRepo.isCategoryExistByIds as jest.Mock).mockResolvedValue(true);
        (tagRepo.isTagExistByIds as jest.Mock).mockResolvedValue(true);

        const useCase = new UpdateBudgetUseCase(
            {
                budgetRepository: budgetRepo,
                categoryRepository: categoryRepo,
                tagRepository: tagRepo,
                dateService: dateService,
            },
            presenter
        );

        await useCase.execute(request);

        expect(budgetMock.setTarget).toHaveBeenCalledWith(request.target);
        expect(budgetMock.setCategories).toHaveBeenCalledWith(request.categoriesRef);
        expect(budgetMock.setTags).toHaveBeenCalledWith(request.tagsRef);
        expect(budgetMock.setDateStart).toHaveBeenCalledWith("2025-01-01");
        expect(budgetMock.setDateEnd).toHaveBeenCalledWith("2025-01-01");
        expect(budgetMock.setPeriod).toHaveBeenCalledWith("monthly");
        expect(budgetMock.setPeriodTime).toHaveBeenCalledWith(1);
        expect(budgetRepo.update).toHaveBeenCalledWith(budgetMock);

        expect(presenter.success).toHaveBeenCalledWith(true);
        expect(presenter.fail).not.toHaveBeenCalled();
    });

    test("should throw an error if categories do not exist", async () => {
        const request: RequestUpdateBudget = {
            id: "budget1",
            title: "Updated Budget",
            target: 2000,
            dateStart: "2025-01-01",
            dateEnd: "2025-12-31",
            period: "monthly",
            periodTime: 1,
            tagsRef: ["tag1", "tag2"],
            categoriesRef: ["cat1", "cat2"],
        };

        const budgetMock = {
            getId: jest.fn().mockReturnValue("budget1"),
            setTarget: jest.fn(),
            setDateStart: jest.fn(),
            setCategories: jest.fn(),
            setTags: jest.fn(),
            setPeriod: jest.fn(),
            setPeriodTime: jest.fn(),
            setDateEnd: jest.fn(),
            hasChange: jest.fn().mockReturnValue(true),
        };

        (budgetRepo.get as jest.Mock).mockResolvedValue(budgetMock);
        (categoryRepo.isCategoryExistByIds as jest.Mock).mockResolvedValue(false);
        (tagRepo.isTagExistByIds as jest.Mock).mockResolvedValue(true);

        const useCase = new UpdateBudgetUseCase(
            {
                budgetRepository: budgetRepo,
                categoryRepository: categoryRepo,
                tagRepository: tagRepo,
                dateService: dateService,
            },
            presenter
        );

        await useCase.execute(request);

        expect(presenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("Category not exist"));
        expect(presenter.success).not.toHaveBeenCalled();
    });

    test("should throw an error if tags do not exist", async () => {
        const request: RequestUpdateBudget = {
            id: "budget1",
            title: "Updated Budget",
            target: 2000,
            dateStart: "2025-01-01",
            dateEnd: "2025-12-31",
            period: "monthly",
            periodTime: 1,
            tagsRef: ["tag1", "tag2"],
            categoriesRef: ["cat1", "cat2"],
        };

        const budgetMock = {
            getId: jest.fn().mockReturnValue("budget1"),
            setTarget: jest.fn(),
            setDateStart: jest.fn(),
            setCategories: jest.fn(),
            setTags: jest.fn(),
            setPeriod: jest.fn(),
            setPeriodTime: jest.fn(),
            setDateEnd: jest.fn(),
            hasChange: jest.fn().mockReturnValue(true),
        };

        (budgetRepo.get as jest.Mock).mockResolvedValue(budgetMock);
        (categoryRepo.isCategoryExistByIds as jest.Mock).mockResolvedValue(true);
        (tagRepo.isTagExistByIds as jest.Mock).mockResolvedValue(false);

        const useCase = new UpdateBudgetUseCase(
            {
                budgetRepository: budgetRepo,
                categoryRepository: categoryRepo,
                tagRepository: tagRepo,
                dateService: dateService,
            },
            presenter
        );

        await useCase.execute(request);

        expect(presenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("an tag not found"));
        expect(presenter.success).not.toHaveBeenCalled();
    });
});

jest.mock("../../repositories/budgetRepository");
jest.mock("@/core/adapters/libs");

describe("AutoUpdateBudgetUseCase", () => {
    let mockBudgetRepo: jest.Mocked<BudgetRepository>;
    let mockDateService: jest.Mocked<DateService>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };
    let useCase: AutoUpdateBudgetUseCase;

    beforeEach(() => {
        // Create mocks for the dependencies
        mockBudgetRepo = new BudgetRepository() as jest.Mocked<BudgetRepository>;
        mockDateService = new DateService() as jest.Mocked<DateService>;
        mockPresenter = { success: jest.fn(), fail: jest.fn() };

        // Instantiate the use case with mocked dependencies
        useCase = new AutoUpdateBudgetUseCase(mockPresenter, mockBudgetRepo, mockDateService);
    });

    it("should successfully update budgets", async () => {
        const mockBudgets = [
            new Budget("1", "Budget 1", 1000, ["cat1"], ["tag1"], "2025-01-01", "2025-01-31", "MONTHLY", 1, "2025-01-01"),
            new Budget("2", "Budget 2", 1500, ["cat2"], ["tag2"], "2025-02-01", "2025-02-28", "MONTHLY", 1, "2025-02-01")
        ];

        // Mock the repository and date service methods
        mockBudgetRepo.getAll.mockResolvedValue(mockBudgets);
        mockDateService.getToday.mockReturnValue("2025-01-15");
        mockDateService.compareDate.mockReturnValue(1); // Means the current date is after budget end date
        mockDateService.getDateAddition.mockReturnValue("2025-02-01");

        // Mock methods for updating and archiving budgets
        mockBudgetRepo.update.mockResolvedValue(true);
        mockBudgetRepo.toggleArchived.mockResolvedValue(true);

        // Call the execute method
        await useCase.execute();

        // Test that success presenter is called
        expect(mockPresenter.success).toHaveBeenCalledWith('All budgets updated successfully');

        // Test that the repository methods were called as expected
        expect(mockBudgetRepo.getAll).toHaveBeenCalled();
        expect(mockBudgetRepo.update).toHaveBeenCalledTimes(1); // Ensure update was called once
        expect(mockBudgetRepo.toggleArchived).toHaveBeenCalledWith("1", true); // Budget with expired date should be archived
    });

    it("should archive budgets where the dateEnd is passed", async () => {
        const mockBudgets = [
            new Budget("1", "Budget 1", 1000, ["cat1"], ["tag1"], "2025-01-01", "2025-01-15", "MONTHLY", 1, "2025-01-01")
        ];

        mockBudgetRepo.getAll.mockResolvedValue(mockBudgets);
        mockDateService.getToday.mockReturnValue("2025-01-16");
        mockDateService.compareDate.mockReturnValue(1); // Current date is after `dateEnd`

        // Call the execute method
        await useCase.execute();

        // Test if the budget was archived
        expect(mockBudgetRepo.toggleArchived).toHaveBeenCalledWith("1", true);
    });

    it("should update budgets with a defined period", async () => {
        const mockBudgets = [
            new Budget("2", "Budget 2", 1500, ["cat2"], ["tag2"], "2025-02-01", "2025-02-28", "MONTHLY", 1, "2025-02-01")
        ];

        mockBudgetRepo.getAll.mockResolvedValue(mockBudgets);
        mockDateService.getToday.mockReturnValue("2025-02-15");
        mockDateService.compareDate.mockReturnValue(-1); // Means the current date is before budget end date
        mockDateService.getDateAddition.mockReturnValue("2025-03-01");

        // Call the execute method
        await useCase.execute();

        // Ensure the dateUpdate was updated
        expect(mockBudgetRepo.update).toHaveBeenCalled();
        expect(mockDateService.getDateAddition).toHaveBeenCalled();
    });

    it("should handle errors and fail gracefully", async () => {
        const errorMessage = "An error occurred while updating the budget";

        // Mock the repository to throw an error
        mockBudgetRepo.getAll.mockRejectedValue(new Error(errorMessage));

        // Call the execute method
        await useCase.execute();

        // Test that fail presenter is called
        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error(errorMessage));
    });
});