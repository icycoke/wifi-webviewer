package com.icycoke.android.wifiwebviewer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ExploreFragment extends Fragment {

    private static final String TAG = "ExploreFragment";

    private WebView webView;
    private WebViewClient webViewClient;

    private PageStartListener pageStartListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        pageStartListener = (PageStartListener) getActivity();
        webView = getView().findViewById(R.id.webview);
        webViewClient = new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(TAG, "onPageStarted: page started, the url is:" + webView.getOriginalUrl());
                pageStartListener.setShareContent(webView.getOriginalUrl());
            }
        };
        webView.setWebViewClient(webViewClient);
        webView.loadUrl("http://www.google.com");
    }

    public interface PageStartListener {
        void setShareContent(String url);
    }
}
