package com.pyo.safe_guard.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pyo.safe_guard.R
import com.pyo.safe_guard.navigation.model.AlarmModel
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import kotlinx.android.synthetic.main.item_comment.view.*

class AlarmFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_alarm, container, false)
        view.alarmfragment_recyclerview.adapter = AlarmRecyclerviewAdapter()
        view.alarmfragment_recyclerview.layoutManager = LinearLayoutManager(activity)

        return view
    }

    inner class AlarmRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var alarmModelList: ArrayList<AlarmModel> = arrayListOf()

        init {
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid", uid)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    alarmModelList.clear()
                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot.documents) {
                        alarmModelList.add(snapshot.toObject(AlarmModel::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.item_comment, p0, false)

            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return alarmModelList.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var view = p0.itemView

            FirebaseFirestore.getInstance().collection("profileImages")
                .document(alarmModelList[p1].uid!!).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val url = task.result!!["image"]
                    Glide.with(view.context).load(url).apply(RequestOptions().circleCrop())
                        .into(view.commentviewitem_imageview_profile)
                }
            }

            when (alarmModelList[p1].kind) {
                0 -> {
                    val str_0 = alarmModelList[p1].userId + " 님이 " + getString(R.string.alarm_favorite)
                    view.commentviewitem_textview_profile.text = str_0
                }
                1 -> {
                    val str_0 =
                        alarmModelList[p1].userId + " 님이 " + getString(R.string.alarm_comment)
                    view.commentviewitem_textview_profile.text = str_0
                }
                2 -> {
                    val str_0 = alarmModelList[p1].userId + " 님이 " + getString(R.string.alarm_follow)
                    view.commentviewitem_textview_profile.text = str_0
                }
            }
            view.commentviewitem_textview_comment.visibility = View.INVISIBLE
        }

    }
}