package me.jcala.xmarket.mvp.main;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.facebook.drawee.view.SimpleDraweeView;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.jcala.xmarket.AppConf;
import me.jcala.xmarket.R;
import me.jcala.xmarket.data.pojo.User;
import me.jcala.xmarket.data.storage.UserIntermediate;
import me.jcala.xmarket.mvp.a_base.BaseActivity;
import me.jcala.xmarket.mvp.message.MessageFragment;
import me.jcala.xmarket.mvp.message.MessageService;
import me.jcala.xmarket.mvp.school.SchoolFragment;
import me.jcala.xmarket.mvp.sort.TradeTagFragment;
import me.jcala.xmarket.mvp.team.TeamFragment;
import me.jcala.xmarket.mvp.user.login.LoginRegisterActivity;
import me.jcala.xmarket.mvp.user.team.UserTeamActivity;
import me.jcala.xmarket.mvp.user.trades.bought.TradeBoughtActivity;
import me.jcala.xmarket.mvp.user.trades.donate.TradeDonateActivity;
import me.jcala.xmarket.mvp.user.trades.sell.TradeSellActivity;
import me.jcala.xmarket.mvp.user.trades.sold.TradeSoldActivity;
import me.jcala.xmarket.mvp.user.trades.uncomplete.TradeUnCompleteActivity;
import me.jcala.xmarket.util.PollingUtils;

