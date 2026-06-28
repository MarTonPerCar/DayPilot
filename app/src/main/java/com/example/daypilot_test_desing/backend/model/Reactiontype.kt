package com.example.daypilot_test_desing.backend.model

import androidx.annotation.StringRes
import com.example.daypilot_test_desing.R

enum class ReactionType(
    @StringRes val labelRes: Int,
    @StringRes val emojiRes: Int
) {
    FIRE(R.string.reaction_fire, R.string.reaction_fire_emoji),
    CLAP(R.string.reaction_clap, R.string.reaction_clap_emoji),
    STRONG(R.string.reaction_strong, R.string.reaction_strong_emoji),
    STAR(R.string.reaction_star, R.string.reaction_star_emoji)
}