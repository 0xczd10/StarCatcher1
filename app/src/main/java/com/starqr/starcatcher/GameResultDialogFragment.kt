package com.starqr.starcatcher

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class GameResultDialogFragment : DialogFragment() {

    // Интерфейс, чтобы сообщить GameActivity, что диалог закрыт
    interface GameResultDialogListener {
        fun onDialogDismissed()
    }

    var listener: GameResultDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_game_result, null)

        val isWin = arguments?.getBoolean(ARG_IS_WIN) ?: false
        val title = arguments?.getString(ARG_TITLE)
        val message = arguments?.getString(ARG_MESSAGE)

        val resultIcon: ImageView = view.findViewById(R.id.resultIcon)
        val resultTitle: TextView = view.findViewById(R.id.resultTitle)
        val resultMessage: TextView = view.findViewById(R.id.resultMessage)
        val okButton: Button = view.findViewById(R.id.okButton)

        resultTitle.text = title
        resultMessage.text = message
        resultIcon.setImageResource(if (isWin) R.drawable.ic_trophy else R.drawable.ic_time_up)
        okButton.text = if (isWin) "CONTINUE" else "TRY AGAIN"

        okButton.setOnClickListener {
            dismiss() // Закрываем диалог
        }

        builder.setView(view)
        val dialog = builder.create()
        // Делаем фон диалога прозрачным, чтобы были видны наши скругленные углы
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        return dialog
    }

    override fun dismiss() {
        super.dismiss()
        listener?.onDialogDismissed() // Сообщаем Activity
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"
        private const val ARG_IS_WIN = "is_win"

        fun newInstance(title: String, message: String, isWin: Boolean): GameResultDialogFragment {
            val fragment = GameResultDialogFragment()
            val args = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_MESSAGE, message)
                putBoolean(ARG_IS_WIN, isWin)
            }
            fragment.arguments = args
            return fragment
        }
    }
}