package org.techtown.comptoir

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.techtown.comptoir.ImageAdapter.onItemClickListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WriteActivity : AppCompatActivity() {

    private lateinit var layoutTags: FlexboxLayout
    private lateinit var countText: TextView
    var uriList = ArrayList<Uri>()
    lateinit var adapter: ImageAdapter
    private val tagList = mutableListOf<String>()
    private var likeList = mutableListOf<String>()

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val spinnerBoard = findViewById<Spinner>(R.id.spinner_board)
        spinnerBoard.adapter = ArrayAdapter.createFromResource(this, R.array.borderList, android.R.layout.simple_spinner_item)

        val postTitle = findViewById<EditText>(R.id.edit_title)
        val postContent = findViewById<EditText>(R.id.edit_content)

        countText = findViewById(R.id.countArea)
        val imgBtn: ImageView = findViewById(R.id.imageArea)
        val recyclerview: RecyclerView = findViewById(R.id.recyclerview)

        adapter = ImageAdapter(this, uriList)

        printCount()
        recyclerview.adapter = adapter
        recyclerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        imgBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            registerForActivityResult.launch(intent)
        }

        adapter.setItemClickListener(object: onItemClickListener {
            override fun onItemClick(position: Int) {
                uriList.removeAt(position)
                adapter.notifyDataSetChanged()
                printCount()

            }
        })

        val btnKind = findViewById<Button>(R.id.btn_kind)
        btnKind.setOnClickListener {
            kindalert()
        }

        val btnNumber = findViewById<Button>(R.id.btn_numberpicker)
        val numberText = findViewById<TextView>(R.id.write_numberpicker)
        btnNumber.setOnClickListener {
            numberpikcer(numberText)
        }

        val placeName = findViewById<EditText>(R.id.place_edit)

        val btnGoal = findViewById<Button>(R.id.btn_goal)
        val resultText = findViewById<TextView>(R.id.goal_number)
        layoutTags = findViewById(R.id.tags_layout)
        btnGoal.setOnClickListener {
            numberalert(resultText)
        }

        val btnReg = findViewById<Button>(R.id.btn_reg)
        btnReg.setOnClickListener {
            val selectedBoard = spinnerBoard.selectedItem.toString()
            val title = postTitle.text.toString()
            val content = postContent.text.toString()
            val place = placeName.text.toString()
            var max = 0
            var number = 0
            var now_number = 0

            val regex ="""(\d+)명""".toRegex()
            val matchResult = regex.find(resultText.text.toString())
            if (matchResult != null) {
                number = matchResult.groupValues[1].toInt()
            }

            val regex2 = """(\d+)개""".toRegex()
            val matchResult2 = regex2.find(numberText.text.toString())
            if (matchResult2 != null) {
                max = matchResult2.groupValues[1].toInt()
            }


            regPost(selectedBoard, title, content, place, max, number, now_number, tagList, uriList, likeList)

        }
    }

    private fun printCount() {
        val text = "${uriList.count()}/10"
        countText.text = text
    }

    private val registerForActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                val clipData = result.data?.clipData
                if (clipData != null) { // 이미지를 여러 개 선택할 경우
                    val clipDataSize = clipData.itemCount
                    val selectableCount = 10 - uriList.count()
                    if (clipDataSize > selectableCount) { // 최대 선택 가능한 개수를 초과해서 선택한 경우
                        Toast.makeText(this@WriteActivity, "이미지는 최대 ${selectableCount}장까지 첨부할 수 있습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        // 선택 가능한 경우 ArrayList에 가져온 uri를 넣어준다.
                        for (i in 0 until clipDataSize) {
                            uriList.add(clipData.getItemAt(i).uri)
                        }
                    }
                } else { // 이미지를 한 개만 선택할 경우 null이 올 수 있다.
                    val uri = result?.data?.data
                    if (uri != null) {
                        uriList.add(uri)
                    }
                }
                // notifyDataSetChanged()를 호출하여 adapter에게 값이 변경 되었음을 알려준다.
                adapter.notifyDataSetChanged()
                printCount()
            }
        }
    }

    private fun numberpikcer(textView: TextView) {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.popup_numberpicker, null)

        val numberPicker: NumberPicker = dialogView.findViewById(R.id.number_picker)
        numberPicker.minValue = 1
        numberPicker.maxValue = 10

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
            .setPositiveButton("확인") { dialog, which ->
                val selectedValue = numberPicker.value
                textView.text = "${selectedValue}개"
            }
            .setNegativeButton("취소") { dialog, which ->
                dialog.cancel()
            }

        builder.create().show()
    }

    private fun numberalert(textView: TextView) {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.popup_numberpicker, null)

        val numberPicker: NumberPicker = dialogView.findViewById(R.id.number_picker)
        numberPicker.minValue = 1
        numberPicker.maxValue = 100

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
            .setPositiveButton("확인") { dialog, which ->
                val selectedValue = numberPicker.value
                textView.text = "${selectedValue}명"
            }
            .setNegativeButton("취소") { dialog, which ->
                dialog.cancel()
            }

        builder.create().show()
    }

    private fun kindalert() {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.popup_kindtag, null)

        val editKind: EditText = dialogView.findViewById(R.id.edit_kind)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
            .setPositiveButton("추가") { dialog, which ->
                val tagText = editKind.text.toString()
                if(tagText.isNotEmpty()) {
                    addTag(tagText)
                }
            }
            .setNegativeButton("취소") { dialog, which ->
                dialog.cancel()
            }

        builder.create().show()
    }

    private fun addTag(tagText: String) {
        val tagLayout = layoutInflater.inflate(R.layout.tag_layout, null) as LinearLayout
        val tagTextView: TextView = tagLayout.findViewById(R.id.tag_text)
        val deleteBtn: ImageView = tagLayout.findViewById(R.id.btn_delete)

        tagTextView.text = tagText

        deleteBtn.setOnClickListener {
            layoutTags.removeView(tagLayout)
            tagList.remove(tagText)
        }

        layoutTags.addView(tagLayout)
        tagList.add(tagText)
    }

    private fun regPost(selectedBoard: String, title: String, content: String, place: String, max: Int, number: Int, now_number: Int, tagList: List<String>, uriList: ArrayList<Uri>, likeList: List<String>) {
        val user = auth.currentUser?.uid

        if(user != null) {
            val db = FirebaseFirestore.getInstance()
            val postsCollection = db.collection("posts")
            val imageUrls = mutableListOf<String>()
            val uploadTasks = mutableListOf<Task<Uri>>()

            for (i in 0 until uriList.count()) {
                val fileName = SimpleDateFormat("yyyyMMddHHmmss_${i}").format(Date())
                val uri = uriList.get(i)
                val imageRef = FirebaseStorage.getInstance().reference.child("images/${fileName}")

                val uploadTask = imageRef.putFile(uri)
                    .continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        imageRef.downloadUrl
                    }
                uploadTasks.add(uploadTask)
            }

            Tasks.whenAllSuccess<String>(uploadTasks)
                .addOnSuccessListener { downloadUrls ->
                    imageUrls.addAll(downloadUrls)

                    val currentDateTime = Calendar.getInstance().time
                    var timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(currentDateTime)
                    val ing = "true"
                    val postData = hashMapOf(
                        "timestamp" to timeStamp,
                        "imageUrls" to imageUrls,
                        "tagList" to tagList,
                        "max number" to max,
                        "Target number of people" to number,
                        "Now number of people" to now_number,
                        "Place of receipt" to place,
                        "content" to content,
                        "title" to title,
                        "selected Board" to selectedBoard,
                        "user" to user,
                        "likeList" to likeList,
                        "ing" to ing
                    )

                    postsCollection.add(postData)
                        .addOnSuccessListener { documentReference ->
                            val postId = documentReference.id

                            val userRef = db.collection("users").document(user)
                            userRef.update("WriterPost", FieldValue.arrayUnion(postId))
                                .addOnSuccessListener {
                                    val intent = Intent(this, ContentActivity::class.java)
                                    intent.putExtra("postId", postId)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("WriterError", "${e.message}")
                                }

                            }
                        .addOnFailureListener { e ->
                            Log.w("RegPost", "작업이 실패하였습니다.", e)
                        }
                }
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}