package com.pyo.safe_guard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.pyo.safe_guard.Adapter.ChatLeftYou
import com.pyo.safe_guard.Adapter.ChatRightMe
import com.pyo.safe_guard.navigation.model.ChatModel
import com.pyo.safe_guard.navigation.model.ChatNewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_room.*

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private val TAG = ChatRoomActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val adapter = GroupAdapter<GroupieViewHolder>()

        val myUid = auth.uid
        val yourUid = intent.getStringExtra("yourUid")
        val name = intent.getStringExtra("name")

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("message")
        var readRef = database.getReference("message").child(myUid.toString()).child(yourUid)

        val childEventListener = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildAdded(p0: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "p0 : " + p0)

//                val msg = p0.getValue(ChatNewModel::class.java)?.message
//                Log.d(TAG, "p0 : "+ msg)

                val model = p0.getValue(ChatModel::class.java)
                Log.d(TAG, "model:" + model)


                val msg = model?.message.toString()
                val who = model?.who
                val yourUid = model?.yourUid

                Log.d(TAG, "msg : " + msg)


                if(who == "me") {
                    adapter.add(ChatRightMe(msg))
                } else {
                    db.collection("users")
                        .get()
                        .addOnSuccessListener { result ->
                            for(document in result) {
                                // 상대방 이름 출력
                                if(document.get("uid") == yourUid) {
                                    adapter.add(ChatLeftYou(msg, document.get("username").toString()))
                                    return@addOnSuccessListener
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d(TAG, "Error getting documents", exception)
                        }

                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        }
        recyclerview_chat_room.adapter = adapter
        readRef.addChildEventListener(childEventListener)

        val myRef_list = database.getReference("message-user-list")

        button_chat_room.setOnClickListener {

            val message = editText_chat_room.text.toString()


            val chat = ChatNewModel(myUid.toString(), yourUid, message, System.currentTimeMillis(), "me")
            myRef.child(myUid.toString()).child(yourUid).push().setValue(chat)

            val chat_get = ChatModel(yourUid, myUid.toString(),  message, System.currentTimeMillis(), "you")
            myRef.child(yourUid.toString()).child(myUid.toString()).push().setValue(chat_get)

            myRef_list.child(myUid.toString()).child(yourUid).setValue(chat)

            editText_chat_room.setText("")

        }
    }
}
