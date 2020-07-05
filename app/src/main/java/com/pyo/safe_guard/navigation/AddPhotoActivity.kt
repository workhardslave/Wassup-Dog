package com.pyo.safe_guard.navigation


import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.pyo.safe_guard.R
import com.pyo.safe_guard.navigation.model.ContentModel
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 앨범 열기
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)

        // 이미지 업로드 이벤트
        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK){
                //이미지 경로
                photoUri = data?.data
                addphoto_image.setImageURI(photoUri)

            }else{
                finish()
            }
        }
    }
    fun contentUpload(){

        // 파일 이름
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        // 파일 업로드(Promise : 구글 권장 방식)
        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentModels = ContentModel()

            // 이미지의 다운로드 url
            contentModels.imageUrl = uri.toString()
            // 유저의 uid
            contentModels.uid = auth?.currentUser?.uid
            // 유저 아이디
            contentModels.userId = auth?.currentUser?.email
            // 컨텐츠 설명
            contentModels.explain = addphoto_edit_explain.text.toString()
            // 시간
            contentModels.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentModels)
            setResult(Activity.RESULT_OK)
            finish()
        }

    }
}

