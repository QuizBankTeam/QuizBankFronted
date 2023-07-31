package com.example.quizbanktest.activity.group

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizbanktest.R
import com.example.quizbanktest.activity.BaseActivity
import com.example.quizbanktest.activity.bank.BankActivity
import com.example.quizbanktest.adapters.group.GroupListAdapter
import com.example.quizbanktest.adapters.main.RecentViewAdapter
import com.example.quizbanktest.models.GroupModel
import com.example.quizbanktest.models.QuestionBankModel
import com.example.quizbanktest.utils.ConstantsGroup

class GroupListActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_list)

        setupNavigationView()
        doubleCheckExit()
        val backArrowBtn:ImageButton = findViewById(R.id.btn_group_back_arrow)

        backArrowBtn.setOnClickListener {
            val intent = Intent(this,BankActivity::class.java)
            startActivity(intent)
            finish()
        }
        setupGroupListRecyclerView(ConstantsGroup.groupList)
    }

    private fun setupGroupListRecyclerView(groupList: ArrayList<GroupModel>) {
        val groupListView : androidx.recyclerview.widget.RecyclerView = findViewById(R.id.groupListRecyclerView)
        groupListView.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL,false)
        val placesAdapter = GroupListAdapter(this, groupList)
        groupListView.adapter = placesAdapter

    }
}