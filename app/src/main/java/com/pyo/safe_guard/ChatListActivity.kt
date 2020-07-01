package com.pyo.safe_guard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pyo.safe_guard.Adapter.UserItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_list.*

class ChatListActivity : AppCompatActivity() {

    private val TAG = ChatListActivity::class.java.simpleName

    val db = FirebaseFirestore.getInstance()
    var auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        myChatList.setOnClickListener {
            val intent = Intent(this, MyRoomActivity::class.java)
            startActivity(intent)
        }

        val adapter = GroupAdapter<GroupieViewHolder>()

        recyclerview_list.adapter = adapter

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for(document in result) {
                    // 자기 자신은 채팅 리스트에 출력하지 않음
                    if(document.get("uid").toString() != auth.currentUser?.uid)
                    adapter.add(UserItem(document.get("username").toString(), document.get("uid").toString()))
                    Log.d(TAG, document.get("username").toString())
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
                recyclerview_list.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents", exception)
            }

        adapter.setOnItemClickListener { item, view ->

            Log.d(TAG, (item as UserItem).name)
            Log.d(TAG, (item as UserItem).uid)

            val name = (item as UserItem).name
            val uid = (item as UserItem).uid

            val intent = Intent(this, ChatRoomActivity::class.java)
            intent.putExtra("name", name)
            intent.putExtra("yourUid", uid)
            startActivity(intent)

        }

    }
}
