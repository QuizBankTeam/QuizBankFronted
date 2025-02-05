package com.example.quizbanktest.adapters.bank

import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.viewpager.widget.PagerAdapter
import com.example.quizbanktest.R
import java.util.*

class ViewPagerAdapter(val context: Context, val imageList: ArrayList<String>) : PagerAdapter() {
    // on below line we are creating a method
    // as get count to return the size of the list.
    override fun getCount(): Int {
        return imageList.size
    }

    // on below line we are returning the object
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as RelativeLayout
    }

    // on below line we are initializing
    // our item and inflating our layout file
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // on below line we are initializing
        // our layout inflater.
        val mLayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // on below line we are inflating our custom
        // layout file which we have created.
        val itemView: View = mLayoutInflater.inflate(R.layout.item_image, container, false)
        itemView.tag = "View$position"

        // on below line we are initializing
        // our image view with the id.
        val imageView: ImageView = itemView.findViewById<View>(R.id.iv_image) as ImageView
        imageView.setOnClickListener{ enlargeImage(position) }

        // on below line we are setting
        // image resource for image view.
        Log.e("ViewPagerAdapter", "position: $position")
        val imageBytes = Base64.decode(imageList[position], Base64.DEFAULT)
        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        imageView.setImageBitmap(decodedImage)

        // on the below line we are adding this
        // item view to the container.
        Objects.requireNonNull(container).addView(itemView)

        // on below line we are simply
        // returning our item view.
        return itemView
    }

    // on below line we are creating a destroy item method.
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        // on below line we are removing view
        container.removeView(`object` as RelativeLayout)
    }

    /** this override function is used for update all item when calling notifyDataSetChanged() */
    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    fun refreshItem() {
        notifyDataSetChanged()
    }

    private fun enlargeImage(position: Int) {
        val enlargeDialog = Dialog(context)
        enlargeDialog.setContentView(R.layout.dialog_enlarged_image)
        enlargeDialog.window?.setGravity(Gravity.CENTER)
        enlargeDialog.show()

        val enlargedImageView = enlargeDialog.findViewById<ImageView>(R.id.image)
        val imageBytes = Base64.decode(imageList[position], Base64.DEFAULT)
        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        enlargedImageView.setImageBitmap(decodedImage)
    }

}