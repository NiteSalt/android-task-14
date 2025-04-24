package g323.saveliev.laba14

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter() : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>()  {
    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val timeText: TextView = itemView.findViewById(R.id.timeText)
    }

    private val messages: ArrayList<Message> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == 0) {
            R.layout.item_sent_message // Layout for sent messages
        } else {
            R.layout.item_received_message // Layout for received messages
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.content
        holder.timeText.text = android.text.format.DateFormat.format("HH:mm:ss dd/mm/YY", message.date)
    }
}