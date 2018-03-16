package wu.chengsiyi.com.base;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import wu.chengsiyi.com.newsxi.R;

/**
 * Created by Administrator on 2018/3/15 0015.
 */

public abstract class BaseActivity extends AppCompatActivity {
    private LayoutInflater inflater;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setLayout());

        initToolBar();                         //         初始化toolbar，

        initData();                           //          初始化数据

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;

        Window window = this.getWindow();

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setStatusBarColor(0xff3CBFFA);



    }

    @Override
    protected void onStart() {
        super.onStart();
        if(null != getToolbar() && isShowBacking()){
            showBack();
        }
    }

    protected abstract void initData();

    private void initView() {

    }



    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            //将Toolbar显示到界面
            mToolbar.setTitle("");
            setSupportActionBar(mToolbar);
        }
    }

    protected abstract int setLayout();

    public Toolbar getToolbar() {
        return (Toolbar) findViewById(R.id.toolbar);
    }

//    2,设置标题
    public void setToolBarTitle(CharSequence title) {
        getToolbar().setTitle(title);
        setSupportActionBar(getToolbar());
    }

//    3,是否显示后退键
    protected boolean isShowBacking(){
        return true;
    }

//    4,后退方法
   private void showBack(){
        getToolbar().setNavigationIcon(R.drawable.btn_fanhui);
        getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

}
