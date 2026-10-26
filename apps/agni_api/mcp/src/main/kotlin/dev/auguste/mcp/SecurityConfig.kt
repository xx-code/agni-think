package dev.auguste.mcp

import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.User
import domain.exceptions.UnAuthorizeException
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Component
import javax.crypto.spec.SecretKeySpec

class JwtAudienceValidator(private val audience: String) : OAuth2TokenValidator<Jwt> {
    override fun validate(jwt: Jwt): org.springframework.security.oauth2.core.OAuth2TokenValidatorResult {
        val audiences = jwt.audience
        if (audiences.contains(audience)) {
            return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success()
        }
        return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
            org.springframework.security.oauth2.core.OAuth2Error("invalid_token", "The required audience is missing", null)
        )
    }
}

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    @Value("\${agni.local.secret-key:my-default-very-secret-local-key-change-me}")
    private val secretKey: String
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity, jwtDecoder: ObjectProvider<JwtDecoder>): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/public/**",
                        "/",
                        "/health",
                        "/actuator/health",
                        "/actuator/health/**"
                    ).permitAll()
                    .anyRequest().authenticated()
            }

        jwtDecoder.ifAvailable?.let { decoder ->

            http.oauth2ResourceServer { oauth ->

                oauth.jwt { it.decoder(decoder) }

            }

        }

        return http.build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {

        val key = SecretKeySpec(secretKey.toByteArray(), "HmacSHA256")

        return NimbusJwtDecoder.withSecretKey(key).build()

    }
}

@Component
class OAuth2Adapter(
    private val userRepo: IRepository<User>,
) : IAuthenticateAdapter {

    override suspend fun isAuthenticate(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication ?: return false
        return authentication.isAuthenticated && authentication.principal is Jwt
    }

    override suspend fun getCurrentUser(): User {
        val jwt = currentJwt() ?: throw UnAuthorizeException.UserUnAuthorizedException()

        val apiToken = (jwt.subject ?: jwt.getClaimAsString("api_key"))
            ?.takeIf { it.isNotBlank() }
            ?: throw UnAuthorizeException.UserUnAuthorizedException()

        val condition = QueryExtendBuilder<User>()
            .addCondition("apiToken", QueryComparator.Equal, apiToken)
        return userRepo.getAll(QueryFilter.queryAll(), condition).items.firstOrNull()
            ?: throw UnAuthorizeException.UserUnAuthorizedException()
    }

    private fun currentJwt(): Jwt? {
        val authentication = SecurityContextHolder.getContext().authentication ?: return null
        return when (val principal = authentication.principal) {
            is Jwt -> principal
            is JwtAuthenticationToken -> principal.token
            else -> null
        }
    }
}

