package dev.auguste.agni_api.core.usecases.interfaces

import dev.auguste.agni_api.core.usecases.invoices.dto.CreateInvoiceInput

interface IInnerUseCase<TInput, TOuput>: IUseCase<TInput, TOuput> {
   fun execInnerAsync(input: TInput): TOuput
}