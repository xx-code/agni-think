from qdrant_client import QdrantClient
from qdrant_client import models
from storages.dto import QdrantAddDocumentInput

class QdrantClientService:

    def __init__(self, collection_name: str, qdrant_url: str):
        self.url = qdrant_url
        self.collection_name = collection_name

    def add_embedding_text(self, input: QdrantAddDocumentInput):
        client = QdrantClient(url=self.url)
        client.add(
            documents=[input.document],
            collection_name=self.collection_name,
            ids=[input.id]
        )
    
    def remove_embedding_text(self, id: str):
        client = QdrantClient(url=self.url)
        client.delete(
            collection_name=self.collection_name,
            points_selector=models.PointIdsList(points=[id]),
            wait=True # Optional: wait for the operation to complete
        )