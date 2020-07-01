package com.pyo.safe_guard.Adapter

import com.pyo.safe_guard.R
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_left_you.view.*

class ChatLeftYou(val msg: String, val stranger: String) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_left_you
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.left_name.text = stranger
        viewHolder.itemView.left_chat.text = msg
    }
}