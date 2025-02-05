package com.example.quizbanktest.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

import com.example.quizbanktest.activity.IntroActivity
import com.example.quizbanktest.models.AccountModel
import com.example.quizbanktest.models.QuestionBankModel
import com.example.quizbanktest.network.AccountService
import com.example.quizbanktest.network.CsrfTokenService
import com.google.gson.Gson
//import com.squareup.okhttp.Headers
//import com.squareup.okhttp.ResponseBody
import okhttp3.Headers
import okhttp3.ResponseBody
import retrofit2.Call
//import retrofit.Callback
//import retrofit.GsonConverterFactory
//import retrofit.Response
//import retrofit.Retrofit
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ConstantsAccountServiceFunction {

    var userAccount : AccountModel ? = null
    fun getCsrfToken(context: Context, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        if (Constants.isNetworkAvailable(context)) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(CsrfTokenService::class.java)
            //TODO 拿到csrf token
            val call = api.getCSRFToken()

            call.enqueue(object : Callback<ResponseBody> {

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response!!.isSuccessful) {
                        val cookies: String? = response.headers().get("Set-Cookie")
                        val cookieHeader: Headers? = response.headers()
                        val cookiesToken: String? = cookieHeader?.get("Set-Cookie")
                        val cookieHeaders = response.headers().values("Set-Cookie")
                        var csrfToken: String? = null
                        var session: String? = null
                        for (cookie in cookieHeaders) {
                            if (cookie.startsWith("CSRF-TOKEN")) {
                                val parts = cookie.split(";").toTypedArray()
                                csrfToken = parts[0].substringAfter("CSRF-TOKEN=").trim()
                            }
                            if (cookie.startsWith("session")) {
                                val parts = cookie.split(";").toTypedArray()
                                session = parts[0].substringAfter("session=").trim()
                            }
                        }
                        Constants.csrfToken = csrfToken!!
                        Constants.session = session!!
                        Constants.cookie =
                            "CSRF-TOKEN=" + Constants.csrfToken + ";" + "session=" + Constants.session
                        onSuccess(Constants.cookie)
                    } else {

                        val sc = response.code()
                        when (sc) {
                            400 -> {
                                Log.e(
                                    "in csrf Error 400", "Bad Re" +
                                            "" +
                                            "quest"
                                )
                            }
                            404 -> {
                                Log.e("in csrf Error 404", "Not Found")
                            }
                            401 -> {
                                val intent = Intent(context, IntroActivity::class.java)
                                context.startActivity(intent)
                            }
                            else -> {
                                Log.e("in csrf Error", "Generic Error")
                            }
                        }
                        onFailure(sc.toString())
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("in csrf Errorrrrr", t?.message.toString())
                    onFailure(t?.message.toString())
                }
            })
        } else {
            Toast.makeText(
                context,
                "No internet connection available.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    fun login(context: Context, email: String, password: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {

        if (Constants.isNetworkAvailable(context)) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(AccountService::class.java)
//            Constants.username = "test"
//            Constants.password = "test"
            val body = AccountService.PostBody(email, password)
//            val body = AccountService.PostBody(Constants.username , Constants.password)
            //TODO 用csrf token 拿access token

            val call = api.login(Constants.cookie, Constants.csrfToken, Constants.session, body)

            call.enqueue(object : Callback<ResponseBody> {


                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response!!.isSuccessful) {
                        val cookieHeader: Headers? = response.headers()

                        Log.e("login in ","call")
                        val cookieHeaders = response.headers().values("Set-Cookie")
                        var accessToken: String? = null
                        var refreshToken: String? = null
                        for (cookie in cookieHeaders) {
                            if (cookie.startsWith("refresh_token_cookie")) {
                                val parts = cookie.split(";").toTypedArray()
                                refreshToken =
                                    parts[0].substringAfter("refresh_token_cookie=").trim()
                            }
                            if (cookie.startsWith("access_token_cookie")) {
                                val parts = cookie.split(";").toTypedArray()
                                accessToken = parts[0].substringAfter("access_token_cookie=").trim()
                            }
                        }
                        val gson = Gson()
                        val accountResponse = gson.fromJson(
                            response.body()?.charStream(),
                            LoginApiResponse::class.java
                        )
                        userAccount = accountResponse.user
                        Log.e("account",accountResponse.toString())
                        Constants.userId = userAccount!!._id
                        Constants.username =  userAccount!!.username
                        Constants.accessToken = accessToken!!
                        Constants.refreshToken = refreshToken!!
                        var cookie: String = Constants.cookie + ";"
                        Constants.COOKIE =
                            cookie + "access_token_cookie=" + Constants.accessToken + ";" + "refresh_token_cookie=" + Constants.refreshToken
                        onSuccess("Ok")

                    } else {

                        val sc = response.code()
                        when (sc) {
                            400 -> {
                                Log.e(
                                    "in login Error 400", "Bad Re" +
                                            "" +
                                            "quest" + response.body()
                                )
                            }
                            404 -> {
                                Log.e("in login Error 404", "Not Found")
                            }
                            401 -> {
                                val intent = Intent(context, IntroActivity::class.java)
                                context.startActivity(intent)
                            }
                            else -> {
                                Log.e("in login Error", "Generic Error")
                            }
                        }
                        onFailure("Request failed with status code $sc")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("in login Errorrrrr", t?.message.toString())
                    onFailure("Request failed with status code ")
                }
            })
        } else {
            Toast.makeText(
                context,
                "No internet connection available.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    fun register(context: Context, userName : String,email: String, password: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {

        if (Constants.isNetworkAvailable(context)) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(AccountService::class.java)
//            Constants.username = "test"
//            Constants.password = "test"
            val currentDate = Date()
            val formatter = SimpleDateFormat("yyyy-M-dd", Locale.getDefault())
            val formattedDate = formatter.format(currentDate)
            val body = AccountService.PostBodyForRegister(userName,email, password,formattedDate.toString())
            Log.e("register Body",body.toString())
            //TODO 用csrf token 拿access token

            val call = api.register(Constants.cookie, Constants.csrfToken, Constants.session, body)

            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>,
                                        response: Response<ResponseBody>) {
                    if (response!!.isSuccessful) {
                        val cookieHeader: Headers? = response.headers()
                        val gson = Gson()
                        val accountResponse = gson.fromJson(
                            response.body()?.charStream(),
                            RegisterApiResponse::class.java
                        )
                        Log.e("register",accountResponse.toString())
                        onSuccess("Ok")
                    } else {

                        val sc = response.code()
                        when (sc) {
                            400 -> {
                                Log.e(
                                    "in register Error 400", "Bad Re" +
                                            "" +
                                            "quest" + response.body()
                                )
                            }
                            404 -> {
                                Log.e("in register Error 404", "Not Found")
                            }
                            401 -> {
                                val intent = Intent(context, IntroActivity::class.java)
                                context.startActivity(intent)
                            }
                            else -> {
                                Log.e("in register Error", "Generic Error")
                            }
                        }
                        onFailure("Request failed with status code $sc")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("in register Errorrrrr", t?.message.toString())
                    onFailure("Request failed with status code ")
                }
            })
        } else {
            Toast.makeText(
                context,
                "No internet connection available.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    fun forgetPwd(context: Context, email: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {

        if (Constants.isNetworkAvailable(context)) {
            val client = ConstantsFunction.createOkHttpClient()
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
            val api = retrofit.create(AccountService::class.java)
//            Constants.username = "test"
//            Constants.password = "test"
            val body = AccountService.ForgetBody(email)
//            val body = AccountService.PostBody(Constants.username , Constants.password)
            //TODO 用csrf token 拿access token

            val call = api.forgotPassword(Constants.cookie, Constants.csrfToken, Constants.session, body)

            call.enqueue(object : Callback<ResponseBody> {


                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response!!.isSuccessful) {
                        Toast.makeText(context,"驗證信已發出請去信箱重設密碼",Toast.LENGTH_SHORT).show()
                        onSuccess(response.body().toString())
                    } else {

                        val sc = response.code()
                        when (sc) {
                            400 -> {
                                Log.e(
                                    "in forget Error 400", "Bad Re" +
                                            "" +
                                            "quest" + response.body()
                                )
                            }
                            404 -> {
                                Log.e("in forget Error 404", "user Not Found")
                            }
                            401 -> {
                                val intent = Intent(context, IntroActivity::class.java)
                                context.startActivity(intent)
                            }
                            else -> {
                                Log.e("in forget Error", "Generic Error")
                            }
                        }
                        onFailure("Request failed with status code $sc")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("in forget Errorrrrr", t?.message.toString())
                    onFailure("Request failed with status code ")
                }
            })
        } else {
            Toast.makeText(
                context,
                "No internet connection available.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    fun logout(context: Context) {
        if (Constants.isNetworkAvailable(context)) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(AccountService::class.java)

            //TODO 拿到csrf token access token
            Log.e("access in scan ", Constants.accessToken)
            Log.e("COOKIE in scan ", Constants.COOKIE)
            val call = api.logout(Constants.COOKIE, Constants.csrfToken, Constants.accessToken)

            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>,
                                        response: Response<ResponseBody>) {
                    if (response!!.isSuccessful) {
                        // 清空目前記錄的登入屬性
                        Constants.cookie = ""
                        Constants.COOKIE = ""
                        Constants.csrfToken = ""
                        Constants.accessToken = ""
                        Constants.session = ""
                        Constants.refreshToken = ""
                        Constants.username = ""
                        Constants.password = ""

                        Log.e("Response Result", "log out success")
                        val intent = Intent(context, IntroActivity::class.java)
                        context.startActivity(intent)

                    } else {

                        val sc = response.code()
                        when (sc) {
                            400 -> {
                                Log.e(
                                    "Error 400", "Bad Re" +
                                            "" +
                                            "quest"
                                )
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }
                            401 -> {
                                val intent = Intent(context, IntroActivity::class.java)
                                context.startActivity(intent)
                            }
                            else -> {
                                Log.e("Error", "in log out  Generic Error")
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("in log out Errorrrrr", t?.message.toString())
                }
            })
        } else {
            Toast.makeText(
                context,
                "No internet connection available.",
                Toast.LENGTH_SHORT
            ).show()
        }

    }
    data class LoginApiResponse(val message: String, val status: Int, val user: AccountModel)
    data class RegisterApiResponse(val message: String, val status: Int)
}