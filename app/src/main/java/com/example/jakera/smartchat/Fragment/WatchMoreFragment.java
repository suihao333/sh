package com.example.jakera.smartchat.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jakera.smartchat.Activity.MainActivity;
import com.example.jakera.smartchat.Adapter.WatchMoreRecyclerAdapter;
import com.example.jakera.smartchat.Entry.BeautifulPictureEntry;
import com.example.jakera.smartchat.Interface.EndLessOnScrollListener;
import com.example.jakera.smartchat.R;
import com.example.jakera.smartchat.Utils.OkhttpHelper;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by jakera on 18-1-25.
 */

public class WatchMoreFragment extends Fragment implements Callback {

    private RecyclerView recyclerview_watch_more;
    private WatchMoreRecyclerAdapter watchMoreRecyclerAdapter;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;

    private OkhttpHelper okhttpHelper;


    private BeautifulPictureEntry datas;

    private SwipeRefreshLayout swipe_refresh_layout;

    private boolean isRefresh = false;

    private String TAG = "WatchMoreFragment";

    public WatchMoreFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.watch_more_fragment,container,false);
        recyclerview_watch_more = (RecyclerView) view.findViewById(R.id.recyclerview_watch_more);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerview_watch_more.setLayoutManager(staggeredGridLayoutManager);
        watchMoreRecyclerAdapter = new WatchMoreRecyclerAdapter();
        watchMoreRecyclerAdapter.setContext(getContext());
        recyclerview_watch_more.setAdapter(watchMoreRecyclerAdapter);
        swipe_refresh_layout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefresh = true;
                okhttpHelper.getByUrl("https://www.apiopen.top/meituApi?page=0");
            }
        });
        recyclerview_watch_more.addOnScrollListener(new EndLessOnScrollListener(staggeredGridLayoutManager) {
            @Override
            public void onLoadMore() {
                Log.i(TAG, "recycler is loading.......");
                okhttpHelper.getByUrl("https://www.apiopen.top/meituApi?page=0");
            }
        });
        init();
        return view;
    }

    public void init() {
        okhttpHelper = new OkhttpHelper();
        okhttpHelper.setCallback(this);
        okhttpHelper.getByUrl("https://www.apiopen.top/meituApi?page=0");

    }


    @Override
    public void onFailure(Call call, IOException e) {

    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {

        Gson gson = new Gson();
        datas = gson.fromJson(response.body().string(), BeautifulPictureEntry.class);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                watchMoreRecyclerAdapter.setDatas(datas.data, isRefresh);
                if (isRefresh) {
                    swipe_refresh_layout.setRefreshing(false);
                    isRefresh = false;
                }

            }
        });
    }
}
