package com.jpfeng.retrowebsocket;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.jpfeng.retrowebsocket.databinding.ActivityMainBinding;
import com.jpfeng.websocket.RetroWebSocket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mViewRoot;
    private ViewTreeObserver.OnGlobalLayoutListener mGLListener;

    private MainViewModel mModel;
    private ActivityMainBinding mBinding;
    private Observable.OnPropertyChangedCallback mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mViewRoot = mBinding.llMainRoot;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        MessageAdapter adapter = new MessageAdapter();
        mBinding.rvMessageList.setLayoutManager(layoutManager);
        mBinding.rvMessageList.setAdapter(adapter);

        mModel.getAddress().observe(this, s -> mBinding.setVariable(BR.address, s));
        mModel.getMessage().observe(this, s -> mBinding.setVariable(BR.message, s));
        mModel.isConnecting().observe(this, b -> mBinding.setVariable(BR.connecting, b));
        mModel.isConnected().observe(this, b -> mBinding.setVariable(BR.connected, b));
        mModel.getHint().observe(this, this::showSnack);
        mModel.getListData().observe(this, messages -> {
            adapter.setData(messages);
            if (0 == layoutManager.findFirstCompletelyVisibleItemPosition()) {
                mBinding.rvMessageList.smoothScrollToPosition(0);
            }
        });

        mBinding.setVariable(BR.connectDisconnectClickListener, (OnClickListener) v -> mModel.changeConnection());
        mBinding.setVariable(BR.sendClickListener, (OnClickListener) v -> mModel.sendMessage());

        mCallback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                mModel.updateField(mBinding, propertyId);
            }
        };
        mBinding.addOnPropertyChangedCallback(mCallback);

        mBinding.setVariable(BR.connecting, false);

        mGLListener = () -> {
            int heightDiff = mViewRoot.getRootView().getHeight() - mViewRoot.getHeight();
            if (heightDiff > getResources().getDimensionPixelSize(R.dimen.height_keyboard_different)) {
                // 如果相差高度大于一个定值则认为是键盘弹出
                mBinding.rvMessageList.smoothScrollToPosition(0);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewRoot.getViewTreeObserver().addOnGlobalLayoutListener(mGLListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mViewRoot.getViewTreeObserver().removeOnGlobalLayoutListener(mGLListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding.removeOnPropertyChangedCallback(mCallback);
    }

    private void showSnack(String hint) {
        if (TextUtils.isEmpty(hint)) {
            return;
        }
        Snackbar.make(mBinding.rvMessageList, hint, Snackbar.LENGTH_SHORT)
                .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        mModel.clearHint();
                    }
                }).show();
    }
}
