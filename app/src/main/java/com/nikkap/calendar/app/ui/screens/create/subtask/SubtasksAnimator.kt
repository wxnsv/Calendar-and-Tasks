package com.nikkap.calendar.app.ui.screens.create.subtask

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class SubtasksAnimator : DefaultItemAnimator() {

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        holder.itemView.animate()
            .translationX(-holder.itemView.width.toFloat())
            .alpha(0f)
            .setDuration(removeDuration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    holder.itemView.translationX = 0f
                    holder.itemView.alpha = 1f

                    dispatchRemoveFinished(holder)
                }
            }).start()

        return true
    }
}