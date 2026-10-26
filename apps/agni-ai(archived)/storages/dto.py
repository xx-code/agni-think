from pydantic import BaseModel


class QdrantDocument(BaseModel):
    id: str
    document: str

class QdrantAddDocumentInput(BaseModel):
    collection_name: str
    documents: list[QdrantDocument]
