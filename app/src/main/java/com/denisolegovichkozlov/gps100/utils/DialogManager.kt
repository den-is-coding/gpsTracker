package com.denisolegovichkozlov.gps100.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.Toast
import com.denisolegovichkozlov.gps100.R
import com.denisolegovichkozlov.gps100.databinding.SaveDialogBinding
import db.TrackItem

object DialogManager {
    fun showLocEnabledDialog(context: Context, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle(R.string.location_disabled)
        dialog.setMessage(context.getString(R.string.location_dialog_message))
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") {
            _, _ ->  listener.onClick()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") {
                _, _ -> dialog.dismiss()
        }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    fun showSaveDialog(context: Context, item: TrackItem?, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        val binding = SaveDialogBinding.inflate(LayoutInflater.from(context), null, false)
        builder.setView(binding.root)
        val dialog = builder.create()
        binding.apply {
            tvTimeSave.text = "time: ${item?.time}"
            tvSpeed.text = "Average speed: ${item?.speed} km/h"
            tvDistanceSave.text = "Distance: ${item?.distance} km"

            bSAve.setOnClickListener {
                listener.onClick()
                dialog.dismiss()
            }
            bCancel.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    interface Listener{
        fun onClick()
    }
}
