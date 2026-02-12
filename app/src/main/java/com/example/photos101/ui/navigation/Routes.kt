package com.example.photos101.ui.navigation

object Routes {
    const val PHOTOS_LIST = "photos_list"
    const val PHOTO_DETAIL = "photo_detail/{photoId}/{secret}"

    fun photoDetail(photoId: String, secret: String): String =
        "photo_detail/$photoId/$secret"
}
