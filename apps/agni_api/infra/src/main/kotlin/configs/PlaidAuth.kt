package configs

import com.google.gson.Gson
import com.plaid.client.ApiClient
import com.plaid.client.ApiClient.Production
import com.plaid.client.model.CountryCode
import com.plaid.client.model.ItemPublicTokenExchangeRequest
import com.plaid.client.model.LinkTokenCreateRequest
import com.plaid.client.model.LinkTokenCreateRequestUser
import com.plaid.client.model.LinkTokenCreateResponse
import com.plaid.client.model.PlaidError
import com.plaid.client.model.Products
import com.plaid.client.request.PlaidApi
import domain.exceptions.UnExpectedException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import usecases.dto.Result
import java.util.UUID

@Component
class PlaidAuth(
    @Value("\${plaid.client.clientId}") private val clientId: String,
    @Value("\${plaid.client.secret}") private val secret: String,
    @Value("\${plaid.client.redirectionUrl}") private val redirectionUrl: String,
    @Value("\${plaid.client.isSandbox}") private val isSandBox: Boolean
) {
    private val plaidClient: PlaidApi

    init {
        val apiKey = mapOf(
            "clientId" to clientId,
            "secret" to secret
        )
        val plaidApiClient = ApiClient(apiKey)
        plaidApiClient.setPlaidAdapter(if (isSandBox)  ApiClient.Sandbox else ApiClient.Production )
        plaidClient = plaidApiClient.createService(PlaidApi::class.java)
    }

    fun createLinkToken(userId: UUID): Result<String> {
        val request = LinkTokenCreateRequest()
        request.products=listOf(Products.TRANSACTIONS)
        request.clientName = "Agni App"
        request.countryCodes=listOf(CountryCode.CA)
        request.redirectUri=redirectionUrl
        request.language="fr"
        val user = LinkTokenCreateRequestUser()
        user.clientUserId = userId.toString()
        request.user=user

        val response = plaidClient.linkTokenCreate(request).execute()

        if (response.isSuccessful) {
            return Result.success(response.body()!!.linkToken)
        }

        try {
            val gson = Gson()
            val error = gson.fromJson(response.errorBody()?.string(), PlaidError::class.java)
            return Result.fail(UnExpectedException("Plaid to Error Code ${error.errorCode}; detail: ${error.errorMessage}"))
        } catch (e: Exception) {
            return Result.fail(UnExpectedException("Failed converting from API Response Error Body to Error ${response.errorBody()?.string()}"))
        }
    }

    fun tokenExchangeRequest(publicToken: String): Result<String> {
        val request = ItemPublicTokenExchangeRequest().publicToken(publicToken)
        val response = plaidClient.itemPublicTokenExchange(request).execute()

        if (response.isSuccessful) {
            return Result.success(response.body()!!.accessToken)
        }

        try {
            val gson = Gson()
            val error = gson.fromJson(response.errorBody()?.string(), PlaidError::class.java)
            return Result.fail(UnExpectedException("Plaid to Error Code ${error.errorCode}; detail: ${error.errorMessage}"))
        } catch (e: Exception) {
            return Result.fail(UnExpectedException("Failed converting from API Response Error Body to Error ${response.errorBody()?.string()}"))
        }
    }

}