package com.theonepiano.blemidi;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.theonepiano.blemidi.bluetooth.BluetoothContract;
import com.theonepiano.blemidi.bluetooth.BluetoothPresenter;
import com.theonepiano.blemidi.bluetooth.adapter.BluetoothDevicesAdapter;
import com.theonepiano.blemidi.bluetooth.base.BaseSwipeBackActivity;
import com.theonepiano.blemidi.bluetooth.base.BaseView;
import com.theonepiano.blemidi.bluetooth.bean.MyBluetoothDevice;
import com.theonepiano.blemidi.bluetooth.event.BluetoothClickedEvent;
import com.theonepiano.blemidi.bluetooth.model.BluetoothModel;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static jp.kshoji.blemidi.listener.OnMidiDeviceStatusListener.DEVICE_DISCONNECTED;
import static jp.kshoji.blemidi.listener.OnMidiDeviceStatusListener.DEVICE_IDLE;

/**
 * Created by jim on 2017/7/18.
 */

public class BluetoothSettingsActivity extends BaseSwipeBackActivity<BluetoothModel, BluetoothPresenter> implements BluetoothContract.View {
    private static final String TAG = BluetoothSettingsActivity.class.getSimpleName();

    @BindView(R.id.bluetooth_content_view)
    RecyclerView mContentView;

    @BindView(R.id.start_scan)
    Button mStartScanBtn;

    @BindView(R.id.stop_scan)
    Button mStopScanBtn;

    @BindView(R.id.scanning_progress)
    ProgressBar mScanningProgressBar;

    @BindView(R.id.send_midi_event)
    Button mSendMidiEventBtn;

    @BindView(R.id.received_midi_event)
    TextView mReceivedMidiEventTextView;

    BluetoothDevicesAdapter mBluetoothDevicesAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_mine_bluetooth_settings;
    }

    @Override
    protected void initViews(Bundle bundle) {
        checkPermissions();
    }

    private void checkPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, 12);
        }
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                mPresenter.init();
                break;
        }
    }

    @Override
    protected BaseView getViewImpl() {
        return this;
    }

    @OnClick(R.id.img_back)
    public void onBackClicked(View view) {
        finish();
    }

    @OnClick(R.id.start_scan)
    public void onStartScanClicked(View view) {
        mScanningProgressBar.setVisibility(View.VISIBLE);
        mReceivedMidiEventTextView.setVisibility(View.GONE);
        mSendMidiEventBtn.setVisibility(View.GONE);

        mPresenter.startScan();
    }

    @OnClick(R.id.stop_scan)
    public void onStopScanClicked(View view) {
        mScanningProgressBar.setVisibility(View.GONE);

        mPresenter.stopScan();
    }

    @OnClick(R.id.send_midi_event)
    public void onSendMidiClicked(View view) {
        mReceivedMidiEventTextView.setVisibility(View.GONE);

        // send midi event: connect({ 0xF0, 0x00, 0x20, 0x2B, 0x69, 0x00, 0x00, 0x55, 0x79, 0xF7}  )
        final byte[] data = { (byte)0xF0, 0x00, 0x20, 0x2B, 0x69, 0x00, 0x00, 0x55, 0x79, (byte)0xF7};
        mPresenter.sendMidiMessage(data);

    }

    @Override
    public Context getMyContext() {
        return this;
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mPresenter.stopScan();
    }

    @Override
    public void onDeviceUpdated(final List<MyBluetoothDevice> devices) {
        Log.w(TAG, "onDeviceUpdated![" + devices + "]!!!!");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothDevicesAdapter == null && !BluetoothSettingsActivity.this.isDestroyed()) {
                    RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
                    mContentView.setRecycledViewPool(viewPool);
                    viewPool.setMaxRecycledViews(0, 10);

                    VirtualLayoutManager manager = new VirtualLayoutManager(BluetoothSettingsActivity.this);
                    mContentView.setLayoutManager(manager);

                    mBluetoothDevicesAdapter = new BluetoothDevicesAdapter(BluetoothSettingsActivity.this, manager);
                }

                if (mContentView != null && mBluetoothDevicesAdapter != null) {
                    mBluetoothDevicesAdapter.update(devices);
                    mContentView.setAdapter(mBluetoothDevicesAdapter);
                }

                updateSendMidiBtnStatus();
            }
        });
    }

    @Override
    public void onDeviceScanStatusChanged(final boolean isScanning) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mStartScanBtn != null && mStopScanBtn != null) {
                    mStartScanBtn.setEnabled(!isScanning);
                    mStopScanBtn.setEnabled(isScanning);

                    if (isScanning) {
                        mScanningProgressBar.setVisibility(View.VISIBLE);
                    } else {
                        mScanningProgressBar.setVisibility(View.GONE);

                        updateSendMidiBtnStatus();
                    }
                }
            }
        });
    }

    @Override
    public void onMidiDataReceived(final byte[] data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mReceivedMidiEventTextView.setVisibility(View.VISIBLE);

                String s = String.format("收到的MIDI数据长度为 %d(", data.length);
                for (int i = 0; i < data.length; ++i) {
                    s += String.format("0x%x ", data[i]);
                }
                s += ")";
                mReceivedMidiEventTextView.setText(s);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BluetoothClickedEvent event) {
        Log.w(TAG, "onMessageEvent[" + mPresenter.isBluetoothDeviceConnected() + "]id[" + event.id + "]name[" + event.name + "]status[" + event.status + "]");

        mPresenter.disconnect();

        if (event.status == DEVICE_IDLE || event.status == DEVICE_DISCONNECTED) {
            mPresenter.connect(event.id, event.name);
        }

        updateSendMidiBtnStatus();
    }

    /**
     * Update midi send btn status
     */
    private void updateSendMidiBtnStatus() {
        if (mPresenter.isBluetoothDeviceConnected()) {
            mSendMidiEventBtn.setVisibility(View.VISIBLE);
        } else {
            mSendMidiEventBtn.setVisibility(View.GONE);
        }
    }
}
