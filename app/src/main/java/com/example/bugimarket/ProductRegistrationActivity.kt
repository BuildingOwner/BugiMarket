package com.example.bugimarket

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.*

class ProductRegistrationActivity : AppCompatActivity() {
    private val REQUEST_CODE = 100
    private val imageList = ArrayList<Uri>()
    private lateinit var adapter: ImageAdapter
    private lateinit var storageRef: StorageReference
    private var imageUriList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_registration)

        val closeButton: ImageView = findViewById(R.id.ic_close)
        closeButton.setOnClickListener {
            finish()
        }

        // Firebase Storage 레퍼런스 초기화
        storageRef = FirebaseStorage.getInstance().reference

        // 저장 버튼에 클릭 리스너 설정
        val saveButton: Button = findViewById(R.id.product_save_button)
        saveButton.setOnClickListener {
            saveDataToFirestore()
            startActivity(
                Intent(this, HomeActivity::class.java)
            )
            finish()
        }

        val add_image: ConstraintLayout = findViewById(R.id.add_image)
        add_image.setOnClickListener {
            // 카메라 앱을 열기 위한 인텐트
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            // 갤러리를 열기 위한 인텐트
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            // 인텐트 선택자를 생성
            val chooser = Intent.createChooser(galleryIntent, "사진을 선택하거나 찍으세요.")

            // 카메라 인텐트를 선택자에 추가
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

            // 인텐트 시작
            startActivityForResult(chooser, REQUEST_CODE)
        }

        adapter = ImageAdapter(imageList)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerview_image)
        recyclerView.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 결과가 OK이고 요청 코드가 일치하는 경우
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            // 선택된 이미지의 URI를 가져옵니다
            val selectedImage = data?.data

            selectedImage?.let {
                imageList.add(it)
                adapter.notifyDataSetChanged()
            }

            // 선택된 이미지를 Firebase에 업로드합니다.
            uploadImageToFirebase(selectedImage)
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri?) {
        if (imageUri != null) {
            val imageName = UUID.randomUUID().toString()
            val imageRef = storageRef.child("images/$imageName")

            val uploadTask = imageRef.putFile(imageUri)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    // 이미지 업로드 성공시에 이미지 URL을 리스트에 추가합니다.
                    downloadUri?.let {
                        imageUriList.add(it.toString())
                    }
                } else {
                    // 이미지 업로드 실패시에 사용자에게 메시지를 보여줍니다.
                    Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveDataToFirestore() {
        // 현재 로그인한 사용자를 가져옵니다.
        val user = Firebase.auth.currentUser

        // 사용자가 로그인하지 않았다면, 함수를 종료합니다.
        if (user == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 사용자가 입력한 데이터를 가져옵니다.
        val title = findViewById<EditText>(R.id.title_edit).text.toString()
        val price = findViewById<EditText>(R.id.price_edit).text.toString()
        val explanation = findViewById<EditText>(R.id.explanation_edit).text.toString()

        // Firestore의 인스턴스를 가져옵니다.
        val db = Firebase.firestore

        val now = Date()
        // 날짜 형식을 지정합니다.
        val format = SimpleDateFormat("yyyy/MM/dd/HH/mm/ss", Locale.getDefault())

        // 'format'을 사용하여 'now'를 문자열로 변환합니다.
        val dateString = format.format(now)

        // 저장할 데이터를 생성합니다.
        val data = hashMapOf(
            "userId" to user.uid,
            "title" to title,
            "price" to price,
            "explanation" to explanation,
            "images" to imageUriList,  // 이미지 URL 리스트를 추가
            "uploadTime" to dateString,    // 현재 시간을 추가
            "isSelling" to true        // 판매 여부를 나타내는 필드를 추가
        )

        // Firestore에 데이터를 저장합니다.
        db.collection("items").add(data)
            .addOnSuccessListener {
                Toast.makeText(this, "데이터 저장 완료", Toast.LENGTH_SHORT).show()
                finish()    // 메인 액티비티로 돌아갑니다.
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "데이터 저장 실패: $e", Toast.LENGTH_SHORT).show()
            }
    }
}
