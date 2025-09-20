/*
 * Copyright (C) 2020 The Android Open Source Project
 *               2021 AOSP-Krypton Project
 *               2024 Paranoid Android
 *               2025 TheParasiteProject
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.systemui.qs.tiles

import android.content.ComponentName
import android.content.Intent
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings.System.MIN_REFRESH_RATE
import android.provider.Settings.System.PEAK_REFRESH_RATE
import android.service.quicksettings.Tile
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import com.android.internal.logging.MetricsLogger
import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.android.systemui.FontStyles
import com.android.systemui.animation.Expandable
import com.android.systemui.dagger.qualifiers.Background
import com.android.systemui.dagger.qualifiers.Main
import com.android.systemui.plugins.ActivityStarter
import com.android.systemui.plugins.FalsingManager
import com.android.systemui.plugins.qs.QSTile.Icon
import com.android.systemui.plugins.qs.QSTile.State
import com.android.systemui.plugins.statusbar.StatusBarStateController
import com.android.systemui.qs.QSHost
import com.android.systemui.qs.QsEventLogger
import com.android.systemui.qs.logging.QSLogger
import com.android.systemui.qs.tileimpl.QSTileImpl
import com.android.systemui.res.R
import com.android.systemui.statusbar.policy.BatteryController
import com.android.systemui.util.settings.SystemSettings
import java.io.PrintWriter
import javax.inject.Inject
import kotlin.math.roundToInt

class RefreshRateTile
@Inject
constructor(
    host: QSHost,
    uiEventLogger: QsEventLogger,
    @Background backgroundLooper: Looper,
    @Main private val mainHandler: Handler,
    falsingManager: FalsingManager,
    metricsLogger: MetricsLogger,
    statusBarStateController: StatusBarStateController,
    activityStarter: ActivityStarter,
    qsLogger: QSLogger,
    private val systemSettings: SystemSettings,
    private val batteryController: BatteryController,
) :
    QSTileImpl<State>(
        host,
        uiEventLogger,
        backgroundLooper,
        mainHandler,
        falsingManager,
        metricsLogger,
        statusBarStateController,
        activityStarter,
        qsLogger,
    ),
    BatteryController.BatteryStateChangeCallback {

    private val settingsObserver: SettingsObserver
    private val tileLabel: String
    private val autoModeLabel: String
    private val unavailableLabel: String

    private val minRefreshRate: Int
    private val maxRefreshRate: Int
    private val refreshRates: List<Int>
    private var refreshRateMode = Mode.MIN

    private var lowPowerMode = false

    private val iconMin: Icon
    private val iconMax: Icon

    init {
        with(mContext.resources) {
            tileLabel = getString(R.string.quick_settings_refresh_rate_label)
            autoModeLabel = getString(R.string.quick_settings_refresh_rate_auto_mode_label)
            unavailableLabel = getString(R.string.tile_unavailable)
        }

        refreshRates = getRefreshRates()
        minRefreshRate = refreshRates.minOrNull() ?: DEFAULT_MIN_REFRESH_RATE
        maxRefreshRate = refreshRates.maxOrNull() ?: DEFAULT_REFRESH_RATE
        settingsObserver = SettingsObserver()

        iconMin = createRefreshIcon(minRefreshRate)
        iconMax = createRefreshIcon(maxRefreshRate)

        batteryController.addCallback(this)
    }

    override fun newTileState() =
        State().also {
            it.icon = icon
            it.state = Tile.STATE_ACTIVE
        }

    override fun onPowerSaveChanged(isPowerSave: Boolean) {
        lowPowerMode = isPowerSave
        refreshState()
    }

    override fun getLongClickIntent() = displaySettingsIntent

    override fun isAvailable(): Boolean = isHighRefreshRateAvailable()

    override fun getTileLabel(): CharSequence = tileLabel

    override protected fun handleInitialize() {
        logD("handleInitialize")
        updateMode()
        settingsObserver.observe()
    }

    override protected fun handleClick(expandable: Expandable?) {
        if (state.state == Tile.STATE_UNAVAILABLE) {
            return
        }
        logD("handleClick")
        refreshRateMode = getNextMode(refreshRateMode)
        logD("refreshRateMode = $refreshRateMode")
        updateRefreshRateForMode(refreshRateMode)
        refreshState()
    }

    override protected fun handleLongClick(expandable: Expandable?) {
        if (state.state == Tile.STATE_UNAVAILABLE) {
            return
        }
        super.handleLongClick(expandable)
    }

    override protected fun handleUpdateState(state: State, arg: Any?) {
        if (state.label == null) {
            state.label = tileLabel
            state.contentDescription = tileLabel
        }
        logD("handleUpdateState, state = $state")
        if (lowPowerMode) {
            state.state = Tile.STATE_UNAVAILABLE
            state.icon = icon
            state.secondaryLabel = unavailableLabel
        } else {
            state.state = Tile.STATE_ACTIVE
            state.icon = getIconForMode(refreshRateMode)
            state.secondaryLabel = getTitleForMode(refreshRateMode)
        }
        logD("secondaryLabel = ${state.secondaryLabel}")
    }

    override fun getMetricsCategory(): Int = MetricsEvent.VIEW_UNKNOWN

    override fun destroy() {
        batteryController.removeCallback(this)
        settingsObserver.unobserve()
        super.destroy()
    }

    override fun dump(pw: PrintWriter, args: Array<out String>) {}

    private fun getRefreshRates(): List<Int> {
        return mContext.display
            ?.supportedModes
            ?.map { it.refreshRate.roundToInt() }
            ?.sorted()
            ?.distinct()
            .orEmpty()
    }

    private fun isHighRefreshRateAvailable(): Boolean =
        refreshRates.any { it > DEFAULT_REFRESH_RATE }

    private fun roundToNearestRefreshRate(refreshRate: Int, floor: Boolean): Int {
        if (refreshRates.contains(refreshRate)) return refreshRate
        return if (floor) {
            refreshRates.findLast { it < refreshRate } ?: minRefreshRate
        } else {
            refreshRates.find { it > refreshRate } ?: maxRefreshRate
        }
    }

    private fun getDefaultPeakRefreshRate(): Float {
        return mContext.resources
            .getInteger(com.android.internal.R.integer.config_defaultPeakRefreshRate)
            .toFloat()
    }

    private fun getPeakRefreshRate(): Int {
        val peakRefreshRate =
            systemSettings.getFloat(PEAK_REFRESH_RATE, getDefaultPeakRefreshRate()).roundToInt()
        return if (peakRefreshRate < minRefreshRate) {
            maxRefreshRate
        } else {
            roundToNearestRefreshRate(peakRefreshRate, true)
        }
    }

    private fun setPeakRefreshRate(refreshRate: Int) {
        systemSettings.putFloat(PEAK_REFRESH_RATE, refreshRate.toFloat())
    }

    private fun getMinRefreshRate(): Int {
        val minRefreshRate =
            systemSettings
                .getFloat(MIN_REFRESH_RATE, DEFAULT_MIN_REFRESH_RATE.toFloat())
                .roundToInt()
        return if (minRefreshRate == DEFAULT_MIN_REFRESH_RATE) {
            DEFAULT_MIN_REFRESH_RATE
        } else {
            roundToNearestRefreshRate(minRefreshRate, false)
        }
    }

    private fun setMinRefreshRate(refreshRate: Int) {
        systemSettings.putFloat(MIN_REFRESH_RATE, refreshRate.toFloat())
    }

    private fun updateMode() {
        val minRate = getMinRefreshRate()
        val maxRate = getPeakRefreshRate()
        logD("minRate = $minRate, maxRate = $maxRate")

        if (minRate == maxRate) {
            if (minRate > DEFAULT_REFRESH_RATE) refreshRateMode = Mode.MAX
            else refreshRateMode = Mode.MIN
        } else {
            refreshRateMode = Mode.AUTO
        }
        logD("refreshRateMode = $refreshRateMode")
    }

    private fun getNextMode(mode: Mode) =
        when (mode) {
            Mode.AUTO -> Mode.MIN
            Mode.MIN -> Mode.MAX
            Mode.MAX -> Mode.AUTO
        }

    private fun updateRefreshRateForMode(mode: Mode) {
        logD("updateRefreshRateForMode, mode = $mode")
        when (mode) {
            Mode.AUTO -> {
                setPeakRefreshRate(maxRefreshRate)
                setMinRefreshRate(DEFAULT_MIN_REFRESH_RATE)
            }
            Mode.MAX -> {
                setPeakRefreshRate(maxRefreshRate)
                setMinRefreshRate(maxRefreshRate)
            }
            Mode.MIN -> {
                setPeakRefreshRate(minRefreshRate)
                setMinRefreshRate(minRefreshRate)
            }
        }
    }

    private fun getTitleForMode(mode: Mode) =
        when (mode) {
            Mode.AUTO -> autoModeLabel
            Mode.MAX -> maxRefreshRate.toString() + "Hz"
            Mode.MIN -> minRefreshRate.toString() + "Hz"
        }

    private fun getIconForMode(mode: Mode) =
        when (mode) {
            Mode.AUTO -> iconAuto
            Mode.MAX -> iconMax
            Mode.MIN -> iconMin
        }

    private fun createRefreshIcon(number: Int): Icon {
        val baseDrawable =
            ContextCompat.getDrawable(mContext, R.drawable.ic_qs_refresh_rate_select) ?: return icon

        val width = baseDrawable.intrinsicWidth
        val height = baseDrawable.intrinsicHeight

        val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        baseDrawable.setBounds(0, 0, width, height)
        baseDrawable.draw(canvas)

        val textPaint =
            Paint().apply {
                color = Color.WHITE
                textSize = width * 0.50f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                this.typeface =
                    Typeface.create(FontStyles.GSF_TITLE_SMALL_EMPHASIZED, Typeface.NORMAL)
            }

        val textToDraw = number.toString()
        val xPos = canvas.width / 2f
        val yPos = (canvas.height * 0.45f) - ((textPaint.descent() + textPaint.ascent()) / 2)

        canvas.drawText(textToDraw, xPos, yPos, textPaint)

        return DrawableIcon(bitmap.toDrawable(mContext.resources))
    }

    private enum class Mode {
        MIN,
        MAX,
        AUTO,
    }

    private inner class SettingsObserver : ContentObserver(mainHandler) {
        private var isObserving = false

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            if (!selfChange) updateMode()
        }

        fun observe() {
            if (isObserving) return
            isObserving = true
            systemSettings.registerContentObserverSync(MIN_REFRESH_RATE, this)
            systemSettings.registerContentObserverSync(PEAK_REFRESH_RATE, this)
        }

        fun unobserve() {
            if (!isObserving) return
            isObserving = false
            systemSettings.unregisterContentObserverSync(this)
        }
    }

    companion object {
        const val TILE_SPEC = "refresh_rate"
        private const val TAG = "RefreshRateTile"
        private const val DEBUG = false

        private const val DEFAULT_REFRESH_RATE = 60
        private const val DEFAULT_MIN_REFRESH_RATE = 0
        private val icon: Icon = ResourceIcon.get(R.drawable.ic_qs_refresh_rate)
        private val iconAuto: Icon = ResourceIcon.get(R.drawable.ic_qs_refresh_rate_auto)
        private val displaySettingsIntent =
            Intent()
                .setComponent(
                    ComponentName(
                        "com.android.settings",
                        "com.android.settings.Settings\$RefreshRateSettingsActivity",
                    )
                )

        private fun logD(msg: String) {
            if (DEBUG) Log.d(TAG, msg)
        }
    }
}
