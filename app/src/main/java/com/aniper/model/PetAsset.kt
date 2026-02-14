package com.aniper.model

/**
 * Holds drawable resource IDs for each pet animation state.
 */
data class PetAsset(
    val id: String = "",
    val name: String = "",
    val idleLeftRes: Int = 0,
    val idleRightRes: Int = 0,
    val tapReactionRes: Int = 0,
    val grabbedRes: Int = 0,
    val fallingRes: Int = 0,
    val landingRes: Int = 0,
    val width: Int = 100,
    val height: Int = 100
) {
    fun getResForState(state: PetState): Int {
        return when (state) {
            PetState.IDLE, PetState.WALKING_RIGHT -> idleRightRes
            PetState.WALKING_LEFT -> idleLeftRes
            PetState.TAP_REACTION -> tapReactionRes
            PetState.GRABBED -> grabbedRes
            PetState.FALLING -> fallingRes
            PetState.LANDING -> landingRes
        }
    }
}
