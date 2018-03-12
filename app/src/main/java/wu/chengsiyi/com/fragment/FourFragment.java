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

public class FourFragment extends BaseFragment {
    protected View rootView;
    private Unbinder mUnbinder;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(rootView==null){
            rootView = inflater.inflate(R.layout.fragment_four, container, false);
            mUnbinder = ButterKnife.bind(this, rootView);
        }

        return rootView;
    }

    @Override
    protected void onFragmentVisibleChange(boolean isVisible) {
        if (isVisible) {
            //更新界面数据，如果数据还在下载中，就显示加载框
            Log.d("lazyTest", " F4 可见 : ");
        } else {
            //关闭加载框
            Log.d("lazyTest", "  F4 不可见:  ");

        }
    }

    @Override
    protected void onFragmentFirstVisible() {
        //去服务器下载数据
        Log.d("lazyTest", "  F4 去服务器下载数据 : ");

    }
}
