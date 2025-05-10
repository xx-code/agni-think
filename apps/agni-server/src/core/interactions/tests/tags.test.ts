import { Tag } from "@core/domains/entities/tag";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import ValidationError from "@core/errors/validationError";
import { CreationTagUseCase } from "@core/interactions/tag/creationTagUseCase";
import { DeleteTagUseCase } from "@core/interactions/tag/deleteTagUseCase";
import { GetAllTagUseCase } from "@core/interactions/tag/getAllTagsUseCase";
import { GetTagUseCase } from "@core/interactions/tag/getTagUseCase";
import { UpdateTagUseCase, RequestUpdateTagUseCase } from "@core/interactions/tag/updateTagUseCase";
import { TagRepository } from "@core/repositories/tagRepository";

jest.mock("../../repositories/tagRepository");

describe("CreationTagUseCase", () => {
    let mockTagRepo: jest.Mocked<TagRepository>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };
    let useCase: CreationTagUseCase;

    beforeEach(() => {
        mockTagRepo = new TagRepository() as jest.Mocked<TagRepository>;
        mockPresenter = { success: jest.fn(), fail: jest.fn() };
        useCase = new CreationTagUseCase(mockTagRepo, mockPresenter);
    });

    it("should successfully create a new tag", async () => {
        const request = { value: "New Tag", color: "blue" };

        mockTagRepo.isTagExistByName.mockResolvedValue(false); // Tag doesn't exist
        mockTagRepo.save.mockResolvedValue(true);

        await useCase.execute(request);

        expect(mockPresenter.success).toHaveBeenCalledWith(true);
        expect(mockTagRepo.save).toHaveBeenCalledWith(expect.any(Tag)); // Expecting a Tag object
    });

    it("should fail if tag already exists", async () => {
        const request = { value: "Existing Tag", color: "green" };

        mockTagRepo.isTagExistByName.mockResolvedValue(true); // Tag already exists

        await useCase.execute(request);

        expect(mockPresenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("Tag already exists"));
    });

    it("should fail if tag value is empty", async () => {
        const request = { value: "", color: "red" };

        await useCase.execute(request);

        expect(mockPresenter.fail).toHaveBeenCalledWith(new ValidationError("Tag value cannot be empty"));
    });
});

// Mock dependencies
jest.mock("../../repositories/tagRepository");

describe("DeleteTagUseCase", () => {
    let mockTagRepo: jest.Mocked<TagRepository>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };
    let useCase: DeleteTagUseCase;

    beforeEach(() => {
        mockTagRepo = new TagRepository() as jest.Mocked<TagRepository>;
        mockPresenter = { success: jest.fn(), fail: jest.fn() };
        useCase = new DeleteTagUseCase(mockTagRepo, mockPresenter);
    });

    it("should successfully delete a tag if it exists", async () => {
        const tagId = "123";

        // Simulate tag existence
        mockTagRepo.isTagExistById.mockResolvedValue(true); // Tag exists
        mockTagRepo.delete.mockResolvedValue(true); // Tag delete success

        await useCase.execute(tagId);

        // Assert that the success response is triggered
        expect(mockPresenter.success).toHaveBeenCalledWith(true);
        expect(mockTagRepo.delete).toHaveBeenCalledWith(tagId); // Assert delete was called with the correct tag ID
    });

    it("should fail if the tag does not exist", async () => {
        const tagId = "456";

        // Simulate tag does not exist
        mockTagRepo.isTagExistById.mockResolvedValue(false); // Tag doesn't exist

        await useCase.execute(tagId);

        // Assert that fail is triggered with the appropriate error
        expect(mockPresenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("Tag not found"));
        expect(mockTagRepo.delete).not.toHaveBeenCalled(); // Ensure delete was not called
    });

    it("should handle errors thrown by the repository", async () => {
        const tagId = "789";

        // Simulate an error during delete
        mockTagRepo.isTagExistById.mockResolvedValue(true); // Tag exists
        mockTagRepo.delete.mockRejectedValue(new Error("Database error"));

        await useCase.execute(tagId);

        // Assert that fail is triggered with the error message
        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error("Database error"));
    });
})

// Mock dependencies
jest.mock("../../repositories/tagRepository");

describe("GetAllTagUseCase", () => {
    let mockTagRepo: jest.Mocked<TagRepository>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };
    let useCase: GetAllTagUseCase;

    beforeEach(() => {
        mockTagRepo = new TagRepository() as jest.Mocked<TagRepository>;
        mockPresenter = { success: jest.fn(), fail: jest.fn() };
        useCase = new GetAllTagUseCase(mockTagRepo, mockPresenter);
    });

    it("should successfully retrieve all tags", async () => {
        const tagsData = [
            { getId: () => "1", getValue: () => "Tag 1", getColor: () => "Red" },
            { getId: () => "2", getValue: () => "Tag 2", getColor: () => "Blue" },
        ];

        // Mocking the repository to return a list of tags
        mockTagRepo.getAll.mockResolvedValue(tagsData);

        await useCase.execute();

        // Assert that the success response is triggered with the right tags output
        expect(mockPresenter.success).toHaveBeenCalledWith([
            { id: "1", value: "Tag 1", color: "Red" },
            { id: "2", value: "Tag 2", color: "Blue" },
        ]);
    });

    it("should handle when no tags are found", async () => {
        // Mocking the repository to return an empty array
        mockTagRepo.getAll.mockResolvedValue([]);

        await useCase.execute();

        // Assert that success is called with an empty array
        expect(mockPresenter.success).toHaveBeenCalledWith([]);
    });

    it("should fail if there is an error fetching tags", async () => {
        // Simulating an error when fetching tags
        mockTagRepo.getAll.mockRejectedValue(new Error("Database error"));

        await useCase.execute();

        // Assert that fail is triggered with the correct error
        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error("Database error"));
    });
});

