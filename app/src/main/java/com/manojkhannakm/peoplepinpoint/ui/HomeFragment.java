package com.manojkhannakm.peoplepinpoint.ui;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.manojkhannakm.peoplepinpoint.Constants;
import com.manojkhannakm.peoplepinpoint.R;
import com.manojkhannakm.peoplepinpoint.service.LocationService;
import com.manojkhannakm.peoplepinpoint.ui.widget.SlidingTabLayout;

import java.lang.ref.WeakReference;

/**
 * @author Manoj Khanna
 */

public class HomeFragment extends Fragment {

    private static final int VIEW_PAGER_PAGE_COUNT = 3;
    private static final int[] VIEW_PAGER_PAGE_TITLE_RES_IDS = new int[]{
            R.string.people_page_title_home,
            R.string.search_page_title_home,
            R.string.map_page_title_home
    };

    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mViewPager = (ViewPager) view.findViewById(R.id.view_pager_home);
        mViewPager.setOffscreenPageLimit(2);

        mViewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);

        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tab_layout_home);
        slidingTabLayout.setCustomTabView(R.layout.sliding_tab_view_home, R.id.sliding_tab_view_home);
        slidingTabLayout.setSelectedIndicatorColors(Color.WHITE);
        slidingTabLayout.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                getActivity().supportInvalidateOptionsMenu();

                Fragment fragment = mViewPagerAdapter.getFragment(position);
                if (fragment != null && fragment instanceof OnPageSelectedListener) {
                    ((OnPageSelectedListener) fragment).onPageSelected();
                }
            }

        });
        slidingTabLayout.setViewPager(mViewPager);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_home, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.account_item_home:
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter_left, R.anim.fragment_exit_left,
                                R.anim.fragment_enter_right, R.anim.fragment_exit_right)
                        .replace(R.id.fragment_container_main, AccountFragment.newInstance())
                        .addToBackStack(null)
                        .commit();
                return true;

            case R.id.sign_out_item_home:
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter_left, R.anim.fragment_exit_left,
                                R.anim.fragment_enter_right, R.anim.fragment_exit_right)
                        .replace(R.id.fragment_container_main, SignInFragment.newInstance())
                        .commit();

                MainActivity activity = (MainActivity) getActivity();

                activity.stopService(new Intent(activity, LocationService.class));

                activity.setLocalPersonEntity(null);

                SQLiteDatabase database = activity.openOrCreateDatabase(Constants.DATABASE, Context.MODE_PRIVATE, null);

                database.delete(Constants.TABLE_PERSON, null, null);

                database.delete(Constants.TABLE_PEOPLE, null, null);

                database.close();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public ViewPagerAdapter getViewPagerAdapter() {
        return mViewPagerAdapter;
    }

    public interface OnPageSelectedListener {

        public void onPageSelected();

    }

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private SparseArray<WeakReference<Fragment>> mFragmentSparseArray = new SparseArray<>();

        private ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);

            mFragmentSparseArray.put(position, new WeakReference<>(fragment));

            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mFragmentSparseArray.remove(position);

            super.destroyItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {

                case 0:
                    return PeopleFragment.newInstance();

                case 1:
                    return SearchFragment.newInstance();

                case 2:
                    return MapFragment.newInstance();

            }

            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(VIEW_PAGER_PAGE_TITLE_RES_IDS[position]);
        }

        @Override
        public int getCount() {
            return VIEW_PAGER_PAGE_COUNT;
        }

        public Fragment getFragment(int position) {
            WeakReference<Fragment> weakReference = mFragmentSparseArray.get(position);
            return weakReference != null ? weakReference.get() : null;
        }

    }

}
