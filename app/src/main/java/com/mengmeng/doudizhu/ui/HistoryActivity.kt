package com.mengmeng.doudizhu.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mengmeng.doudizhu.R
import com.mengmeng.doudizhu.api.ApiClient
import com.mengmeng.doudizhu.model.HistoryRecord

class HistoryActivity : AppCompatActivity() {

    private lateinit var api: ApiClient
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private val records = mutableListOf<HistoryRecord>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        api = ApiClient.getInstance(this)

        recyclerView = findViewById(R.id.historyRecyclerView)
        emptyView = findViewById(R.id.emptyView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = HistoryAdapter(records)

        loadHistory()
    }

    private fun loadHistory() {
        api.getHistory(1) { response, error ->
            runOnUiThread {
                if (error != null) {
                    Toast.makeText(this, "网络错误: ${error.message}", Toast.LENGTH_SHORT).show()
                    emptyView.visibility = View.VISIBLE
                    return@runOnUiThread
                }
                if (response?.ok == true && response.data?.list != null) {
                    records.clear()
                    records.addAll(response.data.list!!)
                    recyclerView.adapter?.notifyDataSetChanged()
                    emptyView.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    emptyView.visibility = View.VISIBLE
                    emptyView.text = response?.message ?: "暂无战绩"
                }
            }
        }
    }

    inner class HistoryAdapter(private val items: List<HistoryRecord>) :
        RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val modeText: TextView = view.findViewById(R.id.historyMode)
            val resultText: TextView = view.findViewById(R.id.historyResult)
            val detailText: TextView = view.findViewById(R.id.historyDetail)
            val coinText: TextView = view.findViewById(R.id.historyCoin)
            val dateText: TextView = view.findViewById(R.id.historyDate)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.modeText.text = if (item.mode == "practice") "练习模式" else "联网对战"
            holder.resultText.text = if (item.result == "win") "胜利" else "失败"
            holder.resultText.setTextColor(if (item.result == "win") 0xFF4CAF50.toInt() else 0xFFF44336.toInt())
            holder.detailText.text = "地主: ${item.landlordName ?: "未知"} | 底分: ${item.baseScore} x ${item.multiplier}"
            holder.coinText.text = "${if (item.coinChange >= 0) "+" else ""}${item.coinChange}"
            holder.coinText.setTextColor(if (item.coinChange >= 0) 0xFF4CAF50.toInt() else 0xFFF44336.toInt())
            holder.dateText.text = item.finishedAt ?: item.createdAt ?: ""
        }

        override fun getItemCount() = items.size
    }
}
