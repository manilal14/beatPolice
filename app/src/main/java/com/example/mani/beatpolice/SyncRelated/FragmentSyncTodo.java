package com.example.mani.beatpolice.SyncRelated;


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
import com.example.mani.beatpolice.R;
import com.example.mani.beatpolice.RoomDatabase.BeatPoliceDb;
import com.example.mani.beatpolice.TodoRelated.TodoTable;
import com.example.mani.beatpolice.TodoRelated.TodoTableDao;

import java.util.ArrayList;
import java.util.List;

public class FragmentSyncTodo extends Fragment implements MyInterface {

    private final String TAG = "FragmentSyncTodo";
    private SyncHomePage mActivity;

    private View mRootView;
    private List<TodoTable> mTodoList;

    FragmentSyncTodo mFragmentSyncTodo;

    public FragmentSyncTodo() {}

    @Override
    public void updatePage() {


        new FetchTodoFromRoom(BeatPoliceDb.getInstance(mActivity)).execute();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (SyncHomePage) getActivity();
        mTodoList = new ArrayList<>();

        mFragmentSyncTodo = this;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_sync_todo, container, false);

        new FetchTodoFromRoom(BeatPoliceDb.getInstance(mActivity)).execute();
        return  mRootView;
    }


    class FetchTodoFromRoom extends AsyncTask<Void, Void, Void> {

        private final TodoTableDao todoTableDao;

        public FetchTodoFromRoom(BeatPoliceDb instance) {
            todoTableDao = instance.getTodoTableDao();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mTodoList != null)
                mTodoList.clear();
            mTodoList = todoTableDao.getAllTodo();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            RecyclerView recyclerView = mRootView.findViewById(R.id.recycler_view_sync_todo);
            recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

            SyncTodoAdapter adapter = new SyncTodoAdapter(mActivity,mTodoList,mFragmentSyncTodo);
            recyclerView.setAdapter(adapter);
        }
    }



}
