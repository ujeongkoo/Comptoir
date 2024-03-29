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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class RewriteActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var re_spinner: Spinner
    private lateinit var re_title: EditText
    private lateinit var re_content: EditText
    private lateinit var re_place: EditText
    private lateinit var re_goal: TextView
    private lateinit var re_countText: TextView
    var uriList = ArrayList<Uri>()
    lateinit var adapter: ImageAdapter
    private val tagList = mutableListOf<String>()
    private lateinit var layoutTags: FlexboxLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rewrite)

        re_spinner = findViewById(R.id.re_spinner_board)
        re_title = findViewById(R.id.re_edit_title)
        re_content = findViewById(R.id.re_edit_content)
        re_place = findViewById(R.id.re_place_edit)
        re_goal = findViewById(R.id.re_goal_number)

        re_countText= findViewById(R.id.re_countArea)
        val imgBtn: ImageView = findViewById(R.id.re_imageArea)
        val recyclerview: RecyclerView = findViewById(R.id.re_recyclerview)

        layoutTags = findViewById(R.id.re_tags_layout)
        val btnKind = findViewById<Button>(R.id.re_btn_kind)
        btnKind.setOnClickListener {
            rekindalert()
        }

        firestore = FirebaseFirestore.getInstance()

        val postId = intent.getStringExtra("postId")

        if(postId != null) {
            val rewriteRef = firestore.collection("posts").document(postId)
            rewriteRef.get()
                .addOnSuccessListener { documentSnapShot ->
                    if (documentSnapShot.exists()) {
                        val title = documentSnapShot.getString("title")
                        val content = documentSnapShot.getString("content")
                        val target = documentSnapShot.getLong("Target number of people")
                        val place = documentSnapShot.getString("Place of receipt")
                        val imageList = documentSnapShot.get("imageUrls") as? ArrayList<String>
                        val item = documentSnapShot.get("tagList") as? ArrayList<String>

                        rewrite(title, content, target, place)

                        if (imageList != null) {
                            for (imageUrl in imageList) {
                                downloadImage(imageUrl) { uri ->
                                    uri?.let {
                                        uriList.add(it)
                                        adapter.notifyDataSetChanged()
                                        rePrintCount()
                                    }
                                }
                            }
                        }
                        adapter = ImageAdapter(this, uriList)
                        rePrintCount()
                        recyclerview.adapter = adapter
                        recyclerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

                        imgBtn.setOnClickListener {
                            val intent = Intent(Intent.ACTION_PICK)
                            intent.type= "image/*"
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            registerForActivityResult.launch(intent)
                        }

                        adapter.setItemClickListener(object: ImageAdapter.onItemClickListener {
                            override fun onItemClick(position: Int) {
                                uriList.removeAt(position)
                                adapter.notifyDataSetChanged()
                                rePrintCount()
                            }
                        })

                        if(item != null) {
                            for (itemName in item) {
                                reTag(itemName)
                            }
                        }
                    } else {
                        Toast.makeText(this@RewriteActivity, "해당 포스트가 존재하지 않습니다", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("reWriteFail", "${e.message}")
                }
        } else {
            Toast.makeText(this@RewriteActivity, "해당 포스트 정보가 없습니다.", Toast.LENGTH_SHORT).show()
        }

        val btnGoal = findViewById<Button>(R.id.re_btn_goal)
        val resultText = findViewById<TextView>(R.id.re_goal_number)

        btnGoal.setOnClickListener {
            reNumberAlert(resultText)
        }

        val btnReg = findViewById<Button>(R.id.re_btn_reg)
        btnReg.setOnClickListener {
            val selectedBoard = re_spinner.selectedItem.toString()
            val title = re_title.text.toString()
            val content = re_content.text.toString()
            val place = re_place.text.toString()
            var number = 0

            val regex = """(\d+)명""".toRegex()
            val matchResult = regex.find(resultText.text.toString())
            if (matchResult != null) {
                number = matchResult.groupValues[1].toInt()
            }

            reRegPost(postId, selectedBoard, title, content, place, number, tagList, uriList)
        }
    }

    private fun rewrite(title: String?, content: String?, target:Long?, place: String?) {
        re_spinner.adapter = ArrayAdapter.createFromResource(this, R.array.borderList, android.R.layout.simple_spinner_item)
        re_title.setText(title)
        re_content.setText(content)
        re_place.setText(place)
        re_goal.text = "${target}명"
    }

    private fun rePrintCount() {
        val text = "${uriList.count()}/10"
        re_countText.text = text
    }

    private fun downloadImage(imageUrl: String, onComplete: (Uri?) -> Unit) {
        val storageRef: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        storageRef.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uri: Uri? = task.result
                onComplete(uri)
            } else {
                onComplete(null)
            }
        }
    }

    private val registerForActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                val clipData = result.data?.clipData
                if (clipData != null) {
                    val clipDataSize = clipData.itemCount
                    val selectableCount = 10 - uriList.count()
                    if(clipDataSize > selectableCount) {
                        Toast.makeText(this@RewriteActivity, "이미지는 최대 ${selectableCount}장까지 첨부할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        for (i in 0 until clipDataSize) {
                            uriList.add(clipData.getItemAt(i).uri)
                        }
                    }
                } else {
                    val uri = result?.data?.data
                    if(uri != null) {
                        uriList.add(uri)
                    }
                }

                adapter.notifyDataSetChanged()
                rePrintCount()
            }
        }
    }

    private fun rekindalert() {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.popup_kindtag, null)

        val editKind: EditText = dialogView.findViewById(R.id.edit_kind)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
            .setPositiveButton("추가") { dialog, which ->
                val tagText = editKind.text.toString()
                if(tagText.isNotEmpty()) {
                    reTag(tagText)
                }
            }
            .setNegativeButton("취소") { dialog, which ->
                dialog.cancel()
            }

        builder.create().show()
    }

    private fun reTag(itemName: String) {
        val tagLayout = layoutInflater.inflate(R.layout.tag_layout, null) as LinearLayout
        val tagTextView: TextView = tagLayout.findViewById(R.id.tag_text)
        val deleteBtn: ImageView = tagLayout.findViewById(R.id.btn_delete)

        tagTextView.text = itemName

        deleteBtn.setOnClickListener {
            layoutTags.removeView(tagLayout)
            tagList.remove(itemName)
        }

        layoutTags.addView(tagLayout)
        tagList.add(itemName)
    }

    private fun reNumberAlert(textView: TextView) {
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

    private fun reRegPost(postId: String?, selectedBoard: String, title: String, content: String, place: String, number: Int, tagList: List<String>, uriList: ArrayList<Uri>) {
        if (postId != null) {
            val postRef = FirebaseFirestore.getInstance().collection("posts").document(postId)
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

                    val postData = hashMapOf(
                        "imageUrls" to imageUrls,
                        "tagList" to tagList,
                        "Target number of people" to number,
                        "Place of receipt" to place,
                        "content" to content,
                        "title" to title,
                        "selected Board" to selectedBoard
                    )

                    postRef.update(postData)
                        .addOnSuccessListener { documentReference ->
                            Toast.makeText(this@RewriteActivity, "수정이 완료되었습니다.", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, ContentActivity::class.java)
                            intent.putExtra("postId", postId)
                            ContextCompat.startActivity(this, intent, null)
                        }
                        .addOnFailureListener { e ->
                            Log.w("ReWrite", "작업이 실패하였습니다.", e)
                        }
                }
        }
    }
}