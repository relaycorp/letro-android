package tech.relaycorp.letro.ui.common.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

fun AnimatedContentTransitionScope<Int>.swipeAnimation(): ContentTransform {
    // Compare the incoming number with the previous number.
    return if (targetState > initialState) {
        // If the target number is larger, it slides up and fades in
        // while the initial (smaller) number slides up and fades out.
        slideInHorizontally { height -> height } + fadeIn() togetherWith
            slideOutHorizontally { height -> -height } + fadeOut()
    } else {
        // If the target number is smaller, it slides down and fades in
        // while the initial number slides down and fades out.
        slideInHorizontally { height -> -height } + fadeIn() togetherWith
            slideOutHorizontally { height -> height } + fadeOut()
    }.using(
        // Disable clipping since the faded slide-in/out should
        // be displayed out of bounds.
        SizeTransform(clip = false),
    )
}
