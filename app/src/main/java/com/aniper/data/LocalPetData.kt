package com.aniper.data

import com.aniper.R
import com.aniper.model.Pet
import com.aniper.model.PetAsset

/**
 * Provides built-in local pet data for testing without Firebase.
 */
object LocalPetData {

    val defaultAsset = PetAsset(
        id = "default_cat",
        name = "Orange Cat",
        idleLeftRes = R.drawable.pet_idle_left,
        idleRightRes = R.drawable.pet_idle_right,
        tapReactionRes = R.drawable.pet_tap_reaction,
        grabbedRes = R.drawable.pet_grabbed,
        fallingRes = R.drawable.pet_falling,
        landingRes = R.drawable.pet_landing,
        width = 100,
        height = 100
    )

    val samplePets = listOf(
        Pet(
            id = "pet_1",
            name = "Mochi",
            assetId = "default_cat",
            isActive = true
        ),
        Pet(
            id = "pet_2",
            name = "Biscuit",
            assetId = "default_cat",
            isActive = false
        )
    )

    fun getAssetById(assetId: String): PetAsset {
        return defaultAsset
    }
}
