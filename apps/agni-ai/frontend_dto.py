from pydantic import BaseModel

class ExchangeTokenDto(BaseModel):
    public_token: str

class TreatInvoiceDto(BaseModel):
    text: str