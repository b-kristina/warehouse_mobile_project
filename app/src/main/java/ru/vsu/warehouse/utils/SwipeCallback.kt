package ru.vsu.warehouse.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import ru.vsu.warehouse.R

class SwipeToEditCallback(
    private val context: Context,
    private val onEdit: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

    private val editIcon: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_edit)!!
    private val intrinsicWidth = editIcon.intrinsicWidth
    private val intrinsicHeight = editIcon.intrinsicHeight

    private val background: GradientDrawable = GradientDrawable().apply {
        setColor(ContextCompat.getColor(context, R.color.swipe_edit_background))
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
            onEdit(position)
            // Сбрасываем состояние свайпа, чтобы зелёный фон вернулся назад
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

        // Горизонтальный отступ, чтобы фон не выходил за границы карточки
        val horizontalMargin = (6 * context.resources.displayMetrics.density).toInt()

        // Ограничиваем ширину свайпа до 50% карточки
        val maxSwipeWidth = itemWidth * 0.5f
        val dxLimited = if (dX > 0) minOf(dX, maxSwipeWidth) else 0f

        if (dxLimited > 0f) {
            val backgroundRight = itemView.left + dxLimited.toInt()

            // Фон рисуем по полной высоте карточки (без вертикальных маргинов)
            background.setBounds(
                itemView.left + horizontalMargin,
                itemView.top,
                backgroundRight - horizontalMargin,
                itemView.bottom
            )
            background.draw(c)

            // Иконка по центру нарисованного фона
            val iconLeft = itemView.left + (dxLimited.toInt() - intrinsicWidth) / 2
            val iconTop = itemView.top + (itemView.height - intrinsicHeight) / 2
            editIcon.setBounds(iconLeft, iconTop, iconLeft + intrinsicWidth, iconTop + intrinsicHeight)
            editIcon.draw(c)
        }

        // Передаём ограниченный dx
        super.onChildDraw(c, recyclerView, viewHolder, dxLimited, dY, actionState, isCurrentlyActive)
    }
}
