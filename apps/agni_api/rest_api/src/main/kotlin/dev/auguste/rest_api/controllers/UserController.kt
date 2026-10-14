package dev.auguste.rest_api.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import usecases.interfaces.IUseCase
import usecases.users.dto.GetUserOutput

@RestController
@RequestMapping("/v2/users")
class UserController(
    private val getCurrentUserUseCase: IUseCase<Unit, GetUserOutput>
) {
    @GetMapping("/me")
    suspend fun getCurrentUser(): ResponseEntity<GetUserOutput> {

        return ResponseEntity.ok(getCurrentUserUseCase(Unit).getOrThrow())
    }
}
