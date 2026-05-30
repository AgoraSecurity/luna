package com.tarmiga.luna

data class Content(val title: String, val text: String)

object NotificationContent {
    fun get(type: NotificationType, phase: PhaseType?, tipIndex: Int): Content? {
        return when (type) {
            NotificationType.PHASE_WARNING -> getPhaseWarning(phase)
            NotificationType.PHASE_START -> getPhaseStart(phase)
            NotificationType.PERIOD_LATE -> 
                Content("Your period is a few days late", "Cycles can vary. If it doesn't arrive soon, it may be worth checking in.")
            NotificationType.DAILY_REMINDER -> getDailyTip(phase, tipIndex)
        }
    }

    private fun getPhaseWarning(phase: PhaseType?): Content? {
        return when (phase) {
            PhaseType.MENSTRUAL -> Content("Luna", "Your period is coming in 2 days. Stock up on what you need and take it easy.")
            PhaseType.FOLLICULAR -> Content("Luna", "Follicular phase in 2 days. Your energy is about to pick up. Good time to plan ahead.")
            PhaseType.OVULATORY -> Content("Luna", "Ovulatory phase in 2 days. Peak energy incoming. Make the most of it.")
            PhaseType.LUTEAL -> Content("Luna", "Luteal phase in 2 days. Your energy may start to dip. Wind down your schedule.")
            null -> null
        }
    }

    private fun getPhaseStart(phase: PhaseType?): Content? {
        return when (phase) {
            PhaseType.FOLLICULAR -> Content("Follicular phase starts today", "Your energy is returning. A good day to start something new.")
            PhaseType.OVULATORY -> Content("Ovulatory phase starts today", "You're at your peak. Great day for big conversations or challenges.")
            PhaseType.LUTEAL -> Content("Luteal phase starts today", "Things may feel heavier soon. Be gentle with yourself.")
            else -> null
        }
    }

    private fun getDailyTip(phase: PhaseType?, index: Int): Content? {
        val idx = index.coerceIn(0, 4)
        return when (phase) {
            PhaseType.MENSTRUAL -> listOf(
                Content("Your body is working hard right now.", "Rest is part of the process."),
                Content("Heat can help with cramps.", "A warm compress or bath can make a difference."),
                Content("Low energy is normal right now.", "It's not a sign of weakness."),
                Content("Iron-rich foods can help during your period.", "Think lentils, spinach, or red meat."),
                Content("Gentle movement can ease cramps.", "A short walk or some stretching can help.")
            )[idx]
            PhaseType.FOLLICULAR -> listOf(
                Content("Energy is picking up.", "A good time to tackle things you've been putting off."),
                Content("Your mind is sharper this phase.", "Good time to learn something new."),
                Content("Social energy is returning.", "A good time to reach out to people you've been meaning to see."),
                Content("Your body recovers faster now.", "A good time to push a little harder in workouts."),
                Content("You may notice your mood lifting.", "That's your body finding its rhythm again.")
            )[idx]
            PhaseType.OVULATORY -> listOf(
                Content("You're at your most energetic right now.", "Good time to take on something you've been avoiding."),
                Content("This is a great time for difficult conversations.", "You're at your most articulate."),
                Content("Your confidence tends to peak this phase.", "Trust it."),
                Content("High energy is great.", "But don't forget to rest too."),
                Content("Your skin tends to look its best right now.", "Enjoy it.")
            )[idx]
            PhaseType.LUTEAL -> listOf(
                Content("Cravings are normal right now.", "Your body needs more energy this phase."),
                Content("If you're feeling irritable, it's hormonal.", "Be patient with yourself."),
                Content("Slower workouts feel better this phase.", "Listen to your body."),
                Content("Wind down your to-do list.", "Your energy is better spent finishing than starting right now."),
                Content("Magnesium-rich foods can help with PMS symptoms.", "Think dark chocolate, nuts, or leafy greens.")
            )[idx]
            null -> Content("Luna", "Time to log your day. How are you feeling?")
        }
    }
}
