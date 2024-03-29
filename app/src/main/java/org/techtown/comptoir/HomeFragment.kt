package org.techtown.comptoir

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class HomeFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ImageSliderAdapter

    private val imageList = arrayOf(
        R.drawable.comptoir1,
        R.drawable.comptoir2
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // ViewPager2 초기화
        viewPager = view.findViewById(R.id.viewPager)
        adapter = ImageSliderAdapter(imageList)
        viewPager.adapter = adapter

        Log.d("checkPoint", "${imageList}")

        val btnDelivery: Button = view.findViewById(R.id.btn_delivery)
        btnDelivery.setOnClickListener {
            delivery()
        }

        val btnLive: Button = view.findViewById(R.id.btn_living)
        btnLive.setOnClickListener {
            living()
        }

        val btnOtt: Button = view.findViewById(R.id.btn_ott)
        btnOtt.setOnClickListener {
            ott()
        }

        val btnFashion: Button = view.findViewById(R.id.btn_fashion)
        btnFashion.setOnClickListener {
            fashion()
        }

        val btnTrip: Button = view.findViewById(R.id.btn_trip)
        btnTrip.setOnClickListener {
            trip()
        }

        val btnBrand: Button = view.findViewById(R.id.btn_brand)
        btnBrand.setOnClickListener {
            brand()
        }

        val btnWrite: ImageButton = view.findViewById(R.id.btn_write)
        btnWrite.setOnClickListener {
            write()
        }

        return view
    }

    private fun write() {
        val intent = Intent(requireContext(), WriteActivity::class.java)
        startActivity(intent)
    }

    private fun delivery() {
        val intent = Intent(requireContext(), DeliveryActivity::class.java)
        startActivity(intent)
    }

    private fun living() {
        val intent = Intent(requireContext(), LiveActivity::class.java)
        startActivity(intent)
    }

    private fun ott() {
        val intent = Intent(requireContext(), OTTActivity::class.java)
        startActivity(intent)
    }

    private fun fashion() {
        val intent = Intent(requireContext(), FashionActivity::class.java)
        startActivity(intent)
    }

    private fun trip() {
        val intent = Intent(requireContext(), TripActivity::class.java)
        startActivity(intent)
    }

    private fun brand() {
        val intent = Intent(requireContext(), BrandActivity::class.java)
        startActivity(intent)
    }

}

class ImageSliderAdapter(private val images: Array<Int>) :
    RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_slider, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageResource(images[position])
    }

    override fun getItemCount(): Int {
        return images.size
    }

}