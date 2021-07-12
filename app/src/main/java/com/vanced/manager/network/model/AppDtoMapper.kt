package com.vanced.manager.network.model

import com.vanced.manager.R
import com.vanced.manager.domain.datasource.PackageInformationDataSource
import com.vanced.manager.domain.model.App
import com.vanced.manager.domain.model.AppStatus
import com.vanced.manager.domain.util.EntityMapper
import com.vanced.manager.network.util.MUSIC_NAME
import com.vanced.manager.network.util.VANCED_NAME
import com.vanced.manager.ui.preferences.CheckboxPreference
import com.vanced.manager.ui.preferences.RadioButtonPreference
import com.vanced.manager.ui.preferences.holder.musicVersionPref
import com.vanced.manager.ui.preferences.holder.vancedLanguagesPref
import com.vanced.manager.ui.preferences.holder.vancedThemePref
import com.vanced.manager.ui.preferences.holder.vancedVersionPref
import com.vanced.manager.ui.widgets.home.installation.CheckboxInstallationOption
import com.vanced.manager.ui.widgets.home.installation.InstallationOption
import com.vanced.manager.ui.widgets.home.installation.RadiobuttonInstallationOption
import java.util.*

class AppDtoMapper(
    private val packageInformationDataSource: PackageInformationDataSource
) : EntityMapper<AppDto, App> {

    override suspend fun mapToModel(entity: AppDto): App =
        with (entity) {
            val localVersionCode = packageInformationDataSource.getVersionCode(packageName ?: "")
            val localVersionCodeRoot = packageInformationDataSource.getVersionCode(packageNameRoot ?: "")
            val localVersionName = packageInformationDataSource.getVersionName(packageName ?: "")
            val localVersionNameRoot = packageInformationDataSource.getVersionName(packageNameRoot ?: "")
            App(
                name = name,
                remoteVersion = version,
                remoteVersionCode = versionCode,
                installedVersion = localVersionName,
                installedVersionCode = localVersionCode,
                installedVersionRoot = localVersionNameRoot,
                installedVersionCodeRoot = localVersionCodeRoot,
                appStatus = compareVersionCodes(versionCode, localVersionCode),
                appStatusRoot = compareVersionCodes(versionCode, localVersionCodeRoot),
                packageName = packageName,
                packageNameRoot = packageNameRoot,
                iconUrl = iconUrl,
                changelog = changelog,
                url = url,
                versions = versions,
                themes = themes,
                languages = languages,
                installationOptions = getInstallationOptions(entity)
            )
        }

    private fun compareVersionCodes(remote: Int?, local: Int?): AppStatus =
        if (local != null && remote != null) {
            when {
                remote > local -> AppStatus.Update
                remote <= local -> AppStatus.Reinstall
                else -> AppStatus.Install
            }
        } else {
            AppStatus.Install
        }

    private fun getInstallationOptions(app: AppDto): List<InstallationOption>? =
        when (app.name) {
            VANCED_NAME -> listOf(
                RadiobuttonInstallationOption(
                    titleId = R.string.app_installation_options_theme,
                    preference = vancedThemePref,
                    buttons = app.themes?.map { version ->
                        RadioButtonPreference(
                            title = version.replaceFirstChar { it.titlecase(Locale.getDefault()) },
                            key = version
                        )
                    } ?: emptyList()
                ),
                RadiobuttonInstallationOption(
                    titleId = R.string.app_installation_options_version,
                    preference = vancedVersionPref,
                    buttons = app.versions?.map {
                        RadioButtonPreference(
                            title = it,
                            key = it
                        )
                    } ?: emptyList()
                ),
                CheckboxInstallationOption(
                    titleId = R.string.app_installation_options_language,
                    preference = vancedLanguagesPref,
                    buttons = app.languages?.map {
                        CheckboxPreference(
                            title = Locale(it).displayName,
                            key = it
                        )
                    } ?: emptyList()
                ),
            )
            MUSIC_NAME -> listOf(
                RadiobuttonInstallationOption(
                    titleId = R.string.app_installation_options_version,
                    preference = musicVersionPref,
                    buttons = app.versions?.map {
                        RadioButtonPreference(
                            title = it,
                            key = it
                        )
                    } ?: emptyList()
                ),
            )
            else -> null
        }
}