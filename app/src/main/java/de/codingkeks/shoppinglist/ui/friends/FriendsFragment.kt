package de.codingkeks.shoppinglist.ui.friends

import android.app.AlertDialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import de.codingkeks.shoppinglist.R
import kotlinx.android.synthetic.main.fragment_friends.*

class FriendsFragment : Fragment() {

    lateinit var myFragment: View
    lateinit var viewPager: ViewPager
    lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myFragment = inflater.inflate(R.layout.fragment_friends, container, false)

        viewPager = myFragment.findViewById(R.id.viewPagerFriends)
        tabLayout = myFragment.findViewById(R.id.tabLayoutFriends)

        return myFragment
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setUpViewPager(viewPager)
        tabLayout.setupWithViewPager(viewPager)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        //add a new friend
        fabAddFriend.setOnClickListener {
            val alertBuilder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.AlertDialogTheme2))
            alertBuilder.setTitle(getString(R.string.addFriendDialogTitle))
        }
    }

    private fun setUpViewPager(viewPager: ViewPager) {
        var adapter: FragmentPagerAdapterFriends = FragmentPagerAdapterFriends(childFragmentManager)

        adapter.addFragment(FriendsAddedFragment(), getString(R.string.friends_added_tab_title))
        adapter.addFragment(
            FriendsRequestsFragment(),
            getString(R.string.friends_requests_tab_title)
        )

        viewPager.adapter = adapter
    }
}