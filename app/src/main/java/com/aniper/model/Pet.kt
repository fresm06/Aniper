package com.aniper.model

data class Pet(
    val id: String = "",
    val name: String = "",
    val assetId: String = "",
    var positionX: Float = 0f,
    var positionY: Float = 0f,
    var state: PetState = PetState.IDLE,
    val isActive: Boolean = true
)

enum class PetState {
    IDLE,
    WALKING_LEFT,
    WALKING_RIGHT,
    TAP_REACTION,
    GRABBED,
    FALLING,
    LANDING
}
