package com.aniper.overlay

/**
 * Simple physics engine for pet falling and bouncing behavior.
 */
class PhysicsEngine {

    companion object {
        const val GRAVITY = 980f          // pixels/s^2
        const val BOUNCE_DAMPING = 0.2f   // velocity retained after bounce (reduced)
        const val BOUNCE_THRESHOLD = 200f // minimum velocity to trigger bounce (increased)
        const val WALK_SPEED = 60f        // pixels/s for idle walking
        const val DIRECTION_CHANGE_INTERVAL = 3000L // ms between direction changes
    }

    private var velocityX = 0f
    private var velocityY = 0f
    private var isAirborne = false
    private var bounceCount = 0
    private val maxBounces = 0  // No bounces, land immediately

    fun reset() {
        velocityX = 0f
        velocityY = 0f
        isAirborne = false
        bounceCount = 0
    }

    fun startFalling(initialVelocityX: Float = 0f, initialVelocityY: Float = 0f) {
        velocityX = initialVelocityX
        velocityY = initialVelocityY
        isAirborne = true
        bounceCount = 0
    }

    /**
     * Updates position based on physics simulation.
     * @param deltaTime time elapsed in seconds
     * @param currentX current X position
     * @param currentY current Y position
     * @param groundY the Y coordinate of the ground (screen bottom - pet height)
     * @param screenWidth screen width for X bounds
     * @return Triple of (newX, newY, hasLanded) where hasLanded indicates the pet reached the ground
     */
    fun update(
        deltaTime: Float,
        currentX: Float,
        currentY: Float,
        groundY: Float,
        screenWidth: Int
    ): PhysicsResult {
        if (!isAirborne) {
            return PhysicsResult(currentX, currentY, false, false)
        }

        // Apply gravity
        velocityY += GRAVITY * deltaTime

        // Update position
        var newX = currentX + velocityX * deltaTime
        var newY = currentY + velocityY * deltaTime

        // Horizontal bounds
        newX = newX.coerceIn(0f, screenWidth.toFloat())
        if (newX <= 0f || newX >= screenWidth.toFloat()) {
            velocityX = -velocityX * BOUNCE_DAMPING
        }

        // Ground collision
        var bounced = false
        if (newY >= groundY) {
            newY = groundY
            if (velocityY > BOUNCE_THRESHOLD && bounceCount < maxBounces) {
                velocityY = -velocityY * BOUNCE_DAMPING
                velocityX *= BOUNCE_DAMPING
                bounceCount++
                bounced = true
            } else {
                // Landed
                velocityY = 0f
                velocityX = 0f
                isAirborne = false
                return PhysicsResult(newX, newY, true, false)
            }
        }

        return PhysicsResult(newX, newY, false, bounced)
    }

    fun isInAir(): Boolean = isAirborne

    data class PhysicsResult(
        val x: Float,
        val y: Float,
        val landed: Boolean,
        val bounced: Boolean
    )
}
