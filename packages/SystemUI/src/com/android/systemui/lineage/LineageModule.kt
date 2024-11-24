/*
 * Copyright (C) 2023 The LineageOS Project
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
 * limitations under the License.
 */

package com.android.systemui.lineage

import com.android.systemui.qs.QsEventLogger
import com.android.systemui.qs.pipeline.shared.TileSpec
import com.android.systemui.qs.shared.model.TileCategory
import com.android.systemui.qs.tileimpl.QSTileImpl
import com.android.systemui.qs.tiles.AmbientDisplayTile
import com.android.systemui.qs.tiles.AODTile
import com.android.systemui.qs.tiles.CaffeineTile
import com.android.systemui.qs.tiles.HeadsUpTile
import com.android.systemui.qs.tiles.NfcTile
import com.android.systemui.qs.tiles.PowerShareTile
import com.android.systemui.qs.tiles.ProfilesTile
import com.android.systemui.qs.tiles.ReadingModeTile
import com.android.systemui.qs.tiles.RefreshRateTile
import com.android.systemui.qs.tiles.SyncTile
import com.android.systemui.qs.tiles.UsbTetherTile
import com.android.systemui.qs.tiles.VpnTile
import com.android.systemui.qs.tiles.base.shared.model.QSTileConfig
import com.android.systemui.qs.tiles.base.shared.model.QSTileUIConfig
import com.android.systemui.res.R

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
interface LineageModule {
    /** Inject AmbientDisplayTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(AmbientDisplayTile.TILE_SPEC)
    fun bindAmbientDisplayTile(ambientDisplayTile: AmbientDisplayTile): QSTileImpl<*>

    /** Inject AODTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(AODTile.TILE_SPEC)
    fun bindAODTile(aodTile: AODTile): QSTileImpl<*>

    /** Inject CaffeineTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(CaffeineTile.TILE_SPEC)
    fun bindCaffeineTile(caffeineTile: CaffeineTile): QSTileImpl<*>

    /** Inject HeadsUpTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(HeadsUpTile.TILE_SPEC)
    fun bindHeadsUpTile(headsUpTile: HeadsUpTile): QSTileImpl<*>

    /** Inject PowerShareTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(PowerShareTile.TILE_SPEC)
    fun bindPowerShareTile(powerShareTile: PowerShareTile): QSTileImpl<*>

    /** Inject ProfilesTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(ProfilesTile.TILE_SPEC)
    fun bindProfilesTile(profilesTile: ProfilesTile): QSTileImpl<*>

    /** Inject ReadingModeTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(ReadingModeTile.TILE_SPEC)
    fun bindReadingModeTile(readingModeTile: ReadingModeTile): QSTileImpl<*>

    /** Inject RefreshRateTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(RefreshRateTile.TILE_SPEC)
    fun bindRefreshRateTile(refreshRateTile: RefreshRateTile): QSTileImpl<*>

    /** Inject SyncTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(SyncTile.TILE_SPEC)
    fun bindSyncTile(syncTile: SyncTile): QSTileImpl<*>

    /** Inject UsbTetherTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(UsbTetherTile.TILE_SPEC)
    fun bindUsbTetherTile(usbTetherTile: UsbTetherTile): QSTileImpl<*>

    /** Inject VpnTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(VpnTile.TILE_SPEC)
    fun bindVpnTile(vpnTile: VpnTile): QSTileImpl<*>

    companion object {
        @Provides
        @IntoMap
        @StringKey(NfcTile.TILE_SPEC)
        fun provideNfcConfig(uiEventLogger: QsEventLogger): QSTileConfig {
            return QSTileConfig(
                tileSpec = TileSpec.create(NfcTile.TILE_SPEC),
                uiConfig = QSTileUIConfig.Resource(
                    iconRes = R.drawable.ic_qs_nfc,
                    labelRes = R.string.quick_settings_nfc_label
                ),
                instanceId = uiEventLogger.getNewInstanceId(),
                category = TileCategory.CONNECTIVITY
            )
        }

        @Provides
        @IntoMap
        @StringKey(AmbientDisplayTile.TILE_SPEC)
        fun provideAmbientDisplayConfig(uiEventLogger: QsEventLogger): QSTileConfig {
            return QSTileConfig(
                tileSpec = TileSpec.create(AmbientDisplayTile.TILE_SPEC),
                uiConfig = QSTileUIConfig.Resource(
                    iconRes = R.drawable.ic_qs_ambient_display,
                    labelRes = R.string.quick_settings_ambient_display_label
                ),
                instanceId = uiEventLogger.getNewInstanceId(),
                category = TileCategory.DISPLAY
            )
        }

        @Provides
        @IntoMap
        @StringKey(AODTile.TILE_SPEC)
        fun provideAODConfig(uiEventLogger: QsEventLogger): QSTileConfig {
            return QSTileConfig(
                tileSpec = TileSpec.create(AODTile.TILE_SPEC),
                uiConfig = QSTileUIConfig.Resource(
                    iconRes = R.drawable.ic_qs_aod,
                    labelRes = R.string.quick_settings_aod_label
                ),
                instanceId = uiEventLogger.getNewInstanceId(),
                category = TileCategory.DISPLAY
            )
        }

        @Provides
        @IntoMap
        @StringKey(CaffeineTile.TILE_SPEC)
        fun provideCaffeineConfig(uiEventLogger: QsEventLogger): QSTileConfig {
            return QSTileConfig(
                tileSpec = TileSpec.create(CaffeineTile.TILE_SPEC),
                uiConfig = QSTileUIConfig.Resource(
                    iconRes = R.drawable.ic_qs_caffeine,
                    labelRes = R.string.quick_settings_caffeine_label
                ),
                instanceId = uiEventLogger.getNewInstanceId(),
                category = TileCategory.DISPLAY
            )
        }

        @Provides
        @IntoMap
        @StringKey(HeadsUpTile.TILE_SPEC)
        fun provideHeadsUpConfig(uiEventLogger: QsEventLogger): QSTileConfig {
            return QSTileConfig(
                tileSpec = TileSpec.create(HeadsUpTile.TILE_SPEC),
                uiConfig = QSTileUIConfig.Resource(
                    iconRes = R.drawable.ic_qs_heads_up,
                    labelRes = R.string.quick_settings_heads_up_label
                ),
                instanceId = uiEventLogger.getNewInstanceId(),
                category = TileCategory.DISPLAY
            )
        }

        @Provides
        @IntoMap
        @StringKey(PowerShareTile.TILE_SPEC)
        fun providePowerShareConfig(uiEventLogger: QsEventLogger): QSTileConfig {
            return QSTileConfig(
                tileSpec = TileSpec.create(PowerShareTile.TILE_SPEC),
                uiConfig = QSTileUIConfig.Resource(
                    iconRes = R.drawable.ic_qs_powershare,
                    labelRes = R.string.quick_settings_powershare_label
                ),
                instanceId = uiEventLogger.getNewInstanceId(),
                category = TileCategory.UTILITIES
            )
        }

        @Provides
        @IntoMap
        @StringKey(ProfilesTile.TILE_SPEC)
        fun provideProfilesConfig(uiEventLogger: QsEventLogger): QSTileConfig {
            return QSTileConfig(
                tileSpec = TileSpec.create(ProfilesTile.TILE_SPEC),
                uiConfig = QSTileUIConfig.Resource(
                    iconRes = R.drawable.ic_qs_profiles,
                    labelRes = R.string.quick_settings_profiles_label
                ),
                instanceId = uiEventLogger.getNewInstanceId(),
                category = TileCategory.UTILITIES
            )
        }

        @Provides
        @IntoMap
        @StringKey(ReadingModeTile.TILE_SPEC)
        fun provideReadingModeConfig(uiEventLogger: QsEventLogger): QSTileConfig {
            return QSTileConfig(
                tileSpec = TileSpec.create(ReadingModeTile.TILE_SPEC),
                uiConfig = QSTileUIConfig.Resource(
                    iconRes = R.drawable.ic_qs_reader,
                    labelRes = R.string.quick_settings_reading_mode
                ),
                instanceId = uiEventLogger.getNewInstanceId(),
                category = TileCategory.DISPLAY
            )
        }

        @Provides
        @IntoMap
        @StringKey(SyncTile.TILE_SPEC)
        fun provideSyncConfig(uiEventLogger: QsEventLogger): QSTileConfig {
            return QSTileConfig(
                tileSpec = TileSpec.create(SyncTile.TILE_SPEC),
                uiConfig = QSTileUIConfig.Resource(
                    iconRes = R.drawable.ic_qs_sync,
                    labelRes = R.string.quick_settings_sync_label
                ),
                instanceId = uiEventLogger.getNewInstanceId(),
                category = TileCategory.CONNECTIVITY
            )
        }

        @Provides
        @IntoMap
        @StringKey(UsbTetherTile.TILE_SPEC)
        fun provideUsbTetherConfig(uiEventLogger: QsEventLogger): QSTileConfig {
            return QSTileConfig(
                tileSpec = TileSpec.create(UsbTetherTile.TILE_SPEC),
                uiConfig = QSTileUIConfig.Resource(
                    iconRes = R.drawable.ic_qs_usb_tether,
                    labelRes = R.string.quick_settings_usb_tether_label
                ),
                instanceId = uiEventLogger.getNewInstanceId(),
                category = TileCategory.UTILITIES
            )
        }

        @Provides
        @IntoMap
        @StringKey(VpnTile.TILE_SPEC)
        fun provideVpnConfig(uiEventLogger: QsEventLogger): QSTileConfig {
            return QSTileConfig(
                tileSpec = TileSpec.create(VpnTile.TILE_SPEC),
                uiConfig = QSTileUIConfig.Resource(
                    iconRes = R.drawable.ic_qs_vpn,
                    labelRes = R.string.quick_settings_vpn_label
                ),
                instanceId = uiEventLogger.getNewInstanceId(),
                category = TileCategory.CONNECTIVITY
            )
        }

        @Provides
        @IntoMap
        @StringKey(RefreshRateTile.TILE_SPEC)
        fun provideRefreshRateTile(uiEventLogger: QsEventLogger): QSTileConfig {
            return QSTileConfig(
                tileSpec = TileSpec.create(RefreshRateTile.TILE_SPEC),
                uiConfig = QSTileUIConfig.Resource(
                    iconRes = R.drawable.ic_refresh_rate,
                    labelRes = R.string.refresh_rate_tile_label
                ),
                instanceId = uiEventLogger.getNewInstanceId(),
                category = TileCategory.DISPLAY
            )
        }
    }
}
