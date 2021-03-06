package com.ttchain.githubusers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.text.Html
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.ttchain.githubusers.dialog.ToastDialog
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

@SuppressLint("HardwareIds")
fun getUniqueDeviceId(context: Context): String {
    val devIDShort = ("35"
            + Build.BOARD.length % 10
            + Build.BRAND.length % 10
            + Build.CPU_ABI.length % 10
            + Build.DEVICE.length % 10
            + Build.MANUFACTURER.length % 10
            + Build.MODEL.length % 10
            + Build.PRODUCT.length % 10)

    val serial: String = try {
        Build::class.java.getField("SERIAL").get(null).toString()
    } catch (exception: Exception) {
        "serial"
    }

    val androidId: String = try {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    } catch (exception: Exception) {
        "android_id"
    }

    return UUID(
        devIDShort.hashCode().toLong(),
        (serial + androidId).hashCode().toLong()
    ).toString()
}

fun getApiAddress(): String {
    return App.apiAddress
}

fun String.getSHA512(): String {
    val md: MessageDigest = MessageDigest.getInstance("SHA-512")
    val messageDigest = md.digest(this.toByteArray())

    // Convert byte array into signum representation
    val no = BigInteger(1, messageDigest)

    // Convert message digest into hex value
    var hashtext: String = no.toString(16)

    // Add preceding 0s to make it 128 bit
    while (hashtext.length < 128) {
        hashtext = "0$hashtext"
    }

    // return the HashText
    return hashtext
}

/**
 * Toast Dialog
 */
fun FragmentManager.showSendToast(success: Boolean, title: String, content: String) {
    addDialog(
        ToastDialog.newInstance(
            title,
            content,
            if (success) R.color.white else R.color.white,
            if (success) R.mipmap.icon_success else R.mipmap.icon_fail
        ), "toast"
    )
}

fun Fragment.startSettingsActivity() {
    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:${requireContext().applicationContext.packageName}")
    })
}

fun <T> Observable<T>.toMain(): Observable<T> {
    return subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

fun BigDecimal.isZero(): Boolean {
    return this.toDouble() == 0.0
}

fun FragmentManager.addDialog(fragment: Fragment, tag: String) {
    if (this.findFragmentByTag(tag)?.isAdded == true) {
        return
    }
    commit(true) {
        add(fragment, tag)
    }
}

fun AppCompatActivity.changeFragment(container: Int, fragment: Fragment) {
    supportFragmentManager.commit(true) {
        replace(container, fragment)
    }
}

fun AppCompatActivity.addFragment(container: Int, fragment: Fragment) {
    supportFragmentManager.commit(true) {
        add(container, fragment)
        addToBackStack(fragment.javaClass.name)
    }
}

fun Fragment.addFragment(container: Int, fragment: Fragment) {
    childFragmentManager.commit(true) {
        add(container, fragment)
    }
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun String.performCopyString(context: Context) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.primaryClip = ClipData.newPlainText(null, this)
    context.showToast("Copied")
}

/**
 * 關閉鍵盤
 */
fun Activity.hideKeyboard() {
    val inputMethodManager =
        this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var focusView = this.currentFocus
    if (focusView == null)
        focusView = View(this)
    inputMethodManager.hideSoftInputFromWindow(focusView.windowToken, 0)
}

/**
 * 開啟鍵盤
 */
fun View.showKeyboard() {
    (this.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
        this@showKeyboard,
        InputMethodManager.SHOW_IMPLICIT
    )
}

/**
 * 載入html文字
 */
@Suppress("DEPRECATION")
fun TextView.html(html: String) {
    text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)!!
    } else {
        Html.fromHtml(html)
    }
}

/**
 * 將圖片放入系統相簿
 */
fun Activity.addImageToGallery(fileName: String, filePath: String) {
    contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues().apply {
        put(MediaStore.Images.Media.TITLE, fileName)
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)
        put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis() / 1000)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.DATA, filePath)
    })
}