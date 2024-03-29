package org.techtown.comptoir

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat
import java.util.Date

class MypageFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var imageViewUser: ImageView

    private var pickImageFromAlbum = 0
    private var uriPhoto : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mypage, container, false)

        val user = auth.currentUser
        if (user == null) {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        imageViewUser = view.findViewById(R.id.user_image)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val mypageNameTextView: TextView = view.findViewById(R.id.mypage_name)
        setUserName(mypageNameTextView)

        val mypageProfile: ImageView = view.findViewById(R.id.user_image)
        setUserImage(mypageProfile)

        val btnChooseImage: Button = view.findViewById(R.id.btn_image_change)
        btnChooseImage.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, pickImageFromAlbum)
        }

        val btnParti: Button = view.findViewById(R.id.btn_order_list)
        btnParti.setOnClickListener {
            val intent = Intent(requireContext(), OrderActivity::class.java)
            startActivity(intent)
        }

        val btnLike: Button = view.findViewById(R.id.btn_like_list)
        btnLike.setOnClickListener {
            val intent = Intent(requireContext(), LikeActivity::class.java)
            startActivity(intent)
        }

        val btnWriter: Button = view.findViewById(R.id.btn_writer_list)
        btnWriter.setOnClickListener {
            val intent = Intent(requireContext(), WriterActivity::class.java)
            startActivity(intent)
        }

        val btnLogOut: Button = view.findViewById(R.id.btn_logout)
        btnLogOut.setOnClickListener {
            logout()
        }

        val btnWithDraw: Button = view.findViewById(R.id.btn_withdraw)
        btnWithDraw.setOnClickListener {
            showWithdrawPopup()
        }

        return view
    }

    private fun setUserName(textView: TextView) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        userId.let {
            val userRef = it?.let { it1 -> firestore.collection("users").document(it1) }
            userRef?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document: DocumentSnapshot? = task.result
                    if (document != null && document.exists()) {
                        val userName = document.getString("name")

                        userName?.let {
                            textView.text = "${it}님"
                        }
                    }
                }
            }
        }
    }

    private fun setUserImage(imageView: ImageView) {
        loadProfileImage(imageView)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == pickImageFromAlbum && resultCode == Activity.RESULT_OK) {
            uriPhoto = data?.data
            imageViewUser.setImageURI(uriPhoto)

            uploadProfileImage()
        }
    }

    private fun uploadProfileImage() {
        val userId = auth.currentUser?.uid ?: return

        deletePreviousImage(userId)

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imgFileName = "IMAGE_$timestamp.jpg"
        val storageRef = storage.reference.child("/images/$userId/$imgFileName")

        val inputStream = requireContext().contentResolver.openInputStream(uriPhoto!!)
        val imageBytes = inputStream?.readBytes()

        if (imageBytes != null) {
            storageRef.putBytes(imageBytes)
                .addOnCompleteListener { task: Task<UploadTask.TaskSnapshot> ->
                    if (task.isSuccessful) {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            updateProfileImageURL(uri.toString())
                        }
                    } else {
                        Toast.makeText(requireContext(), "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun deletePreviousImage(userId: String) {
        val storageRef = storage.reference.child("/images/$userId/")

        storageRef.listAll()
            .addOnSuccessListener { result ->
                for (item in result.items) {
                    item.delete()
                }
            }
            .addOnFailureListener { e ->
                Log.e("DeleteImage", "이미지 삭제 실패: ${e.message}")
            }
    }

    private fun updateProfileImageURL(imageURL: String) {
        val userId = auth.currentUser?.uid ?: return

        val userRef = firestore.collection("users").document(userId)
        userRef.update("profileImageUrl", imageURL)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "프로필 이미지 업로드 완료", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "프로필 이미지 URL 업데이트 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadProfileImage(imageView: ImageView) {
        val userId = auth.currentUser?.uid ?: return
        val ImageRef = storage.reference.child("images/$userId/")

        ImageRef.listAll()
            .addOnSuccessListener { result ->
                if (result.items.isNotEmpty()) {
                    val firstItem = result.items[0]

                    val fileName = firstItem.name

                    val storageRef = storage.reference.child("images/$userId/$fileName")
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(this).load(uri).into(imageView)
                    }.addOnFailureListener { e ->
                        Log.e("LoadImage", "이미지 로딩 실패: ${e.message}", e)
                        imageView.setImageResource(R.drawable.basic_image)
                    }
                }
            }
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
    }

    private fun showWithdrawPopup() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("회원 탈퇴")
        builder.setMessage("정말로 떠나시겠습니까?")

        builder.setPositiveButton("확인") { dialogInterface: DialogInterface, i: Int ->
            withdraw()
        }
        builder.setNegativeButton("취소") { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
        }

        builder.show()
    }

    private fun withdraw() {
        val currentUser = auth.currentUser

        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.popup_withdraw, null)

        val emailEditText = dialogView.findViewById<EditText>(R.id.popup_email)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.popup_password)

        builder.setView(dialogView)
            .setPositiveButton("회원탈퇴") { dialog, _ ->
                val userEmail = auth.currentUser?.email
                val inputEmail = emailEditText.text.toString()
                val inputPassword = passwordEditText.text.toString()

                if (userEmail == inputEmail) {
                    val credential = EmailAuthProvider.getCredential(inputEmail, inputPassword)

                    currentUser?.reauthenticate(credential)
                        ?.addOnCompleteListener { reauthTask ->
                            if (reauthTask.isSuccessful) {
                                currentUser?.delete()

                                deleteFirestoreData(currentUser.uid)
                                deleteStorageData(currentUser.uid)

                                val intent = Intent(requireContext(), MainActivity::class.java)
                                startActivity(intent)
                                requireActivity().finish()
                                Toast.makeText(requireContext(), "안녕히가세요!", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.e("MypageFragment", "재인증 실패", reauthTask.exception)
                            }
                            dialog.dismiss()
                        }
                }
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.cancel()
            }

        builder.create().show()


    }

    private fun deleteFirestoreData(userId: String) {
        firestore.collection("users").document(userId)
            .delete()
            .addOnSuccessListener {
                Log.d("MypageFragment", "사용자 데이터 삭제 성공")
            }
            .addOnFailureListener {
                Log.d("MypageFragment", "사용자 데이터 삭제 실패")
            }
    }

    private fun deleteStorageData(userId: String) {
        val storageRef = storage.reference.child("images/$userId")

        storageRef.listAll()
            .addOnSuccessListener { result ->
                for (item in result.items) {
                    item.delete()
                        .addOnSuccessListener {
                            Log.d("MypageFragment", "사용자 이미지 삭제 성공")
                        }
                        .addOnFailureListener {
                            Log.d("MypageFragment", "사용자 이미지 삭제 실패")
                        }
                }
            }
            .addOnFailureListener {
                Log.d("MypageFragment", "파일 목록 가져오기 실패")
            }
    }
}