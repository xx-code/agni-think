import { SAVING_CATEGORY_ID, TRANSFERT_CATEGORY_ID, FREEZE_CATEGORY_ID } from "@core/domains/constants";
import { Account } from "@core/domains/entities/account";
import { Category } from "@core/domains/entities/category";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import ValidationError from "@core/errors/validationError";
import { IUpdateAccountUseCaseResponse, UpdateAccountUseCase } from "@core/interactions/account/updateAccountUseCase";
import { ICreationCategoryUseCaseResponse, CreationCategoryUseCase, RequestCreationCategoryUseCase } from "@core/interactions/category/creationCategoryUseCase";
import { IDeleteCategoryUseCaseResponse, DeleteCategoryUseCase } from "@core/interactions/category/deleteCategoryUseCase";
import { IGetAllCategoryUseCaseResponse, GetAllCategoryUseCase } from "@core/interactions/category/getAllCategoryUseCase";
import { AccountRepository } from "@core/repositories/accountRepository";
import { CategoryRepository } from "@core/repositories/categoryRepository";


describe("CreationCategoryUseCase", () => {
    let categoryRepo: CategoryRepository;
    let presenter: ICreationCategoryUseCaseResponse;

    beforeEach(() => {
        categoryRepo = {
            isCategoryExistByName: jest.fn(),
            save: jest.fn(),
            isCategoryExistById: jest.fn(),
            isCategoryExistByIds: jest.fn(),
            delete: jest.fn(),
            update: jest.fn(),
            get: jest.fn(),
            getByTitle: jest.fn(),
            getAll: jest.fn(),
        };

        presenter = {
            success: jest.fn(),
            fail: jest.fn(),
        };
    });

    test("should create a new category successfully", async () => {
        (categoryRepo.isCategoryExistByName as jest.Mock).mockResolvedValue(false);
        (categoryRepo.save as jest.Mock).mockResolvedValue(undefined);

        const creationCategoryUseCase = new CreationCategoryUseCase(categoryRepo, presenter);
        const request: RequestCreationCategoryUseCase = {
            title: "Food",
            icon: "🍔",
            color: "#ff5733",
        };

        await creationCategoryUseCase.execute(request);

        expect(categoryRepo.isCategoryExistByName).toHaveBeenCalledWith("Food");
        expect(categoryRepo.save).toHaveBeenCalled();
        expect(presenter.success).toHaveBeenCalledWith(true);
    });

    test("should fail if category title already exists", async () => {
        (categoryRepo.isCategoryExistByName as jest.Mock).mockResolvedValue(true);

        const creationCategoryUseCase = new CreationCategoryUseCase(categoryRepo, presenter);
        const request: RequestCreationCategoryUseCase = {
            title: "Food",
            icon: "🍔",
            color: "#ff5733",
        };

        await creationCategoryUseCase.execute(request);

        expect(categoryRepo.isCategoryExistByName).toHaveBeenCalledWith("Food");
        expect(presenter.fail).toHaveBeenCalledWith(new ValidationError("This category is already use"));
    });

    test("should use default color if no color is provided", async () => {
        (categoryRepo.isCategoryExistByName as jest.Mock).mockResolvedValue(false);
        (categoryRepo.save as jest.Mock).mockResolvedValue(undefined);

        const creationCategoryUseCase = new CreationCategoryUseCase(categoryRepo, presenter);
        const request: RequestCreationCategoryUseCase = {
            title: "Travel",
            icon: "✈️",
            color: null,
        };

        await creationCategoryUseCase.execute(request);

        expect(categoryRepo.isCategoryExistByName).toHaveBeenCalledWith("Travel");
        expect(categoryRepo.save).toHaveBeenCalledWith(expect.objectContaining({ color: "#7f7f7f" }));
        expect(presenter.success).toHaveBeenCalledWith(true);
    });

    test("should fail if repository throws an error", async () => {
        (categoryRepo.isCategoryExistByName as jest.Mock).mockRejectedValue(new Error("Database error"));

        const creationCategoryUseCase = new CreationCategoryUseCase(categoryRepo, presenter);
        const request: RequestCreationCategoryUseCase = {
            title: "Shopping",
            icon: "🛒",
            color: "#00ff00",
        };

        await creationCategoryUseCase.execute(request);

        expect(presenter.fail).toHaveBeenCalledWith(new Error("Database error"));
    });
});

