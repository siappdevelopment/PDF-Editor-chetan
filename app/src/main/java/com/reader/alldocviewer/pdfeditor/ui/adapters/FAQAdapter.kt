package com.reader.alldocviewer.pdfeditor.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.ui.model.FAQItem

class FAQAdapter : RecyclerView.Adapter<FAQAdapter.FAQViewHolder>() {

    private var allFaqList = listOf<FAQItem>()
    private var faqList = mutableListOf<FAQItem>()

    var onFilterResult: ((resultCount: Int, query: String) -> Unit)? = null

    fun updateFAQs(newFAQs: List<FAQItem>) {
        allFaqList = newFAQs
        filter("")
    }

    fun filter(query: String) {
        faqList.clear()
        if (query.isBlank()) {
            faqList.addAll(allFaqList)
        } else {
            val lowerQuery = query.lowercase(java.util.Locale.getDefault())
            faqList.addAll(allFaqList.filter {
                it.question.lowercase(java.util.Locale.getDefault()).contains(lowerQuery) ||
                        it.answer.lowercase(java.util.Locale.getDefault()).contains(lowerQuery)
            })
        }
        notifyDataSetChanged()
        onFilterResult?.invoke(faqList.size, query)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_faq, parent, false)
        return FAQViewHolder(view)
    }

    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        holder.bind(faqList[position])
    }

    override fun getItemCount(): Int = faqList.size

    inner class FAQViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQuestion: TextView = itemView.findViewById(R.id.tvQuestion)
        private val tvAnswer: TextView = itemView.findViewById(R.id.tvAnswer)
        private val ivExpand: ImageView = itemView.findViewById(R.id.ivExpand)
        private val lyView: LinearLayout = itemView.findViewById(R.id.lyView)

        fun bind(faq: FAQItem) {
            tvQuestion.text = faq.question
            tvAnswer.text = faq.answer

            lyView.visibility = if (faq.isExpanded) View.VISIBLE else View.GONE

            ivExpand.setImageResource(R.drawable.ic_arrow_down_)
            ivExpand.rotation = if (faq.isExpanded) 180f else 0f

            itemView.setOnClickListener {
                faq.isExpanded = !faq.isExpanded
                notifyItemChanged(adapterPosition)
            }
        }
    }
}
