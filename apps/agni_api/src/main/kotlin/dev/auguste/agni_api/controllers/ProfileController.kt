package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiCreateProfileModel
import dev.auguste.agni_api.controllers.models.ApiUpdateProfileModel
import dev.auguste.agni_api.controllers.models.mapApiCreateProfileToCreateProfile
import dev.auguste.agni_api.controllers.models.mapApiUpdateProfileToUpdateProfile
import dev.auguste.agni_api.controllers.models.tempPrivateProfileKey
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.profiles.dto.CreateProfileInput
import dev.auguste.agni_api.core.usecases.profiles.dto.GetProfileOutput
import dev.auguste.agni_api.core.usecases.profiles.dto.UpdateProfileInput
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
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
    fun createProvision(@Valid @RequestBody request: ApiCreateProfileModel) : ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createProfileUseCase.execAsync(mapApiCreateProfileToCreateProfile(request)))
    }

    @GetMapping("/{id}")
    fun getProfile(id: UUID): ResponseEntity<GetProfileOutput> {
        return ResponseEntity.ok(getProfileUseCase.execAsync(tempPrivateProfileKey))
    }

    @PutMapping("/{id}")
    fun updateProfile(id: UUID, @Valid @RequestBody request: ApiUpdateProfileModel) : ResponseEntity<Unit> {
        return ResponseEntity.ok(updateProfileUseCase.execAsync(mapApiUpdateProfileToUpdateProfile(id, request)))
    }
}