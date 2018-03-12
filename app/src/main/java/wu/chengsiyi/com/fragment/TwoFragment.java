package wu.chengsiyi.com.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import wu.chengsiyi.com.base.BaseFragment;
import wu.chengsiyi.com.newsxi.R;

/**
 * Created by ${Wu} on 2018/2/25.
 */

public class TwoFragment extends BaseFragment {

    protected View rootView;

    private Unbinder mUnbinder;
    private int count;//记录开启进度条的情况 只能开一个
    //当前Fragment是否处于可见状态标志，防止因ViewPager的缓存机制而导致回调函数的触发
    private boolean isFragmentVisible;
    //是否是第一次开启网络加载
    public boolean isFirst;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null)
            rootView = inflater.inflate(R.layout.fragment_two, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);


        return rootView;
    }

    @Override
    protected void onFragmentVisibleChange(boolean isVisible) {
        if (isVisible) {
            //更新界面数据，如果数据还在下载中，就显示加载框
            Log.d("lazyTest", " F2 可见 : ");
        } else {
            //关闭加载框
            Log.d("lazyTest", "  F2 不可见:  ");

        }
    }

    @Override
    protected void onFragmentFirstVisible() {
        //去服务器下载数据
        Log.d("lazyTest", "  F2 去服务器下载数据 : ");

    }
}
