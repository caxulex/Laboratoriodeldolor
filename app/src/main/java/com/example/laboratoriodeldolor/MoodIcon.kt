package com.example.laboratoriodeldolor

sealed class MoodIcon {
    data class Emoji(val emoji: String) : MoodIcon()
    data class DrawableRes(val resId: Int) : MoodIcon()
}
