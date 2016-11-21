package com.utnapp.instafood.Activities;

/**
 * Created by Flor on 21/11/2016.
 */
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
//import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

public class ScreenSlideActivity extends FragmentActivity{

/**
 private static final int NUM_PAGES = arrayGrid.length(cuando sepa cual es el array del grid);
 private ViewPager mPager;
 private PagerAdapter mPagerAdapter;

 @Override
 protected void onCreate(Bundle savedInstanceState) {
 super.onCreate(savedInstanceState);
 setContentView(R.layout.activity_screen_slide);

 mPager = (ViewPager) findViewById(R.id.pager);
 mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
 mPager.setAdapter(mPagerAdapter);
 mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 @Override
 public void onPageSelected(int position) {
 invalidateOptionsMenu();
 }
 });
 }

 //Tenia un menu desde donde con un click abria las imagenes
 @Override
 public boolean onCreateOptionsMenu(Menu menu) {
 super.onCreateOptionsMenu(menu);
 getMenuInflater().inflate(R.menu.activity_screen_slide, menu);
 return true;
 }

 @Override
 public boolean onOptionsItemSelected(MenuItem item) {
 switch (item.getItemId()) {
 case android.R.id.home:

 NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
 return true;
 }

 return super.onOptionsItemSelected(item);
 }

 private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
 public ScreenSlidePagerAdapter(FragmentManager fm) {
 super(fm);
 }

 @Override
 public Fragment getItem(int position) {
 return ScreenSlidePageFragment.create(position);
 }

 @Override
 public int getCount() {
 return NUM_PAGES;
 }
 }
 */
}