public class MainActivity  extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.bottom_navigation_bar)
    BottomNavigationBar mBottomNavigationBar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.search_view)
    MaterialSearchView searchView;
    private TextView username;
    private TextView phone;
    private SimpleDraweeView avatar;
    private BottomNavigationItem messageItem=new BottomNavigationItem(R.mipmap.menu_message, "消息");
    private TeamFragment teamFragment;
    private FragmentManager fm;
    private TradeTagFragment tradeTagFragment;
    private SchoolFragment schoolFragment;
    private MessageFragment messageFragment;
    private MainPresenter presenter;
    public static String ACTION_UPDATE_UI = "main.update.ui";
    private BroadcastReceiver receiver;

    @Override
    protected void initViews(Bundle savedInstanceState) {
        setContentView(R.layout.main_activity);
        initService();
        ButterKnife.bind(this);
        presenter=new MainPresenterImpl(this);
        fm = getFragmentManager();
        initSlide();
        initBottomMenu();
    }

    private void initService(){
        PollingUtils.startPollingService(this, 5, MessageService.class, MessageService.ACTION);
        IntentFilter filter = new IntentFilter(ACTION_UPDATE_UI);
        receiver=new MainBroadcastReceiver();
        registerReceiver(receiver, filter);
    }

    private void initSlide(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        View headerLayout= navigationView.inflateHeaderView(R.layout.main_slide);
        username=(TextView) headerLayout.findViewById(R.id.info_username);
        phone=(TextView) headerLayout.findViewById(R.id.info_phone);
        avatar=(SimpleDraweeView) headerLayout.findViewById(R.id.info_avatar);
        navigationView.setNavigationItemSelectedListener(this);
        initSlideHeader();
        initSearchView();
    }
    private void initSearchView(){
        searchView.setVoiceSearch(false);
        searchView.setCursorDrawable(R.drawable.color_cursor_white);
        searchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Snackbar.make(findViewById(R.id.toolbar_container), "Query: " + query, Snackbar.LENGTH_LONG)
                        .show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });

    }
    private void initSlideHeader(){
        User user= UserIntermediate.instance.getUser(MainActivity.this);
        username.setText(user.getUsername());
        phone.setText(user.getPhone());
        avatar.setImageURI(Uri.parse(AppConf.BASE_URL+user.getAvatarUrl()));
    }
    private void initBottomMenu(){
        mBottomNavigationBar.setMode(BottomNavigationBar.MODE_SHIFTING);
        mBottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC)
                .addItem(new BottomNavigationItem(R.mipmap.menu_school, "本校").setActiveColorResource(R.color.black))
                .addItem(new BottomNavigationItem(R.mipmap.menu_sort, "分类").setActiveColorResource(R.color.black))
                .addItem(new BottomNavigationItem(R.mipmap.menu_team, "志愿队").setActiveColorResource(R.color.black))
                .addItem(messageItem.setActiveColorResource(R.color.black).setInActiveColor(R.color.red))
                .setFirstSelectedPosition(0)
                .initialise();
        mBottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                switch (position) {
                    case 0:
                        toolbarTitle.setText(R.string.MainActivity_title_school);
                        showFragment(0);
                        break;
                    case 1:
                        toolbarTitle.setText(R.string.MainActivity_title_sort);
                        showFragment(1);
                        break;
                    case 2:
                        toolbarTitle.setText(R.string.MainActivity_title_team);
                        showFragment(2);
                        break;
                    case 3:
                        toolbarTitle.setText(R.string.MainActivity_title_message);
                        showFragment(3);
                        break;
                }

            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {

            }
        });
        toolbarTitle.setText(R.string.MainActivity_title_school);
        showFragment(0);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
           case R.id.info_uncomplete:
               Intent unCompleteIntent=new Intent(MainActivity.this, TradeUnCompleteActivity.class);
               startActivity(unCompleteIntent);
               break;

            case R.id.info_sell:
                Intent sellIntent=new Intent(MainActivity.this, TradeSellActivity.class);
                startActivity(sellIntent);
                break;

            case R.id.info_bought:
                Intent boughtIntent=new Intent(MainActivity.this, TradeBoughtActivity.class);
                startActivity(boughtIntent);
                break;

            case R.id.info_sold:
                Intent soldIntent=new Intent(MainActivity.this, TradeSoldActivity.class);
                startActivity(soldIntent);
                break;

            case R.id.info_donation:
                Intent donateIntent=new Intent(MainActivity.this, TradeDonateActivity.class);
                startActivity(donateIntent);
                break;

            case R.id.info_team:
                Intent teamIntent=new Intent(MainActivity.this, UserTeamActivity.class);
                startActivity(teamIntent);
                break;

            case R.id.info_logout:
                UserIntermediate.instance.logOut(MainActivity.this);
                Intent loginIntent=new Intent(MainActivity.this, LoginRegisterActivity.class);
                startActivity(loginIntent);
                finish();
                break;
            case R.id.info_author:
                Uri uri=Uri.parse("https://github.com/jcalaz");
                Intent uriIntent=new Intent(Intent.ACTION_VIEW,uri);
                startActivity(uriIntent);
                break;
            default:break;
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_top, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showFragment(int position) {
        FragmentTransaction ft = fm.beginTransaction();
        hideAllFragment(ft);
        switch (position) {
            case 0 : if (schoolFragment != null) {
                ft.show(schoolFragment);
            } else {
                schoolFragment = new SchoolFragment();
                ft.add(R.id.frame_layout, schoolFragment);
            }
                break;
            case 1 : if (tradeTagFragment != null) {
                ft.show(tradeTagFragment);
            } else {
                tradeTagFragment = new TradeTagFragment();
                ft.add(R.id.frame_layout, tradeTagFragment);
            }
                break;
            case 2 : if (teamFragment != null) {
                        ft.show(teamFragment);
                    } else {
                teamFragment = new TeamFragment();
                        ft.add(R.id.frame_layout, teamFragment);
                    }
                break;
            case 3 : if (messageFragment != null) {
                ft.show(messageFragment);
            } else {
                messageFragment = new MessageFragment();
                ft.add(R.id.frame_layout, messageFragment);
            }
                break;
        }
        ft.commit();
    }

    private void hideAllFragment(FragmentTransaction ft) {
        if (schoolFragment != null) {
            ft.hide(schoolFragment);
        }
        if (tradeTagFragment != null) {
            ft.hide(tradeTagFragment);
        }
        if (teamFragment != null) {
            ft.hide(teamFragment);
        }
        if (messageFragment != null) {
            ft.hide(messageFragment);
        }
    }
    public class MainBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AppConf.useMock){
                return;
            }
            messageItem.setInActiveColor(R.color.red);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        PollingUtils.stopPollingService(this, MessageService.class, MessageService.ACTION);
        unregisterReceiver(receiver);
    }
}
