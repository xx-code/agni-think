package dev.auguste.agni_api.core.usecases

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Profile
import dev.auguste.agni_api.core.usecases.profiles.CreateProfile
import dev.auguste.agni_api.core.usecases.profiles.GetProfile
import dev.auguste.agni_api.core.usecases.profiles.UpdateProfile
import dev.auguste.agni_api.core.usecases.profiles.dto.CreateProfileInput
import dev.auguste.agni_api.core.usecases.profiles.dto.UpdateProfileInput
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID

private fun buildProfile(
    id: UUID = UUID.randomUUID(),
    maxWishlistAmount: Double = 500.0,
    fixSpendPercentage: Double = 50.0,
    varialSpendPercentage: Double = 30.0,
    savingPercentage: Double = 20.0
): Profile {
    return Profile(
        id = id,
        maxWishlistAmount = maxWishlistAmount,
        fixSpendPercentage = fixSpendPercentage,
        varialSpendPercentage = varialSpendPercentage,
        savingPercentage = savingPercentage
    )
}

private fun mockProfileRepo(): IRepository<Profile> = mockk(relaxed = true)

class CreateProfileTests {

    @Test
    fun `create profile from input and return its id`() {
        val repo = mockProfileRepo()
        val useCase = CreateProfile(repo)
        val input = CreateProfileInput(
            maxWishlistAmount = 1000.0,
            fixSpendPercentage = 50.0,
            varialSpendPercentage = 30.0,
            savingPercentage = 20.0
        )

        val result = useCase.execAsync(input)

        val createdSlot = slot<Profile>()
        verify(exactly = 1) { repo.create(capture(createdSlot)) }

        val created = createdSlot.captured
        assertEquals(result.newId, created.id)
        assertEquals(1000.0, created.maxWishlistAmount)
        assertEquals(50.0, created.fixSpendPercentage)
        assertEquals(30.0, created.varialSpendPercentage)
        assertEquals(20.0, created.savingPercentage)
    }
}

class GetProfileTests {

    @Test
    fun `return mapped output for an existing profile`() {
        val repo = mockProfileRepo()
        val profileId = UUID.randomUUID()
        val profile = buildProfile(
            id = profileId,
            maxWishlistAmount = 800.0,
            fixSpendPercentage = 60.0,
            varialSpendPercentage = 25.0,
            savingPercentage = 15.0
        )
        every { repo.get(profileId) } returns profile
        val useCase = GetProfile(repo)

        val result = useCase.execAsync(profileId)

        assertEquals(800.0, result.maxWishlistAmount)
        assertEquals(60.0, result.fixSpendPercentage)
        assertEquals(25.0, result.varialSpendPercentage)
        assertEquals(15.0, result.savingPercentage)
    }

    @Test
    fun `throw when profile does not exist`() {
        val repo = mockProfileRepo()
        val missingId = UUID.randomUUID()
        every { repo.get(missingId) } returns null
        val useCase = GetProfile(repo)

        assertThrows(DomainException.NotFound.Profile::class.java) {
            useCase.execAsync(missingId)
        }
    }
}

class UpdateProfileTests {

    @Test
    fun `update provided fields and persist`() {
        val repo = mockProfileRepo()
        val profileId = UUID.randomUUID()
        every { repo.get(profileId) } returns buildProfile(id = profileId)
        val useCase = UpdateProfile(repo)

        useCase.execAsync(
            UpdateProfileInput(
                id = profileId,
                maxWishlistAmount = 2000.0,
                varialSpendPercentage = 40.0
            )
        )

        val updatedSlot = slot<Profile>()
        verify(exactly = 1) { repo.update(capture(updatedSlot)) }

        val updated = updatedSlot.captured
        assertEquals(2000.0, updated.maxWishlistAmount)
        assertEquals(40.0, updated.varialSpendPercentage)

        // untouched fields keep their original values
        assertEquals(50.0, updated.fixSpendPercentage)
        assertEquals(20.0, updated.savingPercentage)
    }

    @Test
    fun `update all fields`() {
        val repo = mockProfileRepo()
        val profileId = UUID.randomUUID()
        every { repo.get(profileId) } returns buildProfile(id = profileId)
        val useCase = UpdateProfile(repo)

        useCase.execAsync(
            UpdateProfileInput(
                id = profileId,
                maxWishlistAmount = 100.0,
                fixSpendPercentage = 40.0,
                varialSpendPercentage = 35.0,
                savingPercentage = 25.0
            )
        )

        val updatedSlot = slot<Profile>()
        verify(exactly = 1) { repo.update(capture(updatedSlot)) }

        val updated = updatedSlot.captured
        assertEquals(100.0, updated.maxWishlistAmount)
        assertEquals(40.0, updated.fixSpendPercentage)
        assertEquals(35.0, updated.varialSpendPercentage)
        assertEquals(25.0, updated.savingPercentage)
    }

    @Test
    fun `not persist when no field is provided`() {
        val repo = mockProfileRepo()
        val profileId = UUID.randomUUID()
        every { repo.get(profileId) } returns buildProfile(id = profileId)
        val useCase = UpdateProfile(repo)

        useCase.execAsync(UpdateProfileInput(id = profileId))

        verify(exactly = 0) { repo.update(any()) }
    }

    @Test
    fun `not persist when provided value equals current value`() {
        val repo = mockProfileRepo()
        val profileId = UUID.randomUUID()
        every { repo.get(profileId) } returns buildProfile(id = profileId, fixSpendPercentage = 50.0)
        val useCase = UpdateProfile(repo)

        useCase.execAsync(UpdateProfileInput(id = profileId, fixSpendPercentage = 50.0))

        verify(exactly = 0) { repo.update(any()) }
    }

    @Test
    fun `throw when profile does not exist`() {
        val repo = mockProfileRepo()
        val missingId = UUID.randomUUID()
        every { repo.get(missingId) } returns null
        val useCase = UpdateProfile(repo)

        assertThrows(DomainException.NotFound.Profile::class.java) {
            useCase.execAsync(UpdateProfileInput(id = missingId, savingPercentage = 10.0))
        }

        verify(exactly = 0) { repo.update(any()) }
    }
}
