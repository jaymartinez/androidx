/*
 * Copyright 2022 The Android Open Source Project
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

package androidx.emoji2.emojipicker

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.IntRange

/**
 * A group of items in RecyclerView for emoji picker body.
 * [titleItem] comes first.
 * [contentItems] comes after [titleItem].
 * [emptyPlaceholderItem] will be served after [titleItem] only if [contentItems] is empty.
 * [forceContentSize], if provided, will truncate [contentItems] to certain size or pad with
 * [PlaceholderEmoji]s.
 *
 * [categoryIconId] is the corresponding category icon in emoji picker header.
 */
internal class ItemGroup(
    @DrawableRes internal val categoryIconId: Int,
    private val titleItem: CategoryTitle,
    private val contentItems: List<EmojiViewData>,
    private val forceContentSize: Int? = null,
    private val emptyPlaceholderItem: PlaceholderText? = null
) {

    val size: Int get() = 1 /* title */ +
        (forceContentSize ?: maxOf(contentItems.size, if (emptyPlaceholderItem != null) 1 else 0))

    operator fun get(index: Int): ItemViewData {
        if (index == 0) return titleItem
        val contentIndex = index - 1
        if (contentIndex < contentItems.size) return contentItems[contentIndex]
        if (contentIndex == 0 && emptyPlaceholderItem != null) return emptyPlaceholderItem
        return PlaceholderEmoji
    }
}

/**
 * A view of concatenated list of [ItemGroup].
 */
internal class EmojiPickerItems(
    private val groups: List<ItemGroup>,
) {
    companion object {
        const val LOG_TAG = "EmojiPickerItems"
    }

    val size: Int get() = groups.sumOf { it.size }

    init {
        if (groups.isEmpty()) {
            Log.wtf(LOG_TAG, "Initialized with empty categorized sources")
        }
    }

    fun getBodyItem(@IntRange(from = 0) absolutePosition: Int): ItemViewData {
        var localPosition = absolutePosition
        for (group in groups) {
            if (localPosition < group.size) return group[localPosition]
            else localPosition -= group.size
        }
        throw IndexOutOfBoundsException()
    }

    val numGroups: Int get() = groups.size

    @DrawableRes
    fun getHeaderIconId(@IntRange(from = 0) index: Int): Int = groups[index].categoryIconId
}