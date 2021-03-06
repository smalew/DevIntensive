package com.softdesign.devintensive.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.redmadrobot.chronos.ChronosConnector;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.ui.views.AspectRatioImageView;
import com.softdesign.devintensive.utils.ItemTouchHelperAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;


/**
 * Created by smalew on 14.07.16.
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> implements ItemTouchHelperAdapter{
    private String TAG = "UserAdapter";

    private Context mContext;
    private List<User> mUsers;
    private UserViewHolder.CustomClickListener mCustomClickListener;

    public UsersAdapter(List<User> users, UserViewHolder.CustomClickListener listener) {
        this.mUsers = users;
        this.mCustomClickListener = listener;
    }

    @Override
    public UsersAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false);
        return new UserViewHolder(view, mCustomClickListener);
    }

    @Override
    public void onBindViewHolder(final UsersAdapter.UserViewHolder holder, int position) {
        User user = mUsers.get(position);

        final String photoUri;
        if (user.getPhoto().isEmpty()){
            photoUri = null;
        }else{
            photoUri = user.getPhoto();
        }

        DataManager.getInstance().getPicassoCache().getPicassoInstance().with(mContext).
                load(photoUri).
                placeholder(holder.mDummy).
                error(holder.mDummy).
                networkPolicy(NetworkPolicy.OFFLINE).
                resize(200,0).
                into(holder.mUserFoto, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Load photo from cache");
                    }

                    @Override
                    public void onError() {
                        DataManager.getInstance().getPicassoCache().getPicassoInstance().with(mContext).
                                load(photoUri).
                                placeholder(holder.mDummy).
                                resize(200,0).
                                into(holder.mUserFoto);
                    }
                });

        holder.mUserFullName.setText(user.getFullName());
        holder.mRating.setText(String.valueOf(user.getRating()));
        holder.mCodeLines.setText(String.valueOf(user.getCodeLines()));
        holder.mProjects.setText(String.valueOf(user.getProjects()));

        if (user.getBio() != null && user.getBio().isEmpty()) {
            holder.mBio.setVisibility(View.GONE);
        } else {
            holder.mBio.setText(user.getBio());
            holder.mBio.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    @Override
    public void onItemDismiss(int position) {
        final String userId = mUsers.get(position).getRemoteId();

        Runnable changeDB = new Runnable() {
            @Override
            public void run() {
                User user = DataManager.getInstance().getUser(userId).get(0);
                user.setDeleteFlag(true);
                DataManager.getInstance().deleteUser(user);
            }
        };
        Handler inJob = new Handler();

        inJob.post(changeDB);
        mUsers.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(final int fromPosition, final int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mUsers, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mUsers, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);

        Runnable changeDB = new Runnable() {
            @Override
            public void run() {
                DataManager mDataManager = DataManager.getInstance();

                final String firstUserId = mUsers.get(fromPosition).getRemoteId();
                final String secondUserId = mUsers.get(toPosition).getRemoteId();

                User firstUser = mDataManager.getUser(firstUserId).get(0);
                User secondUser = mDataManager.getUser(secondUserId).get(0);

                int firstPosition = firstUser.getPositionFlag();
                int secondPosition = secondUser.getPositionFlag();

                firstUser.setPositionFlag(secondPosition);
                secondUser.setPositionFlag(firstPosition);

                mDataManager.setUserPosition(firstUser);
                mDataManager.setUserPosition(secondUser);
            }
        };
        Handler inJob = new Handler();
        inJob.post(changeDB);

        return true;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private AspectRatioImageView mUserFoto;
        private Drawable mDummy;

        private TextView mUserFullName;
        private TextView mRating;
        private TextView mCodeLines;
        private TextView mProjects;
        private TextView mBio;
        private Button mOpenInfo;

        private CustomClickListener mListener;

        public UserViewHolder(View itemView, CustomClickListener listener) {
            super(itemView);

            this.mListener = listener;

            this.mUserFoto = (AspectRatioImageView) itemView.findViewById(R.id.list_user_photo);
            mDummy = mUserFoto.getContext().getResources().getDrawable(R.drawable.nav_header_bg);
            this.mUserFullName = (TextView) itemView.findViewById(R.id.list_user_fullname);
            this.mRating = (TextView) itemView.findViewById(R.id.list_user_rating);
            this.mCodeLines = (TextView) itemView.findViewById(R.id.list_user_code_lines);
            this.mProjects = (TextView) itemView.findViewById(R.id.list_user_projects);
            this.mBio = (TextView) itemView.findViewById(R.id.list_user_bio);
            this.mOpenInfo = (Button) itemView.findViewById(R.id.list_open_user_info);

            this.mOpenInfo.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (this.mListener != null){
                this.mListener.onClickOpenUserInfoListener(getAdapterPosition());
            }
        }

        public interface CustomClickListener{

            void onClickOpenUserInfoListener(int position);
        }
    }
}
