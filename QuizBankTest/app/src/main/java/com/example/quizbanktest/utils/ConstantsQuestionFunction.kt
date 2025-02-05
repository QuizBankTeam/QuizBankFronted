package com.example.quizbanktest.utils


import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.introducemyself.utils.ConstantsOcrResults
import com.example.quizbanktest.activity.IntroActivity
import com.example.quizbanktest.models.QuestionModel
import com.example.quizbanktest.models.QuestionSetModel
import com.example.quizbanktest.models.ScanQuestionModel
import com.example.quizbanktest.network.QuestionBankService
import com.example.quizbanktest.network.QuestionService
import com.google.gson.Gson
import okhttp3.ResponseBody
//import com.squareup.okhttp.Request
//import com.squareup.okhttp.ResponseBody
import okio.Buffer
import okio.BufferedSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
//import retrofit.Callback
//import retrofit.GsonConverterFactory
//import retrofit.Response
//import retrofit.Retrofit
import java.nio.charset.Charset

object ConstantsQuestionFunction {
    var allQuestionsReturnResponse : bankInnerQuestion?= null
    var questionList : ArrayList<QuestionModel> = ArrayList()
    var postQuestionPosition : Int = 0
    fun postQuestion(question : ScanQuestionModel, activity: AppCompatActivity, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val client = ConstantsFunction.createOkHttpClient()
        if (Constants.isNetworkAvailable(activity)) {
            Log.e("question post",question.toString())
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
            val api = retrofit.create(QuestionService::class.java)
//            data class PostQuestionBody(val title: String,val number: String,val description: String,val options: ArrayList<String>,val questionType:String,val bankType:String,val questionBank:String,val answerOptions:ArrayList<String>,val answerDescription:String,val provider:String,val originateFrom:String,val createdDate:String,val image : String,val tag:ArrayList<String>)

            val body = QuestionService.PostQuestionBody(question.title!!,question.number!!,question.description,question.options!!,question.questionType!!,question.bankType!!,question.questionBank!!,question.answerOptions!!,question.answerDescription!!,ConstantsAccountServiceFunction.userAccount!!._id,question.originateFrom!!,question.createdDate!!,question.image!!,question.answerImages!!,question.tag!!)

            //TODO 拿到csrf token access token
            Log.e("access in scan ", Constants.accessToken)
            Log.e("COOKIE in scan ", Constants.COOKIE)
            val call = api.postQuestion(
                Constants.COOKIE,
                Constants.csrfToken,
                Constants.accessToken,
                Constants.refreshToken,
                body
            )

            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>,
                                        response: Response<ResponseBody>) {
                    if (response!!.isSuccessful) {
//                        Log.e("Response Result","success post question")
                        Toast.makeText(activity,"upload successfully",Toast.LENGTH_SHORT).show()
                        ConstantsOcrResults.questionList.removeAt(postQuestionPosition)

                        onSuccess("upload ok")
                    } else {

                        val sc = response.code()
                        when (sc) {
                            400 -> {
                                Log.e(
                                    "Error 400", "Bad Re" +
                                            "" +
                                            "quest"+response.toString()
                                )
                                Toast.makeText(activity,"Error 400",Toast.LENGTH_SHORT).show()
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                                Toast.makeText(activity,"Not Found 400",Toast.LENGTH_SHORT).show()
                            }
                            401 -> {
                                val intent = Intent(activity, IntroActivity::class.java)
                                activity.startActivity(intent)
                            }
                            else -> {
                                Log.e("Error", "in post question Error")
                                Toast.makeText(activity,"error",Toast.LENGTH_SHORT).show()
                            }
                        }
                        onFailure("bad request")
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onFailure("bad request")
                    Log.e("in post question Errorrrrr", t?.message.toString())
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
    fun getQuestion(context: Context, id: String, onSuccess: (ArrayList<QuestionModel>) -> Unit, onFailure: (String) -> Unit) {
        if (Constants.isNetworkAvailable(context)) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(QuestionBankService::class.java)
            val call = api.getQuestionBankByID(
                Constants.COOKIE,
                Constants.csrfToken,
                Constants.session,
                id
            )
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>,
                                        response: Response<ResponseBody>) {
                    if (response!!.isSuccessful) {
//                        // TODO
                        val source: BufferedSource = response.body()!!.source()
                        source.request(Long.MAX_VALUE) // Buffer the entire body.

                        val buffer: Buffer = source.buffer()
                        val UTF8: Charset = Charset.forName("UTF-8")
//                        Log.d("REQUEST_JSON", buffer.clone().readString(UTF8))
                        val gson = Gson()
                        val allQuestionsResponse = gson.fromJson(
                            response.body()!!.charStream(),
                            bankInnerQuestion::class.java
                        )
                        allQuestionsReturnResponse = allQuestionsResponse
//                        Log.d("All questions response", allQuestionsReturnResponse.toString())
                        questionList = allQuestionsResponse.questionBank.questions
//                        Log.e("ConstantsQuestionFunction: Question Response Result:", questionList.toString())
                        onSuccess(questionList)
                    } else {
                        val sc = response.code()
                        when (sc) {
                            400 -> {
                                Log.e("Error 400", "Bad Request: $response")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }
                            else -> {
                                Log.e("Error", "in get all banks Generic Error")
                            }
                        }
                        Log.e("get question error",sc.toString())
                        onFailure("Request failed with status code $sc")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onFailure("Request failed with status code ")
                    Log.e("in get all questions Error", t?.message.toString())
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

    fun putQuestion(activity: AppCompatActivity, question: QuestionModel, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        if (Constants.isNetworkAvailable(activity)) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(QuestionService::class.java)

            Log.e("ConstantsQuestionFunction", "put question")
            if (question.answerImage == null || question.answerImage!!.isEmpty()) {
                val answerImage : ArrayList<String> = ArrayList()
                question.answerImage = answerImage
            }
            val body = QuestionService.PutQuestionBody(question._id, question.title, question.number, question.description, question.options, question.questionType, question.bankType, question.answerOptions, question.answerDescription, question.questionImage,
                question.answerImage!!, question.tag)
            //TODO 拿到csrf token access token
            val call = api.updateQuestion(
                Constants.COOKIE,
                Constants.csrfToken,
                Constants.session,
                Constants.refreshToken,
                body
            )
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>,
                                        response: Response<ResponseBody>) {
                    if (response!!.isSuccessful) {

                        onSuccess("upload success")
                    } else {
                        val sc = response.code()
                        when (sc) {
                            400 -> {
                                Log.e("Error 400", "Bad Request: $response")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }
                            else -> {
                                Log.e("Error", "upload failed")
                            }
                        }
                        Log.e("put question error",sc.toString())
                        onFailure("Request failed with status code $sc")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                    onFailure("Request failed with status code ")
                    Log.e("in get all questions Error", t?.message.toString())
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

    fun deleteQuestion(activity: AppCompatActivity, questionId: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        if (Constants.isNetworkAvailable(activity)) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(QuestionService::class.java)

           //TODO 拿到csrf token access token
            val call = api.deleteQuestionByID(
                Constants.COOKIE,
                Constants.csrfToken,
                Constants.session,
                Constants.refreshToken,
                questionId
            )
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>,
                                        response: Response<ResponseBody>) {
                    if (response!!.isSuccessful) {

                        onSuccess("delete success")
                    } else {
                        val sc = response.code()
                        when (sc) {
                            400 -> {
                                Log.e("Error 400", "Bad Request: $response")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }
                            else -> {
                                Log.e("Error", "upload failed")
                            }
                        }
                        Log.e("delete question error",sc.toString())
                        onFailure("Request failed with status code $sc")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                    onFailure("Request failed with status code ")
                    Log.e("in get all questions Error", t?.message.toString())
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

    fun moveQuestion(activity: AppCompatActivity, questionId: String, newBankId: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        if (Constants.isNetworkAvailable(activity)) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(QuestionService::class.java)

            val body = QuestionService.MoveQuestionBody(questionId, newBankId)
            val call = api.moveQuestion(
                Constants.COOKIE,
                Constants.csrfToken,
                Constants.session,
                Constants.refreshToken,
                body
            )
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response!!.isSuccessful) {
                        onSuccess("move success")
                    } else {
                        val sc = response.code()
                        when (sc) {
                            400 -> {
                                Log.e("Error 400", "Bad Request: $response")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }
                            else -> {
                                Log.e("Error", "upload failed")
                            }
                        }
                        Log.e("move question error", sc.toString())
                        onFailure("Request failed with status code $sc")
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onFailure("Request failed with status code ")
                    Log.e("in get all questions Error", t.message.toString())
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


    data class AllQuestionsResponse(val questionBank : ArrayList<QuestionModel>)
    data class bankInnerQuestion(val questionBank:QuestionAndBank)

    data class QuestionAndBank(
        val _id: String,
        val title: String,
        val questionBankType: String,
        val createdDate: String,
        val members : ArrayList<String>,
        val originateFrom : String,
        val creator:String,
        val questionSets:ArrayList<QuestionSetModel>,
        val questions:ArrayList<QuestionModel>
    )
}