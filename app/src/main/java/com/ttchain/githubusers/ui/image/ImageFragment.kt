package com.ttchain.githubusers.ui.image

import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.ttchain.githubusers.R
import com.ttchain.githubusers.base.BaseFragment
import com.ttchain.githubusers.getBundleValue
import kotlinx.android.synthetic.main.image_fragment.*

class ImageFragment : BaseFragment() {
    private val mUrl by getBundleValue("image", "")

    override val layoutId = R.layout.image_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun initView() {
        if (mUrl.isNotEmpty()) Glide.with(this).load(mUrl).into(photo_view)

        close_btn.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
    }
}