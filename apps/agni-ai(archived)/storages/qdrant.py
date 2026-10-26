from qdrant_client import QdrantClient
from qdrant_client import models
from storages.dto import QdrantAddDocumentInput


class QdrantClientService:

    def __init__(self, qdrant_url: str):
        self.url = qdrant_url

    def add_embedding_text(self, input: QdrantAddDocumentInput):
        client = QdrantClient(url=self.url)
        client.add(
            documents=[embedding.document for embedding in input.documents],
            collection_name=input.collection_name,
            ids=[embedding.id for embedding in input.documents]
        )
    
    def remove_embedding_text(self, collection_name: str, id: str):
        client = QdrantClient(url=self.url)
        client.delete(
            collection_name=collection_name,
            points_selector=models.PointIdsList(points=[id]),
            wait=True # Optional: wait for the operation to complete
        )