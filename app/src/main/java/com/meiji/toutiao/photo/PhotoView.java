package com.meiji.toutiao.photo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meiji.toutiao.R;
import com.meiji.toutiao.adapter.photo.PhotoViewAdapter;
import com.meiji.toutiao.bean.photo.PhotoViewBean;
import com.meiji.toutiao.interfaces.IOnItemClickListener;
import com.meiji.toutiao.view.BasePageFragment;

import java.util.List;

public class PhotoView extends BasePageFragment implements IPhoto.View, SwipeRefreshLayout.OnRefreshListener {

    private static final String CATEGORY = "CATEGORY";
    private static final String TAG = "PhotoView";
    private RecyclerView recycler_view;
    private SwipeRefreshLayout refresh_layout;
    private PhotoViewAdapter adapter;
    private String categoryId;
    private boolean canLoading = false;
    private IPhoto.Presenter presenter;

    public static PhotoView newInstance(String categoryId) {
        Bundle bundle = new Bundle();
        bundle.putString(CATEGORY, categoryId);
        PhotoView instance = new PhotoView();
        instance.setArguments(bundle);
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            categoryId = bundle.getString(CATEGORY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.base_fragment_main, container, false);
        presenter = new PhotoPresenter(this);
        initView(view);
        return view;
    }

    private void initView(View view) {
        recycler_view = (RecyclerView) view.findViewById(R.id.recycler_view);
        recycler_view.setHasFixedSize(true);
        recycler_view.setLayoutManager(new LinearLayoutManager(getActivity()));

        refresh_layout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        // 设置下拉刷新的按钮的颜色
        refresh_layout.setColorSchemeResources(R.color.colorPrimary);
        // 设置手指在屏幕上下拉多少距离开始刷新
        refresh_layout.setDistanceToTriggerSync(300);
        // 设置下拉刷新按钮的背景颜色
        refresh_layout.setProgressBackgroundColorSchemeColor(Color.WHITE);
        // 设置下拉刷新按钮的大小
        refresh_layout.setSize(SwipeRefreshLayout.DEFAULT);
        refresh_layout.setOnRefreshListener(this);
    }

    @Override
    public void fetchData() {
        onRequestData();
    }

    @Override
    public void onRefresh() {
        presenter.doRefresh();
    }

    @Override
    public void onRequestData() {
        presenter.doGetUrl(categoryId);
    }

    @Override
    public void onSetAdapter(final List<PhotoViewBean.DataBean> list) {
        if (adapter == null) {
            adapter = new PhotoViewAdapter(list, getActivity());
            recycler_view.setAdapter(adapter);
            adapter.setOnItemClickListener(new IOnItemClickListener() {
                @Override
                public void onClick(View view, int position) {
                    presenter.doOnClickItem(position);
                }
            });
        } else {
            adapter.notifyItemInserted(list.size());
        }

        canLoading = true;

        recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!recyclerView.canScrollVertically(1)) {
                        if (canLoading) {
                            presenter.doRefresh();
                            canLoading = false;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onShowRefreshing() {
        refresh_layout.setRefreshing(true);
    }

    @Override
    public void onHideRefreshing() {
        refresh_layout.setRefreshing(false);
    }

    @Override
    public void onFail() {
        Snackbar.make(refresh_layout, R.string.network_error, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) {
            adapter = null;
        }
    }
}
