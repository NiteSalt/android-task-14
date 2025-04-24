package g323.saveliev.laba14

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView


class UsersAdapter(private val context: Context) : BaseAdapter() {
    private var inflater: LayoutInflater? = null

    private var users: ArrayList<User> = ArrayList<User>()

    init {
        inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return users.size
    }

    override fun getItem(index: Int): Any {
        return getUser(index)
    }

    fun getUser(index: Int): User {
        return users[index]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        var vi: View? = p1
        if (vi == null) vi = inflater!!.inflate(R.layout.users, null)
        val ipText = vi!!.findViewById<View>(R.id.ipAddress) as TextView
        val usernameText = vi.findViewById<View>(R.id.username) as TextView
        ipText.text = users[p0].ipAddress
        usernameText.text = users[p0].username
        return vi
    }

    fun fill(array: Array<User?>) {
        users.clear()

        for (x in array) {
            users.add(x!!)
        }

        notifyDataSetChanged()
    }
}