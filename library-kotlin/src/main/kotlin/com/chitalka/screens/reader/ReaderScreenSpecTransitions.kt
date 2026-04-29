package com.chitalka.screens.reader

import kotlin.math.abs

fun ReaderScreenSpec.outgoingPageTranslateXPx(
    transitionProgress: Float,
    directionSign: Int,
    distancePx: Int,
): Float =
    -directionSign *
        ReaderScreenSpec.Transition.EDGE_TRANSLATE_FRACTION *
        distancePx *
        transitionProgress.coerceIn(0f, 1f)

fun ReaderScreenSpec.incomingPageTranslateXPx(
    transitionProgress: Float,
    directionSign: Int,
    distancePx: Int,
): Float {
    val p = transitionProgress.coerceIn(0f, 1f)
    return directionSign *
        ReaderScreenSpec.Transition.EDGE_TRANSLATE_FRACTION *
        distancePx *
        (1f - p)
}

fun ReaderScreenSpec.piecewiseLinear(
    x: Float,
    xs: FloatArray,
    ys: FloatArray,
): Float {
    require(xs.size == ys.size && xs.size >= 2)
    val t = x.coerceIn(xs.first(), xs.last())
    var i = 0
    while (i < xs.size - 1 && t > xs[i + 1]) {
        i++
    }
    val x0 = xs[i]
    val x1 = xs[i + 1]
    val y0 = ys[i]
    val y1 = ys[i + 1]
    if (abs(x1 - x0) < ReaderScreenSpec.Transition.PIECEWISE_SEGMENT_EPS) {
        return y1
    }
    val f = (t - x0) / (x1 - x0)
    return y0 + (y1 - y0) * f
}

fun ReaderScreenSpec.outgoingPageOpacity(transitionProgress: Float): Float =
    piecewiseLinear(
        transitionProgress,
        ReaderScreenSpec.Transition.ACTIVE_OPACITY_INPUTS,
        ReaderScreenSpec.Transition.ACTIVE_OPACITY_OUTPUTS,
    )

fun ReaderScreenSpec.incomingPageOpacity(transitionProgress: Float): Float =
    piecewiseLinear(
        transitionProgress,
        ReaderScreenSpec.Transition.INCOMING_OPACITY_INPUTS,
        ReaderScreenSpec.Transition.INCOMING_OPACITY_OUTPUTS,
    )

fun ReaderScreenSpec.outgoingShadeOpacity(transitionProgress: Float): Float =
    ReaderScreenSpec.Transition.OUTGOING_SHADE_END * transitionProgress.coerceIn(0f, 1f)

fun ReaderScreenSpec.incomingShadeOpacity(transitionProgress: Float): Float {
    val p = transitionProgress.coerceIn(0f, 1f)
    val lead = ReaderScreenSpec.Transition.INCOMING_INVISIBLE_UNTIL_PROGRESS
    return if (p <= lead) {
        ReaderScreenSpec.Transition.INCOMING_SHADE_PEAK * (p / lead)
    } else {
        ReaderScreenSpec.Transition.INCOMING_SHADE_PEAK *
            (1f - (p - lead) / ReaderScreenSpec.Transition.INCOMING_SHADE_FADE_WIDTH)
    }
}