// Mock dependencies
jest.mock("../../repositories/tagRepository");

describe("GetTagUseCase", () => {
    let mockTagRepo: jest.Mocked<TagRepository>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };
    let useCase: GetTagUseCase;

    beforeEach(() => {
        mockTagRepo = new TagRepository() as jest.Mocked<TagRepository>;
        mockPresenter = { success: jest.fn(), fail: jest.fn() };
        useCase = new GetTagUseCase(mockTagRepo, mockPresenter);
    });

    it("should successfully retrieve a tag by id", async () => {
        const tagData = { getId: () => "1", getValue: () => "Tag 1", getColor: () => "Red" };

        // Mock the repository to return a tag
        mockTagRepo.get.mockResolvedValue(tagData);

        await useCase.execute("1");

        // Assert that success is called with the expected tag output
        expect(mockPresenter.success).toHaveBeenCalledWith({
            id: "1",
            value: "Tag 1",
            color: "Red"
        });
    });

    it("should fail if the tag is not found", async () => {
        // Mock the repository to throw an error
        mockTagRepo.get.mockRejectedValue(new Error("Tag not found"));

        await useCase.execute("non-existing-id");

        // Assert that fail is called with the error message
        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error("Tag not found"));
    });

    it("should fail if there is an error during tag retrieval", async () => {
        // Mock the repository to throw an unexpected error
        mockTagRepo.get.mockRejectedValue(new Error("Database error"));

        await useCase.execute("1");

        // Assert that fail is called with the correct error message
        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error("Database error"));
    });
});

// Mock dependencies
jest.mock("@/core/repositories/tagRepository");

describe("UpdateTagUseCase", () => {
    let mockTagRepo: jest.Mocked<TagRepository>;
    let mockPresenter: { success: jest.Mock; fail: jest.Mock };
    let useCase: UpdateTagUseCase;

    beforeEach(() => {
        mockTagRepo = new TagRepository() as jest.Mocked<TagRepository>;
        mockPresenter = { success: jest.fn(), fail: jest.fn() };
        useCase = new UpdateTagUseCase(mockTagRepo, mockPresenter);
    });

    it("should successfully update a tag if changes are made", async () => {
        const tagData = {
            getId: () => "1",
            getValue: () => "Old Value",
            getColor: () => "Old Color",
            hasChange: jest.fn().mockReturnValue(true),
            setValue: jest.fn(),
            setColor: jest.fn(),
        };

        const request: RequestUpdateTagUseCase = {
            id: "1",
            value: "New Value",
            color: "New Color",
        };

        // Mock the repository to return the tag
        mockTagRepo.get.mockResolvedValue(tagData);

        await useCase.execute(request);

        // Assert that the repository update method is called
        expect(mockTagRepo.update).toHaveBeenCalledWith(tagData);

        // Assert that success was called
        expect(mockPresenter.success).toHaveBeenCalledWith(true);
    });

    it("should fail if tag is not found", async () => {
        const request: RequestUpdateTagUseCase = {
            id: "non-existing-id",
            value: "New Value",
            color: "New Color",
        };

        // Mock the repository to throw an error if tag is not found
        mockTagRepo.get.mockRejectedValue(new ResourceNotFoundError("Tag not found"));

        await useCase.execute(request);

        // Assert that fail is called with the error
        expect(mockPresenter.fail).toHaveBeenCalledWith(new ResourceNotFoundError("Tag not found"));
    });

    it("should not update if tag has no changes", async () => {
        const tagData = {
            getId: () => "1",
            getValue: () => "Old Value",
            getColor: () => "Old Color",
            hasChange: jest.fn().mockReturnValue(false),
            setValue: jest.fn(),
            setColor: jest.fn(),
        };

        const request: RequestUpdateTagUseCase = {
            id: "1",
            value: "Old Value",
            color: "Old Color",
        };

        // Mock the repository to return the tag
        mockTagRepo.get.mockResolvedValue(tagData);

        await useCase.execute(request);

        // Assert that the update method was not called
        expect(mockTagRepo.update).not.toHaveBeenCalled();

        // Assert that success was called
        expect(mockPresenter.success).toHaveBeenCalledWith(true);
    });

    it("should fail if an unexpected error occurs", async () => {
        const request: RequestUpdateTagUseCase = {
            id: "1",
            value: "New Value",
            color: "New Color",
        };

        // Mock the repository to throw an unexpected error
        mockTagRepo.get.mockRejectedValue(new Error("Database error"));

        await useCase.execute(request);

        // Assert that fail is called with the correct error message
        expect(mockPresenter.fail).toHaveBeenCalledWith(new Error("Database error"));
    });
});