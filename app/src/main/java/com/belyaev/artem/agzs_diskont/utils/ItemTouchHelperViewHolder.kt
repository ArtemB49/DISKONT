package com.belyaev.artem.agzs_diskont.utils

import android.view.View

interface ItemTouchHelperViewHolder {
    fun onItemSelected(view: View)
    fun onItemClear(view: View)
}