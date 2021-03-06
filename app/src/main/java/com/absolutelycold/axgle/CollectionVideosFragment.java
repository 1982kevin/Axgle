package com.absolutelycold.axgle;


import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.baoyz.widget.PullRefreshLayout;


/**
 * A simple {@link Fragment} subclass.
 */
public class CollectionVideosFragment extends Fragment {

    private VideoCollection videoCollection = null;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private PullRefreshLayout refreshLayout;
    private boolean isRefreshing = false;
    private Boolean needBlur;

    boolean isLoading = false;

    public CollectionVideosFragment() {
        // Required empty public constructor
    }


    public static CollectionVideosFragment newInstance(Boolean needBlur) {

        Bundle args = new Bundle();
        args.putBoolean("need_blur", needBlur);
        CollectionVideosFragment fragment = new CollectionVideosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_collections, container, false);


        needBlur = getArguments().getBoolean("need_blur");

        refreshLayout = linearLayout.findViewById(R.id.pull_refresh_layout);

        refreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new AllVideosLoadTask().execute(0, 20);
            }
        });

        new AllVideosLoadTask().execute(0, 20);
        recyclerView = refreshLayout.findViewById(R.id.all_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));;


        return linearLayout;
    }

    private class AllVideosLoadTask extends AsyncTask<Integer, Void, VideoCollection> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!isRefreshing) {
                refreshLayout.setRefreshing(true);
            }
        }

        @Override
        protected VideoCollection doInBackground(Integer... pages) {
            VideoCollection vc = new VideoCollection(pages[0],pages[1]);
            return vc;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(VideoCollection vc) {
            super.onPostExecute(vc);

            videoCollection = vc;
            adapter = new CoverCardAdapter(vc, needBlur);
            recyclerView.setAdapter(adapter);
            InitScrollListener();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    }

    public class LoadMoreTask extends AsyncTask<Void, Void, Void> {


        private VideoCollection videoCollection;
        private RecyclerView recyclerView;
        private int videoInfoCounts;


        public LoadMoreTask(VideoCollection videoCollection, RecyclerView recyclerView) {
            this.videoCollection = videoCollection;
            this.recyclerView = recyclerView;
            videoInfoCounts = videoCollection.ItemsCount();
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... rvs) {

            videoCollection.LoadMore();
            return null;
        }

        @Override
        protected void onPostExecute(Void rv) {
            super.onPostExecute(rv);
            this.videoCollection.removeItem(videoInfoCounts - 1);
            this.recyclerView.getAdapter().notifyItemRemoved(videoInfoCounts - 1);
            this.recyclerView.getAdapter().notifyDataSetChanged();
            isLoading = false;
        }
    }



    public void InitScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull final RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//                if (dy > 0) {
//                    int visibleItemCount = layoutManager.getChildCount();
//                    int totalItemsCount = layoutManager.getItemCount();
//                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
//                    if (visibleItemCount + pastVisibleItems >= totalItemsCount) {
//                        LoadMoreTask loadMoreTask = new LoadMoreTask(videoCollection);
//                        loadMoreTask.execute(recyclerView);
//                    }
//                }

                final VideoCollection vc = (VideoCollection) ((CoverCardAdapter)recyclerView.getAdapter()).getVideoInfos();
                if (!isLoading) {
                    if (layoutManager != null && (layoutManager.findLastVisibleItemPosition() == vc.ItemsCount() - 1)) {
                        vc.addItem(null);
                        recyclerView.getAdapter().notifyItemInserted(vc.ItemsCount() - 1);
                        isLoading = true;
                        LoadMoreTask loadMoreTask = new LoadMoreTask(vc, recyclerView);
                        loadMoreTask.execute();
                    };
                }
            }

        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    public void refreshCollections() {
        new AllVideosLoadTask().execute(0, 20);
    }

    public void setNeedBlur(Boolean needBlur) {
        this.needBlur = needBlur;
    }
}
