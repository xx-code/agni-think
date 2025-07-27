import TagController from "src/controllers/tags";
import container from "src/di_contenair";

const usecases = container.tagUseCase;

if (usecases === undefined)
    throw new Error("Tags Usecases not declare");

const tagRouters = new TagController(
    usecases.createTag,
    usecases.updateTag,
    usecases.deleteTag,
    usecases.getAllTag,
    usecases.getTag
);

export default tagRouters.getRoute();