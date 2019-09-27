package by.green.simplemail

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper

class EmailsItemTouchHelper(
    dragDirs: Int,
    swipeDirs: Int,
    val listener: SwipeListsener,
    val context: Context?
) :
    ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {


    interface SwipeListsener {
        fun onItemSwiped(adapterIndex: Int)
    }



    override fun onChildDraw(
        c: Canvas,
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val background = ColorDrawable(Color.RED)
            val icon = ContextCompat.getDrawable(context ?: return, R.drawable.trash)
            val w = (viewHolder.itemView.bottom) - (viewHolder.itemView.top)

            if (dX > 0) {
                val mh = (viewHolder.itemView.bottom - viewHolder.itemView.top) / 2 - 25
                val mw = 10
                icon?.setBounds(
                    mw, viewHolder.itemView.top + mh,
                    (mw + 1.3 * icon.intrinsicWidth).toInt(),
                    (viewHolder.itemView.top + mh + 1.3 * icon.intrinsicHeight).toInt()
                )

                background.setBounds(
                    0, viewHolder.itemView.top,
                    viewHolder.itemView.left + dX.toInt(),
                    viewHolder.itemView.bottom
                )

            } else if (dX < 0) {
                return
                val dx = dX.toInt()
                var left = viewHolder.itemView.right
                left += dx
                val right = viewHolder.itemView.right
                /*icon?.setBounds(
                    right - w  //dX.toInt()
                    , viewHolder.itemView.top,
                    right,
                    viewHolder.itemView.bottom
                )*/
                background.setBounds(
                    left  //dX.toInt()
                    , viewHolder.itemView.top,
                    right,
                    viewHolder.itemView.bottom
                )
            }
            background.draw(c)
            if (Math.abs(dX.toInt()) > w)
                icon?.draw(c)
        }
    }


    override fun onMove(
        p0: androidx.recyclerview.widget.RecyclerView,
        p1: androidx.recyclerview.widget.RecyclerView.ViewHolder,
        p2: androidx.recyclerview.widget.RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
        if (//(direction == ItemTouchHelper.LEFT)  ||
            (direction == ItemTouchHelper.RIGHT)) {
            val id = viewHolder.adapterPosition ?: return
            listener.onItemSwiped(id)
        }
    }
}