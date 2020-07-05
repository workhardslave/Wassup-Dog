package com.pyo.safe_guard.navigation

import android.content.Intent
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
import com.pyo.safe_guard.navigation.model.ContentModel
import com.pyo.safe_guard.navigation.util.FcmPush
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.item_detail.view.*


class HomeFragment : Fragment() {
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_home, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.detailviewfragment_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentModels: ArrayList<ContentModel> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {


            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentModels.clear()
                    contentUidList.clear()
                    //Sometimes, This code return null of querySnapshot when it signout
                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(ContentModel::class.java)
                        contentModels.add(item!!)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }

        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.item_detail, p0, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentModels.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var viewholder = (p0 as CustomViewHolder).itemView



            // 유저 아이디
            viewholder.detailviewitem_profile_textview.text = contentModels!![p1].userId

            // 이미지
            Glide.with(p0.itemView.context).load(contentModels!![p1].imageUrl).into(viewholder.detailviewitem_imageview_content)

            // 컨텐츠 설명
            viewholder.detailviewitem_explain_textview.text = contentModels!![p1].explain

            // 프로필 이미지
            FirebaseFirestore.getInstance()
                .collection("profileImages")
                .document(contentModels[p1].uid!!)
                .get()
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        var url = task.result!!["image"]
                        Glide.with(p0.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(viewholder.detailviewitem_profile_image)
                    }
                }

            // 좋아요
            viewholder.detailviewitem_favoritecounter_textview.text = "좋아요 " + contentModels!![p1].favoriteCount

            // 버튼 클릭시
            viewholder.detailviewitem_favorite_imageview.setOnClickListener {
                favoriteEvent(p1)
            }

            // 좋아요 색깔 이벤트
            if(contentModels!![p1].favorites.containsKey(uid)){
                // 좋아요 클릭 O
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)
            }else{
                // 좋아요 클릭 X
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }

            // 이미지 클릭 시 발생 이벤트
            viewholder.detailviewitem_profile_image.setOnClickListener {
                var fragment = AccountFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentModels[p1].uid)
                bundle.putString("userId", contentModels[p1].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
            }
            viewholder.detailviewitem_comment_imageview.setOnClickListener { v ->
                var intent = Intent(v.context,CommentActivity::class.java)
                intent.putExtra("contentUid", contentUidList[p1])
                intent.putExtra("destinationUid", contentModels[p1].uid)
                startActivity(intent)
            }
        }

        fun favoriteEvent(position : Int){
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->


                var contentDTO = transaction.get(tsDoc!!).toObject(ContentModel::class.java)

                if(contentDTO!!.favorites.containsKey(uid)){
                    // 버튼을 클릭했을 때
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                    contentDTO?.favorites.remove(uid)
                }else{
                    // 버튼을 클릭하지 않았을 때
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                    contentDTO?.favorites[uid!!] = true
                    favoriteAlarm(contentModels[position].uid!!)

                }
                transaction.set(tsDoc,contentDTO)
            }

        }
        fun favoriteAlarm(destinationUid : String){
            var alarmDTO = AlarmModel()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

            var message = FirebaseAuth.getInstance()?.currentUser?.email +  getString(R.string.alarm_favorite)
            FcmPush.instance.sendMessage(destinationUid,"왔어독",message)
        }

    }
}