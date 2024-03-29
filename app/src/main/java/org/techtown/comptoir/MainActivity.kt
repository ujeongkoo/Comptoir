package org.techtown.comptoir

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 툴바 사용 설정
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        // 타이틀 안보이게 설정
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        // BottomNavigationView 초기화
        bottomNavigation = findViewById(R.id.bottom_navigation)

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), 1)

        // 첫 번째 Fragment 표시
        val homeFragment = HomeFragment()
        replaceFragment(homeFragment)
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> {
                    val homeFragment = HomeFragment()
                    replaceFragment(homeFragment)
                    true
                }
                R.id.menu_cart -> {
                    val cartFragment = CartFragment()
                    replaceFragment(cartFragment)
                    true
                }
                R.id.menu_star -> {
                    val starFragment = StarFragment()
                    replaceFragment(starFragment)
                    true
                }
                R.id.menu_mypage -> {
                    val mypageFragment = MypageFragment()
                    replaceFragment(mypageFragment)
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)

        return true
    }

    // Toolbar 메뉴 클릭 이벤트
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.search -> {
                Toast.makeText(applicationContext, "검색", Toast.LENGTH_SHORT).show()
                return super.onOptionsItemSelected(item)
            } else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.containers, fragment)
        fragmentTransaction.commit()
    }
}