package dev.auguste.rest_api.controllers

import dev.auguste.rest_api.controllers.models.ApiCreateProfileModel
import dev.auguste.rest_api.controllers.models.ApiUpdateProfileModel
import dev.auguste.rest_api.controllers.models.mapApiCreateProfileToCreateProfile
import dev.auguste.rest_api.controllers.models.mapApiUpdateProfileToUpdateProfile
import dev.auguste.rest_api.controllers.models.tempPrivateProfileKey
import usecases.dto.CreatedOutput
import usecases.interfaces.IUseCase
import usecases.profiles.dto.CreateProfileInput
import usecases.profiles.dto.GetProfileOutput
import usecases.profiles.dto.UpdateProfileInput
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


@RestController
@RequestMapping("/v2/profiles")
class ProfileController(
    val createProfileUseCase: IUseCase<CreateProfileInput, CreatedOutput>,
    val updateProfileUseCase: IUseCase<UpdateProfileInput, Unit>,
    val getProfileUseCase: IUseCase<UUID, GetProfileOutput>
) {

    @PostMapping
    suspend fun createProvision(@Valid @RequestBody request: ApiCreateProfileModel) : ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createProfileUseCase.execute(mapApiCreateProfileToCreateProfile(request)).getOrThrow())
    }

    @GetMapping("/{id}")
    suspend fun getProfile(@PathVariable id: UUID): ResponseEntity<GetProfileOutput> {
        return ResponseEntity.ok(getProfileUseCase.execute(tempPrivateProfileKey).getOrThrow())
    }

    @PutMapping("/{id}")
    suspend fun updateProfile(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateProfileModel) : ResponseEntity<Unit> {
        return ResponseEntity.ok(updateProfileUseCase.execute(mapApiUpdateProfileToUpdateProfile(id, request)).getOrThrow())
    }
}