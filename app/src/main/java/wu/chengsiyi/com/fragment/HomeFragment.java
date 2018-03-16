package wu.chengsiyi.com.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.zhouyou.http.EasyHttp;
import com.zhouyou.http.exception.ApiException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import wu.chengsiyi.com.base.BaseFragment;
import wu.chengsiyi.com.callbacks.SimpleCallBack;
import wu.chengsiyi.com.newsxi.ConnectActivity;
import wu.chengsiyi.com.newsxi.DeviceInfoActivity;
import wu.chengsiyi.com.newsxi.R;
import wu.chengsiyi.com.newsxi.SetActivity;
import wu.chengsiyi.com.utils.GlideImageLoader;

/**
 * Created by ${Wu} on 2018/2/25.
 */

/**
        广告图
 https://www.yihisxminiid.com/api/image
 */
public class HomeFragment extends BaseFragment {
    protected View rootView;
    private Unbinder mUnbinder;
    @BindView(R.id.headBanner)
    Banner headBanner;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(rootView==null){
            rootView = inflater.inflate(R.layout.fragment_home, container, false);
            mUnbinder = ButterKnife.bind(this, rootView);
        }

        headBanner.setImageLoader(new GlideImageLoader());

        return rootView;
    }

    @Override
    protected void onFragmentVisibleChange(boolean isVisible) {
        if (isVisible) {
            //更新界面数据，如果数据还在下载中，就显示加载框
            Log.d("lazyTest", " F1 可见 : ");
        } else {
            //关闭加载框
            Log.d("lazyTest", "  F1 不可见:  ");
        }
    }

    @Override
    protected void onFragmentFirstVisible() {
        //去服务器下载数据
        Log.d("EasyHttp", "下载数据: ");

        EasyHttp.get("/api/image")
                .baseUrl("https://www.yihisxminiid.com")

                .execute(new SimpleCallBack<String>() {
                    @Override
                    public void onError(ApiException e) {
                        Log.d("EasyHttp", "  错误  : ");
                    }

                    @Override
                    public void onSuccess(String s) {
                        Log.d("EasyHttp", "onSuccess: >>>  "+s);
                       /*
                        Gson gson = new Gson();
                        HeadBannerEntity entity = gson.fromJson(s,HeadBannerEntity.class);

                        List<HeadBannerEntity.ImagesUrlBean> images_url = entity.getImages_url();
                        Log.d("EasyHttp", "onSuccess:  sssss  "+images_url.get(0).getLink());
                        */

                        ArrayList<String> links = new ArrayList<>();
                        try {
                            JSONObject object = new JSONObject(s);
                            JSONArray array = object.getJSONArray("images_url");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object1 = array.getJSONObject(i);
                                links.add(object1.getString("link"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("EasyHttp", "onSuccess: "+links.toString());

                        headBanner.setImageLoader(new GlideImageLoader());
                        headBanner.setImages(links);
                        headBanner.setIndicatorGravity(BannerConfig.CENTER);
                        headBanner.start();
                    }


                })
        ;

    }

    @OnClick(R.id.lianjie)
    public void clicklianjie(){
        Context applicationContext = getActivity().getApplication().getApplicationContext();
        startActivity(new Intent(applicationContext, ConnectActivity.class));
    }

   @OnClick(R.id.mendian)
    public void clickmendian(){

   }

   @OnClick(R.id.shezhi)
   public void clickshezhi(){
       Context applicationContext = getActivity().getApplication().getApplicationContext();
       startActivity(new Intent(applicationContext, SetActivity.class));
   }

   @OnClick(R.id.device_info)
   public void clickdeviceInfo(){
       Context applicationContext = getActivity().getApplication().getApplicationContext();
       startActivity(new Intent(applicationContext, DeviceInfoActivity.class));
   }


}
