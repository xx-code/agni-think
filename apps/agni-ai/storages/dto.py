from pydantic import BaseModel

class QdrantAddDocumentInput(BaseModel):
    id: str
    document: str