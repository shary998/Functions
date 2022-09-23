package com.go.sport.base

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gdacciaro.iOSDialog.iOSDialogBuilder
import com.go.sport.R
import com.go.sport.constants.Constants
import com.go.sport.sharedpref.MySharedPreference
import com.go.sport.ui.walkthrough.WalkThroughActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonParser
import com.jakewharton.rxbinding2.view.RxView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.bottom_sheet_are_you_sure.view.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


open class BaseActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MyBase"
        const val POPUPDISPLAY_MATCHCONT = 1
        const val POPUPDISPLAY_WRAPCONT = 2
    }

    lateinit var areYouSureBottomSheet: BottomSheetDialog

    interface OnBottomSheetDialogClickListener {
        fun onDismissButtonClick()
    }

    fun getDateTime(s: String): String? {
        var timeStamp = if (s.contains("."))
            s.split(".")[0]
        else
            s
        return try {
            val sdf = SimpleDateFormat("MM/dd/yyyy")
            val netDate = Date((timeStamp.toLong()) * 1000)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

    fun getTime(s: String): String? {
        var timeStamp = if (s.contains("."))
            s.split(".")[0]
        else
            s
        return try {
            val sdf = SimpleDateFormat("h:mm a")
            val netDate = Date((timeStamp.toLong()) * 1000)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }


    fun getConvoDateTime(s: String): String? {
        var timeStamp = if (s.contains("."))
            s.split(".")[0]
        else
            s
        return try {
            val sdf = SimpleDateFormat("dd-MMM-yyyy h:mm a")
            val netDate = Date((timeStamp.toLong()) * 1000)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    fun returnUserAuthToken(): HashMap<String, String> {
        val auth = HashMap<String, String>()
        auth["Authorization"] = MySharedPreference(this).getUserObject()?.token ?: ""
        return auth
    }

    @SuppressLint("CheckResult")
    fun initBottomSheet(
        msgText: String,
        btnText: String,
        listener: OnBottomSheetDialogClickListener
    ) {
        try {
            areYouSureBottomSheet = BottomSheetDialog(this)
            val view = LayoutInflater.from(this)
                .inflate(R.layout.bottom_sheet_are_you_sure, null)
            areYouSureBottomSheet.setContentView(view)

            view.tv_msg.text = msgText
            view.tv_btn.text = btnText
            RxView.clicks(view.cont_delete_account).throttleFirst(2, TimeUnit.SECONDS).subscribe {
                listener.onDismissButtonClick()
            }

            setupDialogBackground()
        } catch (ex: NullPointerException) {

        }
    }

    private fun setupDialogBackground() {
        areYouSureBottomSheet.setOnShowListener(DialogInterface.OnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
                ?: return@OnShowListener
            bottomSheet.background = null
        })
    }

    fun fullScreen() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }

    fun openBrowser(url: String) {
        var mUrl = url
        if (!mUrl.contains("https://") || !mUrl.contains("http://"))
            mUrl = "https://$mUrl"
        val browserIntent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(mUrl)
            )
        startActivity(browserIntent)
    }

    fun openWhatsApp(number: String) {
        val url = "https://api.whatsapp.com/send?phone=$number"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }


    /*fun openLoginActivity(
        from: String,
        shouldFinishAll: Boolean,
        code: Int = 1,
        isFinish: Boolean = false
    ) {
        val b = Bundle()
        b.putString("from", from)

        if (shouldFinishAll)
            startActivityFinishAll(
                this,
                LoginActivity::class.java,
                false,
                -1,
                b
            )
        else
            startActivity(
                this,
                LoginActivity::class.java,
                isFinish,
                code,
                b
            )
    }*/

    /*@SuppressLint("ClickableViewAccessibility")
    fun makeViewSmallOnClicked(view: View) {
        view.setOnTouchListener { _, event ->
            Log.d(TAG, "setOnTouchListener")
            if (event.action == MotionEvent.ACTION_DOWN) {
                val x = 0.95.toFloat()
                val y = 0.95.toFloat()
                view.scaleX = x
                view.scaleY = y
                view.setBackgroundResource(R.color.green_1)
            } else if (event.action == MotionEvent.ACTION_UP) {
                val x = 1f
                val y = 1f
                view.scaleX = x
                view.scaleY = y
                view.setBackgroundResource(R.color.green_1)
            }
            false
        }
    }*/

    /* fun getUserOrTemporaryId(completion: (String?) -> Unit) {
         if (MySharedPreference(this).getUserObject() != null) {
             MySharedPreference(this).getUserObject()?.user_id.let {
                 completion(it!!)
             }
         } else {
             completion(MySharedPreference(this).getTemporaryId())
         }
     }*/

    fun showDatePicker(
        activity: Activity,
        textView: TextView?,
        format: String = "dd-MM-yyyy",
        completion: (String?) -> Unit
    ) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd =
            activity.let {
                DatePickerDialog(
                    it,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        run {
                            textView?.text = formatDate(year, monthOfYear, dayOfMonth, format)
                            completion(formatDate(year, monthOfYear, dayOfMonth, format))
                        }
                    },
                    year,
                    month,
                    day
                )
            }
        dpd.show()
    }

    fun showDateOfBirthPicker(
        activity: Activity,
        editText: EditText?,
        format: String = "dd-MM-yyyy"
    ) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR) - 12
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd =
            activity.let {
                DatePickerDialog(
                    it,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        run {
                            val dob = formatDate(year, monthOfYear, dayOfMonth, format)
                            val sdf = SimpleDateFormat(format, Locale.getDefault())
                            val strDate = sdf.parse(dob)

                            if (System.currentTimeMillis() <= strDate.time)
                                warningToast("Please select date before current date")
                            else
                                editText?.setText(dob)
                        }
                    },
                    year,
                    month,
                    day
                )
            }
        dpd.show()
    }

    /*fun showPassword(isShow: Boolean, editText: EditText, imageView: ImageView): Boolean {
        val typeface = Typeface.createFromAsset(assets, Constants.MEDIUM)
        return if (isShow) {
            imageView.setImageResource(R.drawable.icon_show_password)
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            editText.typeface = typeface
            editText.setSelection(editText.text.length)
            false
        } else {
            imageView.setImageResource(R.drawable.icon_hide_password)
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            editText.typeface = typeface
            editText.setSelection(editText.text.length)
            true
        }
    }*/

    fun formatDate(year: Int, month: Int, day: Int, format: String): String {
        val myCalendar = Calendar.getInstance()
        myCalendar.set(year, month, day)
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(myCalendar.time)
    }

    fun shareAppExternally(message: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "GoPlay")
        var shareMessage = message
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(shareIntent, "choose one"))
    }

    fun showTimePickerDialog(
        activity: Activity,
        textView: TextView,
        completion: (time: Long) -> Unit
    ) {
        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            textView.text = SimpleDateFormat("hh:mm a").format(cal.time)
            completion((cal.timeInMillis) / 1000 / 60)
        }
        TimePickerDialog(
            activity,
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
    }

    open fun getURLForResource(resourceId: Int): String? {
        //use BuildConfig.APPLICATION_ID instead of R.class.getPackage().getName() if both are not same
        return Uri.parse(
            "android.resource://" + R::class.java.getPackage()!!.name + "/" + resourceId
        ).toString()
    }

    fun callPhone(activity: Activity) {

        Dexter.withActivity(activity)
            .withPermissions(
                Manifest.permission.CALL_PHONE
            ).withListener(object : MultiplePermissionsListener {
                @SuppressLint("MissingPermission")
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "1122334455"))
                        startActivity(intent)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token!!.continuePermissionRequest()
                }

            }
            ).check()
    }

    open fun setFont(font: String, textView: TextView) {
        val typeface = Typeface.createFromAsset(assets, font)
        textView.typeface = typeface
    }

    open fun setFont(font: String, editText: EditText) {
        val typeface = Typeface.createFromAsset(assets, font)
        editText.typeface = typeface
    }

    open fun setFont(font: String, textInputLayout: TextInputLayout) {
        val typeface = Typeface.createFromAsset(assets, font)
        textInputLayout.typeface = typeface
    }

    open fun setFont(font: String, radioButton: RadioButton) {
        val typeface = Typeface.createFromAsset(assets, font)
        radioButton.typeface = typeface
    }

    open fun setFont(font: String, cb: CheckBox) {
        val typeface = Typeface.createFromAsset(assets, font)
        cb.typeface = typeface
    }

    /**
    -1 id for LTR, 1 is for RTL, 0 is for fade, 2 id for pushinright,, 3 bottom to top, 4 top to bottom
     */

    open fun startActivity(
        context: Context,
        activity: Class<*>,
        isFinish: Boolean?,
        code: Int
    ) {
        //-1 id for LTR, 1 is for RTL, 0 is for fade
        startActivity(Intent(context, activity))
        when (code) {
            0 -> (context as Activity).overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            1 -> (context as Activity).overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_left
            )
            -1 -> (context as Activity).overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )
            2 -> (context as Activity).overridePendingTransition(
                R.anim.push_in_right,
                R.anim.push_out_left
            )
            3 -> (context as Activity).overridePendingTransition(
                R.anim.slide_in_bottom,
                R.anim.slide_out_bottom
            )
            4 -> (context as Activity).overridePendingTransition(
                R.anim.fadein_splash,
                R.anim.fadeout_splash
            )
        }
        if (isFinish!!)
            (context as Activity).finish()

    }

    open fun startActivity(
        context: Context,
        activity: Class<*>,
        isFinish: Boolean?,
        code: Int,
        bundle: Bundle
    ) {
        //-1 id for LTR, 1 is for RTL, 0 is for fade
        val intent = Intent(context, activity)

        intent.putExtras(bundle)
        /*for (i in 0 until keyvalue.size){
            intent.putExtra(keyvalue[i].key,keyvalue[i].value)
        }*/

        startActivity(intent)
        when (code) {
            0 -> (context as Activity).overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            1 -> (context as Activity).overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_left
            )
            -1 -> (context as Activity).overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )
            2 -> (context as Activity).overridePendingTransition(
                R.anim.push_in_right,
                R.anim.push_out_left
            )
            3 -> (context as Activity).overridePendingTransition(
                R.anim.slide_in_bottom,
                R.anim.slide_out_bottom
            )
        }
        if (isFinish!!)
            (context as Activity).finish()

    }

    open fun startActivityForResult(
        context: Context,
        activity: Class<*>,
        isFinish: Boolean?,
        code: Int,
        bundle: Bundle?,
        requestCode: Int
    ) {
        //-1 id for LTR, 1 is for RTL, 0 is for fade
        val intent = Intent(context, activity)

        bundle?.let { intent.putExtras(it) }

        startActivityForResult(intent, requestCode)
        when (code) {
            0 -> (context as Activity).overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            1 -> (context as Activity).overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_left
            )
            -1 -> (context as Activity).overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )
            2 -> (context as Activity).overridePendingTransition(
                R.anim.push_in_right,
                R.anim.push_out_left
            )
            3 -> (context as Activity).overridePendingTransition(
                R.anim.slide_in_bottom,
                R.anim.slide_out_bottom
            )
        }
        if (isFinish!!)
            (context as Activity).finish()

    }

    @SuppressLint("CheckResult")
    protected fun startActivityFinishAllWithRx(
        view: View,
        duration: Long,
        context: Context,
        activity: Class<*>,
        isFinish: Boolean?,
        code: Int = 1
    ) {
        RxView.clicks(view)
            .throttleFirst(duration, TimeUnit.SECONDS)
            .subscribe {
                startActivityFinishAll(context, activity, isFinish, code)
            }
    }

    open fun startActivityFinishAll(
        context: Context,
        activity: Class<*>,
        isFinish: Boolean?,
        code: Int
    ) {
        //-1 id for LTR, 1 is for RTL, 0 is for fade
        startActivity(Intent(context, activity))
        when (code) {
            0 -> (context as Activity).overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            1 -> (context as Activity).overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_left
            )
            -1 -> (context as Activity).overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )
            2 -> (context as Activity).overridePendingTransition(
                R.anim.push_in_right,
                R.anim.push_out_left
            )
            3 -> (context as Activity).overridePendingTransition(
                R.anim.slide_in_bottom,
                R.anim.slide_out_bottom
            )
        }
        if (isFinish!!)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                (context as Activity).finishAffinity()
            }
    }

    open fun startActivityFinishAll(
        context: Context,
        activity: Class<*>,
        isFinish: Boolean?,
        code: Int,
        bundle: Bundle
    ) {
        //-1 id for LTR, 1 is for RTL, 0 is for fade
        val intent = Intent(context, activity)

        intent.putExtras(bundle)
        /*for (i in 0 until keyvalue.size){
            intent.putExtra(keyvalue[i].key,keyvalue[i].value)
        }*/

        startActivity(intent)
        when (code) {
            0 -> (context as Activity).overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            1 -> (context as Activity).overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_left
            )
            -1 -> (context as Activity).overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )
            2 -> (context as Activity).overridePendingTransition(
                R.anim.push_in_right,
                R.anim.push_out_left
            )
            3 -> (context as Activity).overridePendingTransition(
                R.anim.slide_in_bottom,
                R.anim.slide_out_bottom
            )
        }
        if (isFinish!!)
            (context as Activity).finishAffinity()
    }

    //1 top to bottom
    open fun finish(
        context: Context,
        code: Int
    ) {
        (context as Activity).finish()
        if (code == 1) {
            context.overridePendingTransition(
                R.anim.slide_in_top,
                R.anim.slide_out_top
            )
        } else if (code == -1) {
            context.overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )
        }
    }

    @SuppressLint("CheckResult")
    open fun finishWithRx(
        view: View,
        duration: Long,
        context: Context,
        code: Int = -1
    ) {
        RxView.clicks(view)
            .throttleFirst(duration, TimeUnit.SECONDS)
            .subscribe {
                finish(context, code)
            }
    }

    override fun onBackPressed() {
        finish(this, -1)
    }

    open fun finish(
        context: Context,
        code: Int,
        resultCode: Int,
        bundle: Bundle
    ) {

        val intent = Intent()
        intent.putExtras(bundle)
        (context as Activity).setResult(resultCode, intent)
        (context).finish()

        if (code == 1) {
            (context).overridePendingTransition(
                R.anim.slide_in_top,
                R.anim.slide_out_top
            )
        } else if (code == -1) {
            (context).overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )
        }
    }

    @SuppressLint("CheckResult")
    open fun startActivityWithRx(
        view: View,
        duration: Long,
        context: Context,
        activity: Class<*>,
        isFinish: Boolean?,
        code: Int = 1
    ) {
        RxView.clicks(view)
            .throttleFirst(duration, TimeUnit.SECONDS)
            .subscribe {
                startActivity(context, activity, isFinish, code)
            }
    }

    protected fun changeTabsFont(context: Context, tablayout: TabLayout) {
        val childTabLayout = tablayout.getChildAt(0) as ViewGroup
        for (i in 0 until childTabLayout.childCount) {
            val viewTab = childTabLayout.getChildAt(i) as ViewGroup
            for (j in 0 until viewTab.childCount) {
                val tabTextView = viewTab.getChildAt(j)
                if (tabTextView is TextView) {
                    val typeface = Typeface.createFromAsset(context.assets, "TTNorms-Regular.otf")
                    tabTextView.typeface = typeface
                    tabTextView.setTextSize(
                        TypedValue.COMPLEX_UNIT_DIP,
                        12f
                    )
                }
            }
        }
    }


    fun popupDisplay(
        context: Context,
        optionsCont: View,
        tv: View?,
        showAtTop: Boolean,
        list: ArrayList<PowerMenuItem>,
        width: Int? = POPUPDISPLAY_MATCHCONT,
        index: Int,
        completion: (String) -> Unit
    ) {
        val powerMenu = PowerMenu.Builder(context)
            .addItemList(list)
            //.setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT)
            .setMenuRadius(20f)
            .setMenuShadow(10f)
            .setTextColor(ContextCompat.getColor(context, R.color.black))
            .setTextGravity(Gravity.START)
            .setTextSize(15)
            .setBackgroundAlpha(0.05F)
            .setTextTypeface(
                Typeface.createFromAsset(
                    context.assets,
                    Constants.MEDIUM
                )
            )
            .setDividerHeight(3)
            .setDivider(
                ColorDrawable(ContextCompat.getColor(context, R.color.grey))
            )
            .setSelectedTextColor(R.color.grey)
            .setMenuColor(Color.WHITE)
            .setSelectedMenuColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .build()
        powerMenu.setOnMenuItemClickListener { i: Int, powerMenuItem: PowerMenuItem ->
            if (tv is EditText)
                tv.setText(powerMenuItem.title)
            else if (tv is TextView)
                tv.setText(powerMenuItem.title)

            powerMenu.dismiss()

            when (index) {
                0 -> {
                    completion(powerMenuItem.title)
                }
                1 -> {

                    completion(powerMenuItem.title)
                }
            }
        }

        if (width == POPUPDISPLAY_MATCHCONT)
            powerMenu.setWidth(optionsCont.measuredWidth)

        if (showAtTop) {
            powerMenu.showAsAnchorRightTop(optionsCont)
        } else {
            powerMenu.showAsDropDown(optionsCont)
        }
    }

    open fun nameRegex(input: String): Boolean {
        val regex = Regex("[a-zA-Z]+")
        return input.matches(regex)
    }

    interface onDialogDone {
        fun onDoneClicked(dialog: Dialog)
    }

    protected fun getScaledBitmap(
        b: Bitmap,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        val m = Matrix()
        m.setRectToRect(
            RectF(0f, 0f, b.width.toFloat(), b.height.toFloat()),
            RectF(0f, 0f, reqWidth.toFloat(), reqHeight.toFloat()),
            Matrix.ScaleToFit.CENTER
        )
        return Bitmap.createBitmap(b, 0, 0, b.width, b.height, m, true)
    }

    protected fun vibratePhone(context: Context, vibrateAmount: Long) {
        val vibrator = context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    vibrateAmount,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(vibrateAmount)
        }
    }

    fun openKeyboard(editText: EditText) {
        val imm: InputMethodManager =
            getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, 0);
    }

    fun closeKeyboard() {
        // Check if no view has focus:
        val view = currentFocus
        if (view != null) {
            val imm =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    protected fun transparentStatusBar() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        )
    }

    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        Log.i(
            "*** Elenasys :: ",
            "Height is= $result"
        )
        return result
    }

    private var progressbar: AlertDialog? = null
    fun pBar(showOrHide: Int) {
        if (progressbar == null) {
            progressbar =
                AlertDialog.Builder(this).setView(R.layout.dialog_loader).setCancelable(false)
                    .create()
            progressbar?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        if (progressbar != null)
            if (showOrHide == 1) {
                progressbar?.show()
            } else if (showOrHide == 0) {
                progressbar?.dismiss()
            }
    }

    open fun getFileFromBitmap(bitmap: Bitmap, index: Int): File {
        val f = File(cacheDir, "something+$index")
        f.createNewFile()
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 25 /*ignored for PNG*/, bos)
        val bitmapdata = bos.toByteArray()
        val fos = FileOutputStream(f)
        fos.write(bitmapdata)
        fos.flush()
        fos.close()

        return f
    }

    fun scrollNestedScrollToBottom(root: NestedScrollView) {
        root.post { root.fullScroll(View.FOCUS_DOWN) }
    }

    open fun isLastItemDisplaying(recyclerView: RecyclerView): Boolean {
        if (recyclerView.adapter != null && recyclerView.adapter!!.itemCount != 0) {
            val lastItem =
                (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
            if (lastItem != RecyclerView.NO_POSITION && lastItem == recyclerView.adapter!!.itemCount - 1) return true
        }
        return false
    }

    fun changeFormatTime(
        currentFormat: String,
        currentTime: String,
        requiredFormat: String
    ): String {
        return try {
            val dateFormat = SimpleDateFormat(currentFormat);
            var sourceDate: Date? = null;
            try {
                sourceDate = dateFormat.parse(currentTime);
            } catch (e: ParseException) {
                e.printStackTrace();
            }

            val targetFormat = SimpleDateFormat(requiredFormat)
            targetFormat.format(sourceDate);
        } catch (e: Exception) {
            currentTime
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    open fun parseDateToddMMyyyy(time: String): String? {
        var datetime: String? = null
        val inputFormat: DateFormat = SimpleDateFormat("dd-MMM-yyyy")
        val d = SimpleDateFormat("yyyy-MM-dd")
        try {
            val convertedDate = inputFormat.parse(time)
            datetime = d.format(convertedDate)
        } catch (e: ParseException) {
        }
        return datetime
    }


    fun mOnError(error: Throwable) {
        var message = ""

        when (error) {
            is HttpException -> {
                // Kotlin will smart cast at this point
                val errorJsonString = error.response()?.errorBody()?.string()
                try {
                    message = JsonParser().parse(errorJsonString).asJsonObject["message"].asString
                } catch (ex: Exception) {
                    Log.e("mOnError", "Exception: $ex")
                }
            }
            is IOException -> {
                //message = "No Internet Connection"
            }
            else -> {
                message = if (error.message?.contains("Unable to resolve host") == true)
                    "No Internet Connection"
                else
                    error.message.toString()
            }
        }

        if (message != "")
            errorToast(message)
    }

    fun getDateTimeFormatted(requiredFormat: String): String {
        val dateFormat: DateFormat = SimpleDateFormat(requiredFormat)
        val date = Date()
        return dateFormat.format(date)
    }

    fun warningToast(message: String, length: Int = Toasty.LENGTH_LONG) {
        /*val toast: Toast = Toasty.warning(this, message, length, true)
        val toastLayout = toast.view as LinearLayout
        val toastTV = toastLayout.getChildAt(1) as TextView
        toastTV.typeface = Typeface.createFromAsset(assets, Constants.MEDIUM)
        toastTV.textSize = 14f
        toast.show()*/
        mytoast(message)
    }

    fun errorToast(message: String, length: Int = Toasty.LENGTH_LONG) {
        /*    val toast: Toast = Toasty.error(this, message, length, true)
            val toastLayout = toast.view as LinearLayout
            val toastTV = toastLayout.getChildAt(1) as TextView
            toastTV.typeface = Typeface.createFromAsset(assets, Constants.MEDIUM)
            toastTV.textSize = 14f
            toast.show()*/
        mytoast(message)

    }

    fun successToast(message: String, length: Int = Toasty.LENGTH_LONG) {
/*        val toast: Toast = Toasty.success(this, message, length, true)
        val toastLayout = toast.view as LinearLayout
        val toastTV = toastLayout.getChildAt(1) as TextView
        toastTV.typeface = Typeface.createFromAsset(assets, Constants.MEDIUM)
        toastTV.textSize = 14f
        toast.show()*/
        mytoast(message)

    }

    fun infoToast(message: String, length: Int = Toasty.LENGTH_LONG) {
        /*  val toast: Toast = Toasty.info(this, message, length, true)
          val toastLayout = toast.view as LinearLayout
          val toastTV = toastLayout.getChildAt(1) as TextView
          toastTV.typeface = Typeface.createFromAsset(assets, Constants.MEDIUM)
          toastTV.textSize = 14f
          return toast*/
        mytoast(message)

    }

    fun cancelToast(message: String, length: Int = Toasty.LENGTH_LONG) {
        warntoast(message)
    }


    fun infoToastOnBackPressed(message: String, length: Int = Toasty.LENGTH_LONG): Toast {
        val toast: Toast = Toasty.info(this, message, length, true)
        val toastLayout = toast.view as LinearLayout
        val toastTV = toastLayout.getChildAt(1) as TextView
        toastTV.typeface = Typeface.createFromAsset(assets, Constants.MEDIUM)
        toastTV.textSize = 14f
        return toast
    }

    private fun mytoast(message: String) {
        var dialog = iOSDialogBuilder(this)
            .setTitle("GoPlay")
            .setSubtitle(message)
            .setBoldPositiveLabel(true)
            .setCancelable(false)
            .setPositiveListener(
                "Ok"
            ) { dialog -> dialog.dismiss() }
            /* .setNegativeListener("Dismiss"
             ) { dialog -> dialog.dismiss() }*/
            .build().show()
    }

    private fun warntoast(message: String) {
        var dialog = iOSDialogBuilder(this)
            .setTitle("GoPlay")
            .setSubtitle(message)
            .setBoldPositiveLabel(true)
            .setCancelable(false)
            .setPositiveListener(
                "Ok"
            ) { dialog -> startActivity(this, WalkThroughActivity::class.java, false, -1) }
            .setNegativeListener(
                "Cancel"
            ) { dialog -> dialog.dismiss() }
            .build().show()
    }

    fun apiToast(message: String, completion: () -> Unit) {
        var dialog = iOSDialogBuilder(this)
            .setTitle("GoPlay")
            .setSubtitle(message)
            .setBoldPositiveLabel(true)
            .setCancelable(false)
            .setPositiveListener(
                "Ok"
            ) { dialog ->
                completion()
                dialog.dismiss()
            }
            .build().show()
    }

    fun disclaimer(message: String, completion: () -> Unit) {
        var dialog = iOSDialogBuilder(this)
            .setTitle("GoPlay")
            .setSubtitle(message)
            .setBoldPositiveLabel(true)
            .setCancelable(false)
            .setPositiveListener(
                "Agree"
            ) { dialog ->
                completion()
                dialog.dismiss()
            }
            .setNegativeListener(
                "Cancel"
            ) { dialog -> dialog.dismiss() }
            .build().show()
    }

    fun paymentToast(message: String, wallet: () -> Unit, cash: () -> Unit) {
        var dialog = iOSDialogBuilder(this)
            .setTitle("GoPlay")
            .setSubtitle(message)
            .setBoldPositiveLabel(true)
            .setCancelable(false)
            .setPositiveListener(
                "Wallet"
            ) { dialog ->
                wallet()
                dialog.dismiss()
            }
            .setNegativeListener(
                "Cash"
            ) { dialog ->
                cash()
                dialog.dismiss()
            }
            .build().show()
    }


    fun warntoastH(message: String, completion: () -> Unit) {
        var dialog = iOSDialogBuilder(this)
            .setTitle("GoPlay")
            .setSubtitle(message)
            .setBoldPositiveLabel(true)
            .setCancelable(false)
            .setPositiveListener(
                "Ok"
            ) { dialog ->
                completion()
                dialog.dismiss()
            }
            .setNegativeListener(
                "Cancel"
            ) { dialog -> dialog.dismiss() }
            .build().show()
    }


    fun getCurrentDate(format: String = "dd-MM-yyyy"): String {
        val c = Calendar.getInstance().time
        val df = SimpleDateFormat(format)
        return df.format(c)
    }

    fun getCurrentTime(format: String = "hh:mm a"): String {
        val c = Calendar.getInstance().time
        val df = SimpleDateFormat(format)
        return df.format(c)
    }

    fun getCurrentDateminusseven(format: String = "dd-MM-yyyy"): String {
        val calendar = Calendar.getInstance()

        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val newDate = calendar.time

        val df = SimpleDateFormat(format)
        return df.format(newDate)
    }

    fun imageViewAnimatedChange(v: ImageView, new_image: Bitmap?) {
        val animOut: Animation =
            AnimationUtils.loadAnimation(this, R.anim.fadeout)
        val animIn: Animation =
            AnimationUtils.loadAnimation(this, R.anim.fadein)
        animOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                v.setImageBitmap(new_image)
                animIn.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationRepeat(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {}
                })
                v.startAnimation(animIn)
            }
        })
        v.startAnimation(animOut)
    }

    open fun makeTextViewResizable(
        tv: TextView,
        maxLine: Int,
        expandText: String,
        viewMore: Boolean
    ) {
        if (tv.tag == null) {
            tv.tag = tv.text
        }
        val vto = tv.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val text: String
                val lineEndIndex: Int
                val obs = tv.viewTreeObserver
                obs.removeGlobalOnLayoutListener(this)
                if (maxLine == 0) {
                    lineEndIndex = tv.layout.getLineEnd(0)
                    text = tv.text.subSequence(0, lineEndIndex - expandText.length + 1)
                        .toString() + " " + expandText
                } else if (maxLine > 0 && tv.lineCount >= maxLine) {
                    lineEndIndex = tv.layout.getLineEnd(maxLine - 1)
                    text = tv.text.subSequence(0, lineEndIndex - expandText.length + 1)
                        .toString() + " " + expandText
                } else {
                    lineEndIndex = tv.layout.getLineEnd(tv.layout.lineCount - 1)
                    text = tv.text.subSequence(0, lineEndIndex).toString() + " " + expandText
                }
                tv.text = text
                tv.movementMethod = LinkMovementMethod.getInstance()
                tv.setText(
                    addClickablePartTextViewResizable(
                        Html.fromHtml(tv.text.toString()), tv, lineEndIndex, expandText,
                        viewMore
                    ), TextView.BufferType.SPANNABLE
                )
            }
        })
    }

    fun addClickablePartTextViewResizable(
        strSpanned: Spanned,
        tv: TextView,
        maxLine: Int,
        spannableText: String,
        viewMore: Boolean
    ): SpannableStringBuilder? {
        val str: String = strSpanned.toString()
        val ssb = SpannableStringBuilder(strSpanned)
        if (str.contains(spannableText)) {
            ssb.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    tv.layoutParams = tv.layoutParams
                    tv.setText(tv.tag.toString(), TextView.BufferType.SPANNABLE)
                    tv.invalidate()
                    if (viewMore) {
                        makeTextViewResizable(tv, -1, "View Less", false)
                    } else {
                        makeTextViewResizable(tv, 3, "View More", true)
                    }
                }
            }, str.indexOf(spannableText), str.indexOf(spannableText) + spannableText.length, 0)
        }
        return ssb
    }

    fun stringToRequestBody(text: String): RequestBody {
        return RequestBody.create(MediaType.parse("text/plain"), text)
    }

    fun getAgeFromDOB(year: Int, month: Int, day: Int): String? {
        val dob = Calendar.getInstance()
        val today = Calendar.getInstance()
        dob[year, month + 1] = day
        var age = today[Calendar.YEAR] - dob[Calendar.YEAR]
        if (today[Calendar.DAY_OF_YEAR] < dob[Calendar.DAY_OF_YEAR]) {
            age--
        }
        val ageInt = age
        return ageInt.toString()
    }

    fun isValidDate(inDate: String): Boolean {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        dateFormat.isLenient = false
        try {
            dateFormat.parse(inDate.trim())
        } catch (pe: ParseException) {
            return false
        }
        return true
    }

}
