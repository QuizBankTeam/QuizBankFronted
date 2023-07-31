package com.example.quizbanktest.utils

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.introducemyself.utils.ConstantsOcrResults
import com.example.quizbanktest.activity.BaseActivity
import com.example.quizbanktest.activity.scan.ScannerTextWorkSpaceActivity
import com.example.quizbanktest.network.ScanImageService
import com.google.gson.Gson
import com.squareup.okhttp.ResponseBody
import retrofit.Callback
import retrofit.GsonConverterFactory
import retrofit.Response
import retrofit.Retrofit

object ConstantsScanServiceFunction {
    fun scanBase64ToOcrText(base64String: String, activity:BaseActivity) {
        if (Constants.isNetworkAvailable(activity)) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(ScanImageService::class.java)
            val body = ScanImageService.PostBody(base64String)

            //TODO 拿到csrf token access token
            Log.e("access in scan ", Constants.accessToken)
            Log.e("COOKIE in scan ", Constants.COOKIE)
            val call = api.scanBase64(
                Constants.COOKIE,
                Constants.csrfToken,
                Constants.accessToken,
                Constants.refreshToken,
                body
            )

            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(response: Response<ResponseBody>?, retrofit: Retrofit?) {
                    if (response!!.isSuccess) {
                        val gson = Gson()
                        val ocrResponse = gson.fromJson(
                            response.body().charStream(),
                            OCRResponse::class.java
                        )
                        Log.e("Response Result", ocrResponse.text)
                        ConstantsOcrResults.setOcrResult(ocrResponse.text)
                        activity.hideProgressDialog()
                        val intent = Intent(activity, ScannerTextWorkSpaceActivity::class.java)
                        intent.putExtra("ocrText", ocrResponse.text)
                        activity.startActivity(intent)

                    } else {

                        val sc = response.code()
                        activity.hideProgressDialog()

                        when (sc) {
                            400 -> {
                                activity.showErrorSnackBar("發生了錯誤(BAD REQUEST)")
                                Log.e(
                                    "Error 400", "Bad Re" +
                                            "" +
                                            "quest"
                                )
                            }
                            404 -> {
                                activity.showErrorSnackBar("系統找不到")
                                Log.e("Error 404", "Not Found")
                            }
                            else -> {
                                Log.e("Error", "in scan Generic Error")
                            }
                        }
                    }
                }

                override fun onFailure(t: Throwable?) {
                    activity.showErrorSnackBar("掃描發生錯誤")
                    Log.e("in scan Errorrrrr", t?.message.toString())
                }
            })
        } else {

            Toast.makeText(
                activity,
                "No internet connection available.",
                Toast.LENGTH_SHORT
            ).show()
        }

    }
    data class OCRResponse(val text: String)

}