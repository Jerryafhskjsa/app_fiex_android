package com.black.lib.stickylistheaders

import android.content.Context
import android.widget.SectionIndexer

internal class SectionIndexerAdapterWrapper(context: Context?, delegate: StickyListHeadersAdapter) : AdapterWrapper(context!!, delegate), SectionIndexer {
    var mSectionIndexerDelegate: SectionIndexer? = delegate as SectionIndexer?
    override fun getPositionForSection(section: Int): Int {
        return mSectionIndexerDelegate?.getPositionForSection(section) ?: 0
    }

    override fun getSectionForPosition(position: Int): Int {
        return mSectionIndexerDelegate?.getSectionForPosition(position) ?: 0
    }

    override fun getSections(): Array<Any> {
        return mSectionIndexerDelegate?.sections ?: arrayOf()
    }
}