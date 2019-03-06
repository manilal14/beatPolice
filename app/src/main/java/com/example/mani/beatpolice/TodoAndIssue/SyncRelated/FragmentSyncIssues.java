package com.example.mani.beatpolice.TodoAndIssue.SyncRelated;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mani.beatpolice.CommonPackage.MyInterface;
import com.example.mani.beatpolice.TodoAndIssue.IssueRelated.IssueDao;
import com.example.mani.beatpolice.TodoAndIssue.IssueRelated.IssueTable;
import com.example.mani.beatpolice.R;
import com.example.mani.beatpolice.RoomDatabase.BeatPoliceDb;

import java.util.ArrayList;
import java.util.List;

public class FragmentSyncIssues extends Fragment implements MyInterface {

    private final String TAG = "FragmentSyncIssues";
    private SyncHomePage mActivity;

    private View mRootView;
    private List<IssueTable> mIssueList;
    FragmentSyncIssues mFragmentSyncIssue;



    @Override
    public void updatePage() {
        new FetchIssueFromRoom(BeatPoliceDb.getInstance(mActivity)).execute();
    }


    public FragmentSyncIssues() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (SyncHomePage) getActivity();
        mIssueList = new ArrayList<>();

        mFragmentSyncIssue = this;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_sync_issues, container, false);
        new FetchIssueFromRoom(BeatPoliceDb.getInstance(mActivity)).execute();
        return mRootView;
    }



    class FetchIssueFromRoom extends AsyncTask<Void, Void, Void> {

        private final IssueDao issueDao;

        public FetchIssueFromRoom(BeatPoliceDb instance) {
            issueDao = instance.getIssueDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mIssueList != null)
                mIssueList.clear();
            mIssueList = issueDao.getAllIssue();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(mIssueList.size()==0){
                mRootView.findViewById(R.id.no_data).setVisibility(View.VISIBLE);
            }
            else
                mRootView.findViewById(R.id.no_data).setVisibility(View.GONE);

            RecyclerView recyclerView = mRootView.findViewById(R.id.recycler_view_sync_issue);
            recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
            SyncIssueAdapter adapter = new SyncIssueAdapter(mActivity,mIssueList,mFragmentSyncIssue);
            recyclerView.setAdapter(adapter);
        }
    }

}