describe("DeleteCategoryUseCase", () => {
    let categoryRepo: CategoryRepository;
    let presenter: IDeleteCategoryUseCaseResponse;

    beforeEach(() => {
        categoryRepo = {
            isCategoryExistByName: jest.fn(),
            save: jest.fn(),
            isCategoryExistById: jest.fn(),
            isCategoryExistByIds: jest.fn(),
            delete: jest.fn(),
            update: jest.fn(),
            get: jest.fn(),
            getByTitle: jest.fn(),
            getAll: jest.fn(),
        };

        presenter = {
            success: jest.fn(),
            fail: jest.fn(),
        };
    });

    test("should delete a category successfully", async () => {
        (categoryRepo.isCategoryExistById as jest.Mock).mockResolvedValue(true);
        (categoryRepo.delete as jest.Mock).mockResolvedValue(undefined);

        const deleteCategoryUseCase = new DeleteCategoryUseCase(categoryRepo, presenter);
        const categoryId = "12345";

        await deleteCategoryUseCase.execute(categoryId);

        expect(categoryRepo.isCategoryExistById).toHaveBeenCalledWith(categoryId);
        expect(categoryRepo.delete).toHaveBeenCalledWith(categoryId);
        expect(presenter.success).toHaveBeenCalledWith(true);
    });

    test("should fail if category does not exist", async () => {
        (categoryRepo.isCategoryExistById as jest.Mock).mockResolvedValue(false);

        const deleteCategoryUseCase = new DeleteCategoryUseCase(categoryRepo, presenter);
        const categoryId = "nonexistent";

        await deleteCategoryUseCase.execute(categoryId);

        expect(categoryRepo.isCategoryExistById).toHaveBeenCalledWith(categoryId);
        expect(presenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("Category not found"));
    });

    test("should fail if repository throws an error", async () => {
        (categoryRepo.isCategoryExistById as jest.Mock).mockRejectedValue(new Error("Database error"));

        const deleteCategoryUseCase = new DeleteCategoryUseCase(categoryRepo, presenter);
        const categoryId = "error_case";

        await deleteCategoryUseCase.execute(categoryId);

        expect(presenter.fail).toHaveBeenCalledWith(new Error("Database error"));
    });
});


describe("GetAllCategoryUseCase", () => {
    let categoryRepo: CategoryRepository;
    let presenter: IGetAllCategoryUseCaseResponse;

    beforeEach(() => {
        categoryRepo = {
            isCategoryExistByName: jest.fn(),
            save: jest.fn(),
            isCategoryExistById: jest.fn(),
            isCategoryExistByIds: jest.fn(),
            delete: jest.fn(),
            update: jest.fn(),
            get: jest.fn(),
            getByTitle: jest.fn(),
            getAll: jest.fn(),
        };

        presenter = {
            success: jest.fn(),
            fail: jest.fn(),
        };
    });

    test("should return all categories except system-reserved ones", async () => {
        const mockCategories = [
            new Category("1", "Food", "🍔", "#FF0000"),
            new Category("2", "Transport", "🚗", "#00FF00"),
            new Category(SAVING_CATEGORY_ID, "Saving", "💰", "#0000FF"),
            new Category(TRANSFERT_CATEGORY_ID, "Transfer", "🔄", "#FFFF00"),
            new Category(FREEZE_CATEGORY_ID, "Freeze", "❄️", "#CCCCCC"),
        ];

        (categoryRepo.getAll as jest.Mock).mockResolvedValue(mockCategories);

        const getAllCategoryUseCase = new GetAllCategoryUseCase(categoryRepo, presenter);
        await getAllCategoryUseCase.execute();

        expect(categoryRepo.getAll).toHaveBeenCalled();
        expect(presenter.success).toHaveBeenCalledWith([
            {
                categoryId: "1",
                title: "Food",
                color: "#FF0000",
                icon: "🍔",
            },
            {
                categoryId: "2",
                title: "Transport",
                color: "#00FF00",
                icon: "🚗",
            }
        ]);
    });

    test("should handle repository failure", async () => {
        (categoryRepo.getAll as jest.Mock).mockRejectedValue(new Error("Database error"));

        const getAllCategoryUseCase = new GetAllCategoryUseCase(categoryRepo, presenter);
        await getAllCategoryUseCase.execute();

        expect(categoryRepo.getAll).toHaveBeenCalled();
        expect(presenter.fail).toHaveBeenCalledWith(new Error("Database error"));
    });
});