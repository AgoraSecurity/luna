package com.tarmiga.luna

data class Content(val title: String, val text: String)

object NotificationContent {
    fun get(type: NotificationType, phase: PhaseType?, index: Int = 0): Content? {
        return when (type) {
            NotificationType.TIP -> getTip(phase, index)
            NotificationType.PHASE_WARNING -> getComingUp(phase)
            NotificationType.PHASE_START -> getPhaseChange(phase)
            NotificationType.PERIOD_LATE -> 
                Content("Your period is a few days late", "Cycles can vary. If it doesn't arrive soon, it may be worth checking in.")
        }
    }

    private fun getTip(phase: PhaseType?, index: Int): Content? {
        return when (phase) {
            PhaseType.MENSTRUAL -> Content("Your body is working hard right now.", "Rest is part of the process.")
            PhaseType.FOLLICULAR -> if (index == 0) {
                Content("Social energy is returning.", "A good time to reach out to people you've been meaning to see.")
            } else {
                Content("Your body recovers faster now.", "A good time to push a little harder in workouts.")
            }
            PhaseType.OVULATORY -> Content("High energy is great.", "But don't forget to rest too.")
            PhaseType.LUTEAL -> when (index) {
                0 -> Content("Wind down your to-do list.", "Your energy is better spent finishing than starting right now.")
                1 -> Content("Slower workouts feel better this phase.", "Listen to your body.")
                else -> Content("Magnesium-rich foods can help with PMS symptoms.", "Think dark chocolate, nuts, or leafy greens.")
            }
            null -> null
        }
    }

    private fun getComingUp(phase: PhaseType?): Content? {
        return when (phase) {
            PhaseType.MENSTRUAL -> Content("Follicular phase in 2 days.", "Your energy is about to pick up. Good time to plan ahead.")
            PhaseType.FOLLICULAR -> Content("Ovulatory phase in 2 days.", "Peak energy incoming. Make the most of it.")
            PhaseType.OVULATORY -> Content("Luteal phase in 2 days.", "Things may feel heavier soon. Be gentle with yourself.")
            PhaseType.LUTEAL -> Content("Your period is coming in 2 days.", "Stock up on what you need and take it easy.")
            null -> null
        }
    }

    private fun getPhaseChange(phase: PhaseType?): Content? {
        return when (phase) {
            PhaseType.FOLLICULAR -> Content("Follicular phase starts today.", "Your mind is sharper this phase. Good time to learn something new.")
            PhaseType.OVULATORY -> Content("Ovulatory phase starts today.", "Your confidence tends to peak this phase. Trust it.")
            PhaseType.LUTEAL -> Content("Luteal phase starts today.", "Cravings are normal right now. Your body needs more energy this phase.")
            else -> null
        }
    }
}
