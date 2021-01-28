package com.icycoke.android.wifiwebviewer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class ShareFragment extends Fragment {

    private static final String TAG = "ShareFragment";
    private static final String FACEBOOK_GRAPH_URL_PREFIX = "https://graph.facebook.com/";
    private static final String FACEBOOK_GRAPH_URL_SUFFIX = "/picture?type=large";

    private LoginButton loginButton;
    private ShareButton shareButton;
    private ImageView picImageView;
    private TextView userNameTextView;

    private CallbackManager callbackManager;
    private Bundle bundle;
    private AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            if (currentAccessToken == null) {
                LoginManager.getInstance().logOut();
                picImageView.setImageResource(R.drawable.ic_baseline_account_box_24);
                userNameTextView.setText("");
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_share, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        loginButton = getView().findViewById(R.id.login_button);
        shareButton = getView().findViewById(R.id.share_button);
        picImageView = getView().findViewById(R.id.profile_pic);
        userNameTextView = getView().findViewById(R.id.user_id);

        callbackManager = CallbackManager.Factory.create();

        loginButton.setPermissions(Arrays.asList("user_gender, user_friends, pages_messaging"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess: Login successful");
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel: Login canceled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "onError: Login error!");
            }
        });

        shareButton.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.d(TAG, "onSuccess: share button on click");

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
        GraphRequest graphRequest = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d(TAG, "onCompleted: " + object.toString());
                        try {
                            String userName = object.getString("name");
                            String userId = object.getString("id");

                            userNameTextView.setText(userName);
                            Picasso.get()
                                    .load(FACEBOOK_GRAPH_URL_PREFIX + userId + FACEBOOK_GRAPH_URL_SUFFIX)
                                    .into(picImageView);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        bundle = new Bundle();
        bundle.putString("fields", "gender, name, id");

        graphRequest.setParameters(bundle);
        graphRequest.executeAsync();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LoginManager.getInstance().logOut();
        picImageView.setImageResource(R.drawable.ic_baseline_account_box_24);
        userNameTextView.setText("");
        accessTokenTracker.stopTracking();
    }

    public void setShareContent(String url) {
        ShareLinkContent shareLinkContent = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(url))
                .build();
        shareButton.setShareContent(shareLinkContent);
        Log.d(TAG, "setShareContent: the share content is:" + shareButton.getShareContent().getContentUrl().toString());
    }
}
