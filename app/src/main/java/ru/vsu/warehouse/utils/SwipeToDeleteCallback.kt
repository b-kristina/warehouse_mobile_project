package ru.vsu.warehouse.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import ru.vsu.warehouse.R
import kotlin.math.absoluteValue

class SwipeToDeleteCallback(
    private val context: Context,
    private val onDelete: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val deleteIcon: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_delete)!!
    private val intrinsicWidth = deleteIcon.intrinsicWidth
    private val intrinsicHeight = deleteIcon.intrinsicHeight

    private val background: GradientDrawable = GradientDrawable().apply {
        setColor(ContextCompat.getColor(context, R.color.swipe_delete_background))
        cornerRadius = 12f * context.resources.displayMetrics.density // 12dp
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.5f

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        if (position != RecyclerView.NO_POSITION) {
            onDelete(position)
            // Сбрасываем свайп, чтобы фон исчез
            viewHolder.bindingAdapter?.notifyItemChanged(position)
        }
    }


    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemWidth = itemView.width.toFloat()

        val horizontalMargin = (6 * context.resources.displayMetrics.density).toInt()
        val maxSwipeWidth = itemWidth * 0.5f
        val dxLimited = if (dX < 0) maxOf(dX, -maxSwipeWidth) else 0f

        if (dxLimited < 0f) {
            val backgroundLeft = itemView.right + dxLimited.toInt()

            background.setBounds(
                backgroundLeft + horizontalMargin,
                itemView.top,
                itemView.right - horizontalMargin,
                itemView.bottom
            )
            background.draw(c)

            val iconLeft = itemView.right - (dxLimited.toInt().absoluteValue / 2 + intrinsicWidth / 2)
            val iconTop = itemView.top + (itemView.height - intrinsicHeight) / 2
            deleteIcon.setBounds(iconLeft, iconTop, iconLeft + intrinsicWidth, iconTop + intrinsicHeight)
            deleteIcon.draw(c)
        }

        super.onChildDraw(c, recyclerView, viewHolder, dxLimited, dY, actionState, isCurrentlyActive)
    }
}
