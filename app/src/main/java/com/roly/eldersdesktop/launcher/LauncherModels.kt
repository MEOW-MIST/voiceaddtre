package com.roly.eldersdesktop.launcher

import java.util.UUID

enum class LauncherCardType(
    val storageValue: String
) {
    CLOCK(storageValue = "clock"),
    PHONE(storageValue = "phone"),
    CONTACTS(storageValue = "contacts"),
    CAMERA(storageValue = "camera"),
    APP(storageValue = "app");

    companion object {
        fun fromStorage(value: String): LauncherCardType? {
            return entries.firstOrNull { it.storageValue == value }
        }
    }
}

data class LauncherCard(
    val id: String = UUID.randomUUID().toString(),
    val type: LauncherCardType,
    val packageName: String? = null
)

data class InstalledApp(
    val packageName: String,
    val label: String
)

data class BuiltInCardTemplate(
    val type: LauncherCardType,
    val title: String,
    val subtitle: String
)

val BuiltInCardTemplates = listOf(
    BuiltInCardTemplate(
        type = LauncherCardType.CLOCK,
        title = "大时钟",
        subtitle = "一眼就能看到时间和日期"
    ),
    BuiltInCardTemplate(
        type = LauncherCardType.PHONE,
        title = "拨号电话",
        subtitle = "点一下就进入拨号界面"
    ),
    BuiltInCardTemplate(
        type = LauncherCardType.CONTACTS,
        title = "联系人",
        subtitle = "快速进入通讯录"
    ),
    BuiltInCardTemplate(
        type = LauncherCardType.CAMERA,
        title = "相机",
        subtitle = "快速打开拍照"
    )
)
