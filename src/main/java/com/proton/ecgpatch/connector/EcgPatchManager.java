package com.proton.ecgpatch.connector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.proton.ecgcard.algorithm.bean.AlgorithmResult;
import com.proton.ecgcard.algorithm.bean.RealECGData;
import com.proton.ecgcard.algorithm.callback.AlgorithmResultListener;
import com.proton.ecgcard.algorithm.interfaces.CardFilterAlgorithm;
import com.proton.ecgcard.algorithm.interfaces.IFilterAlgorithm;
import com.proton.ecgpatch.connector.callback.DataListener;
import com.proton.ecgpatch.connector.data.parse.EcgPatchBleDataParse;
import com.proton.ecgpatch.connector.data.parse.IBleDataParse;
import com.proton.ecgpatch.connector.data.uuid.EcgPatchUUID;
import com.proton.ecgpatch.connector.data.uuid.IDeviceUUID;
import com.proton.ecgpatch.connector.utils.BleUtils;
import com.wms.ble.BleOperatorManager;
import com.wms.ble.bean.ScanResult;
import com.wms.ble.callback.OnConnectListener;
import com.wms.ble.callback.OnReadCharacterListener;
import com.wms.ble.callback.OnScanListener;
import com.wms.ble.callback.OnSubscribeListener;
import com.wms.ble.callback.OnWriteCharacterListener;
import com.wms.ble.operator.IBleOperator;
import com.wms.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 王梦思 on 2018/7/7.
 * ble设备管理器
 */
@SuppressLint("StaticFieldLeak")
public class EcgPatchManager {
    /**
     * 设备管理器
     */
    private static Map<String, EcgPatchManager> mEcgPatchManager = new HashMap<>();
    private static Context mContext;
    /**
     * 服务
     */
    private String serviceUUID;
    /**
     * 订阅uuid
     */
    private String notifyCharactorUUID;
    /**
     * 写uuid
     */
    private String writeCharactorUUID;
    /**
     * 服务:设备信息服务
     */
    private String serviceDeviceInfo;
    /**
     * 特征:电量（可读可订阅）
     */
    private String charactorBattery;
    /**
     * 特征:设备版本号（可读）
     */
    private String charactorVersion;
    /**
     * 特征:序列号（可读）
     */
    private String charactorSearial;
    /**
     * 未处理过的数据
     */
    private List<Float> mSourceData = new ArrayList<>();
    private IBleOperator mBleOperator;
    /**
     * 第一次处理数据
     */
    private boolean isFirst = true;
    private int WaveExten = 256;
    /**
     * 第一次处理的数据大小
     */
    private int mFirstDealDataSize = WaveExten + 512;
    /**
     * 处理数据大小
     */
    private int mDealDataSize = 512;
    /**
     * 第几次处理数据（供算法使用）
     */
    private int section;
    /**
     * 接受数据监听器
     */
    private DataListener mDataListener;
    /**
     * 连接监听器
     */
    private OnConnectListener mConnectListener;
    /**
     * 数据解析
     */
    private IBleDataParse dataParse = new EcgPatchBleDataParse();
    /**
     * 设备uuid数据提供者
     */
    private IDeviceUUID deviceUUID = new EcgPatchUUID();
    /**
     * 滤波算法处理器
     */
    private IFilterAlgorithm algorithm = new CardFilterAlgorithm();
    /**
     * 采样率
     */
    private int mSampleRate = 256;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    /**
     * 是否启用算法
     */
    private boolean enableAlgorithm = true;
    private String macaddress;
    /**
     * band50Switch：	50Hz 带阻滤波器开关，0关闭，1开启；
     * band100Switch：	100Hz 带阻滤波器开关，0关闭，1开启；
     * lowpassSwitch：	低通滤波器开关，0关闭，1开启；
     * highpassSwitch：高通滤波器开关，0关闭，1开启；
     * smoothSwitch：	平滑滤波开关，0关闭，1开启；
     */
    private int band50Switch = 1, band100Switch = 1, lowpassSwitch = 1, highpassSwitch = 1, smoothSwitch = 1;
    /**
     * 是否在接收心电数据
     */
    private boolean receivingEcgData;
    private float gainMultiple = 200;

    private EcgPatchManager(String macaddress) {
        mBleOperator = BleOperatorManager.getInstance();
        this.macaddress = macaddress;
        initUUID();
    }

    public static void init(Context context) {
        mContext = context;
        BleOperatorManager.init(mContext);
        //初始化日志
        Logger.newBuilder()
                .tag("ecg_patch")
                .showThreadInfo(false)
                .methodCount(1)
                .methodOffset(5)
                .context(mContext)
                .deleteOnLaunch(true)
                .isDebug(BuildConfig.DEBUG)
                .build();
    }

    public static EcgPatchManager getInstance(String macaddress) {
        if (mContext == null) {
            throw new IllegalStateException("You should initialize EcgPatchManager before using,You can initialize in your Application class");
        }
        if (!mEcgPatchManager.containsKey(macaddress)) {
            mEcgPatchManager.put(macaddress, new EcgPatchManager(macaddress));
        }
        return mEcgPatchManager.get(macaddress);
    }

    /**
     * 扫描心电贴的设备
     */
    public static void scanDevice(final OnScanListener listener) {
        scanDevice(10000, listener);
    }

    /**
     * 扫描心电贴的设备
     */
    public static void scanDevice(int scanTime, final OnScanListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("you should set a scan listener,or you will not receive data");
        }
        BleOperatorManager.getInstance().scanDevice(new OnScanListener() {

            @Override
            public void onScanStart() {
                listener.onScanStart();
            }

            @Override
            public void onDeviceFound(ScanResult scanResult) {
                if (BleUtils.bytesToHexString(scanResult.getScanRecord()).contains("09ff0300")) {
                    listener.onDeviceFound(scanResult);
                }
            }

            @Override
            public void onScanCanceled() {
                listener.onScanCanceled();
            }

            @Override
            public void onScanStopped() {
                listener.onScanStopped();
            }
        }, scanTime, "ECG_Paste");
    }

    /**
     * 停止搜索
     */
    public static void stopScan() {
        BleOperatorManager.getInstance().stopScan();
    }

    /**
     * 通过mac地址连接
     */
    public void connectByMacAddress() {
        clear(false);
        mBleOperator.setConnectListener(mConnectListener);
        scanConnect();
    }

    /**
     * 连接设备带回调
     */
    public void connectByMacAddress(OnConnectListener onConnectListener) {
        this.mConnectListener = onConnectListener;
        connectByMacAddress();
    }

    /**
     * 心电卡连接操作
     */
    public void connectEcgPatch(final OnConnectListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("connect listener can not be null");
        }
        if (mDataListener == null) {
            throw new IllegalArgumentException("you must set receiverDataLister before you connect" +
                    ",if you do not want receive data,you can use other connect method");
        }
        clear(false);
        this.mConnectListener = listener;
        mBleOperator.setConnectListener(new OnConnectListener() {
            @Override
            public void onConnectSuccess() {
                mConnectListener.onConnectSuccess();
                //设置mtu
                mBleOperator.setMTU(macaddress, 23);
                subscribeNotification();
                getBattery();
                getHardVersion();
                getSerial();
                writeGainCommand();
                writeStartEcgCommand();
                writeSyncTimeCommand();
            }

            @Override
            public void onConnectFaild() {
                if (mConnectListener != null) {
                    mConnectListener.onConnectFaild();
                }
            }

            @Override
            public void onDisconnect(boolean isManual) {
                if (mConnectListener != null) {
                    mConnectListener.onDisconnect(isManual);
                }
            }
        });

        scanConnect();
    }

    private void scanConnect() {
        scanDevice(15000, new OnScanListener() {

            private boolean hasFoundDevice;

            @Override
            public void onDeviceFound(ScanResult scanResult) {
                if (scanResult.getDevice().getAddress().equals(macaddress)) {
                    hasFoundDevice = true;
                    mBleOperator.connect(macaddress);
                    stopScan();
                }
            }

            @Override
            public void onScanStopped() {
                Logger.w("扫描停止");
                if (!hasFoundDevice) {
                    if (mConnectListener != null) {
                        mConnectListener.onConnectFaild();
                    }
                }
            }

            @Override
            public void onScanCanceled() {
                Logger.w("扫描取消");
                if (!hasFoundDevice) {
                    if (mConnectListener != null) {
                        mConnectListener.onConnectFaild();
                    }
                }
            }
        });
    }

    private void subscribeNotification() {
        mBleOperator.subscribeNotification(macaddress, serviceUUID, notifyCharactorUUID, new OnSubscribeListener() {
            @Override
            public void onNotify(String uuid, byte[] value) {
                if (value == null || value.length < 23) return;
                byte[] data;
                if (value.length > 23) {
                    data = new byte[23];
                    System.arraycopy(value, 0, data, 0, data.length);
                } else {
                    data = value;
                }
                String dataHex = BleUtils.bytesToHexString(data);
                if (!TextUtils.isEmpty(dataHex)) {
                    if (dataHex.startsWith("aa030eff")) {
                        //电量内存
                        parseBatteryAndMemory(data);
                    } else if (dataHex.startsWith("aa040eff")) {
                        //心电数据
                        receivingEcgData = true;
                    } else if (dataHex.startsWith("aa110e")) {
                        parseGainMultiple(dataHex);
                    } else if (dataHex.startsWith("aa000eff")) {
                        Logger.w("心电贴时间同步成功");
                    } else {
                        if (receivingEcgData) {
                            parseEcgData(data);
                        }
                    }
                }
            }

            @Override
            public void onSuccess() {
                Logger.w("订阅成功");
            }

            @Override
            public void onFail() {
                Logger.w("订阅失败");
            }
        });
    }

    private void parseGainMultiple(String dataHex) {
        int num = Integer.parseInt(dataHex.substring(6, 8), 16);
        int decimals = Integer.parseInt(dataHex.substring(8, 10), 16);
        String value = String.valueOf(num);
        if (decimals < 10) {
            value = value + ".0" + decimals;
        } else {
            value = value + "." + decimals;
        }

        gainMultiple = Float.parseFloat(value);
        Logger.w("增益数据:" + gainMultiple);
    }

    private void writeSyncTimeCommand() {
        String time = Long.toHexString(System.currentTimeMillis());
        // 16eaa978fb2
        int size = 16 - time.length();
        StringBuilder zero = new StringBuilder();
        for (int i = 0; i < size; i++) {
            zero.append('0');
        }
        time = zero.toString() + time;

        byte[] bytes = BleUtils.hexStringToBytes(time);
        bytes = BleUtils.changeBytes(bytes);
        writeCommand("0008" + BleUtils.bytesToHexString(bytes).toUpperCase());
    }

    private void writeGainCommand() {
        writeCommand("1100");
    }

    private void writeStartEcgCommand() {
        writeCommand("04");
    }

    private void writeStopEcgCommand() {
        receivingEcgData = false;
        writeCommand("05");
    }

    private void writeCommand(final String command) {
        mBleOperator.write(macaddress, serviceUUID, writeCharactorUUID, BleUtils.hexStringToBytes(command), new OnWriteCharacterListener() {
            @Override
            public void onSuccess() {
                Logger.w("写入成功:", command);
            }

            @Override
            public void onFail() {
                Logger.w("写入失败:", command);
            }
        });
    }

    /**
     * 断开连接
     */
    public void disConnect() {
        disConnect(true);
    }

    /**
     * 断开连接
     */
    public void disConnect(boolean isClearListener) {
        clear(isClearListener);
        writeStopEcgCommand();
        mBleOperator.disConnect(macaddress);
        mEcgPatchManager.remove(macaddress);
    }

    /**
     * 清空信息
     */
    public void clear(boolean isClearListener) {
        mSourceData.clear();
        receivingEcgData = false;
        section = 0;
        isFirst = true;
        if (isClearListener) {
            mConnectListener = null;
            mDataListener = null;
        }
    }

    /**
     * 解析电量
     */
    private void parseBatteryAndMemory(final byte[] data) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mDataListener != null) {
                    byte[] batteryData = new byte[1];
                    System.arraycopy(data, 4, batteryData, 0, 1);
                    Integer battery = dataParse.parseBattery(batteryData);
                    Logger.w("电量:", battery);
                    mDataListener.receiveBattery(battery);

                    byte[] memoryData = new byte[1];
                    System.arraycopy(data, 13, memoryData, 0, 1);
                    Integer memory = dataParse.parseMemory(memoryData);
                    Logger.w("内存:", memory);
                    mDataListener.receiveMemory(memory);
                }
            }
        });
    }

    private void parseHardVersion(final byte[] data) {
        String hardVersion = dataParse.parseHardVersion(data);
        Logger.w("固件版本:", hardVersion);
        if (mDataListener != null) {
            mDataListener.receiveHardVersion(hardVersion);
        }
    }

    private void parseSerial(final byte[] data) {
        String sn = dataParse.parseSerial(data);
        Logger.w("序列号:", sn);
        if (mDataListener != null) {
            mDataListener.receiveSerial(sn);
        }
    }

    /**
     * 解析ecg数据
     */
    private void parseEcgData(byte[] value) {
        //心电数据
        byte[] ecgData = new byte[21];
        //包序
        final byte[] packageData = new byte[1];
        //体温
        final byte[] tempByte = new byte[1];
        System.arraycopy(value, 0, packageData, 0, packageData.length);
        System.arraycopy(value, 1, tempByte, 0, tempByte.length);
        System.arraycopy(value, 2, ecgData, 0, ecgData.length);

        mSourceData.addAll(dataParse.parseEcgData(ecgData, gainMultiple));
        int sourceDataSize = mSourceData.size();

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mDataListener != null) {
                    mDataListener.receivePackageNum(Integer.parseInt(BleUtils.bytesToHexString(packageData), 16));
                }
                float temp = Integer.parseInt(BleUtils.bytesToHexString(tempByte), 16) * 0.1f + 25;
                if (mDataListener != null) {
                    mDataListener.receiveTemp(temp);
                }
                if (mDataListener != null) {
                    mDataListener.receiveEcgSourceData(mSourceData);
                }
            }
        });

        if (isFirst && sourceDataSize >= mFirstDealDataSize) {
            //滤波处理
            filterEcgData(new ArrayList<>(mSourceData.subList(0, mFirstDealDataSize)));
            isFirst = false;
        } else if (!isFirst && sourceDataSize > mFirstDealDataSize + mDealDataSize * section) {
            //滤波处理
            filterEcgData(new ArrayList<>(mSourceData.subList(mFirstDealDataSize + mDealDataSize * (section - 1), mFirstDealDataSize + mDealDataSize * section)));
        }
    }

    /**
     * 滤波算法处理ecg原始数据
     */
    private void filterEcgData(final List<Float> dealingDataList) {
        final RealECGData processData;
        if (algorithm != null && enableAlgorithm) {
            processData = algorithm.processEcgData(dealingDataList, section++, mSampleRate, band50Switch, band100Switch, lowpassSwitch, highpassSwitch, smoothSwitch);
        } else {
            processData = new RealECGData(dealingDataList);
        }

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mDataListener != null) {
                    mDataListener.signalInterference(processData.getSignalQuality());
                }
                //两次判断目的是为了信号干扰的时候mReceiverDataListener 置空处理
                if (mDataListener != null) {
                    mDataListener.receiveEcgFilterData(processData.getEcgData());
                    mDataListener.receiverHeartRate(processData.getHeartRate());
                }
            }
        });
    }

    /**
     * 获取电量
     */
    public EcgPatchManager getBattery() {
        writeCommand("03");
        return this;
    }

    /**
     * 获取固件版本
     */
    public EcgPatchManager getHardVersion() {
        mBleOperator.read(macaddress, serviceDeviceInfo, charactorVersion, new OnReadCharacterListener() {
            @Override
            public void onSuccess(byte[] data) {
                parseHardVersion(data);
            }

            @Override
            public void onFail() {
                Logger.w("获取固件版本失败");
            }
        });
        return this;
    }

    /**
     * 获取序列号
     */
    public EcgPatchManager getSerial() {
        mBleOperator.read(macaddress, serviceDeviceInfo, charactorSearial, new OnReadCharacterListener() {
            @Override
            public void onSuccess(byte[] data) {
                parseSerial(data);
            }

            @Override
            public void onFail() {
                Logger.w("获取序列号失败");
            }
        });
        return this;
    }

    /**
     * 设置数据接受监听器，只能有一个监听器
     */
    public EcgPatchManager setDataListener(DataListener mReceiverDataListener) {
        this.mDataListener = mReceiverDataListener;
        return this;
    }

    /**
     * 设置uuid加载策略，动态提供uuid
     */
    public EcgPatchManager setDeviceUUIDStrategy(IDeviceUUID deviceUUID) {
        if (deviceUUID == null) {
            throw new IllegalArgumentException("device uuid startegy can not be null");
        }
        this.deviceUUID = deviceUUID;
        initUUID();
        return this;
    }

    /**
     * 获取滤波算法
     */
    public IFilterAlgorithm getFilterAlgorithm() {
        return algorithm;
    }

    /**
     * 设置滤波算法
     */
    public EcgPatchManager setFilterAlgorithm(IFilterAlgorithm filterAlgorithm) {
        algorithm = filterAlgorithm;
        return this;
    }

    /**
     * 获取分析结果
     */
    public void getAnalysisResult(final List<Float> data, int band50Switch, int band100Switch, int lowpassSwitch, int highpassSwitch, int smoothSwitch, final AlgorithmResultListener listener) {
        algorithm.fullAnalyse(data, mSampleRate, band50Switch, band100Switch, lowpassSwitch, highpassSwitch, smoothSwitch, listener);
    }

    public void getAnalysisResult(final AlgorithmResultListener listener) {
        getAnalysisResult(getSourceData(), band50Switch, band100Switch, lowpassSwitch, highpassSwitch, smoothSwitch, listener);
    }

    public void getAnalysisResult(List<Float> data, final AlgorithmResultListener listener) {
        getAnalysisResult(data, band50Switch, band100Switch, lowpassSwitch, highpassSwitch, smoothSwitch, listener);
    }

    public void getAnalysisResult(int band50Switch, int band100Switch, int lowpassSwitch, int highpassSwitch, int smoothSwitch, final AlgorithmResultListener listener) {
        getAnalysisResult(getSourceData(), band50Switch, band100Switch, lowpassSwitch, highpassSwitch, smoothSwitch, listener);
    }

    public AlgorithmResult getAnalysisResult(List<Float> ecgDatas, int band50Switch, int band100Switch, int lowpassSwitch, int highpassSwitch, int smoothSwitch) {
        return algorithm.fullAnalyse(ecgDatas, mSampleRate, band50Switch, band100Switch, lowpassSwitch, highpassSwitch, smoothSwitch);
    }

    /**
     * 获取所有的源数据
     */
    public List<Float> getSourceData() {
        return new ArrayList<>(mSourceData);
    }

    /**
     * 初始化uuid
     */
    private void initUUID() {
        serviceUUID = deviceUUID.getServiceUUID();
        notifyCharactorUUID = deviceUUID.getNotifyCharactor();
        writeCharactorUUID = deviceUUID.getWriteCharactor();
        serviceDeviceInfo = deviceUUID.getDeviceInfoServiceUUID();
        charactorBattery = deviceUUID.getCharactorBatteryUUID();
        charactorSearial = deviceUUID.getCharactorSearialUUID();
        charactorVersion = deviceUUID.getCharactorVersionUUID();
    }

    public EcgPatchManager setConnectListener(OnConnectListener mConnectListener) {
        this.mConnectListener = mConnectListener;
        return this;
    }

    /**
     * 设置是否启用算法
     */
    public EcgPatchManager setAlgorithmEnable(boolean enable) {
        this.enableAlgorithm = enable;
        return this;
    }

    public int getBand50Switch() {
        return band50Switch;
    }

    public void setBand50Switch(int band50Switch) {
        this.band50Switch = band50Switch;
    }

    public int getBand100Switch() {
        return band100Switch;
    }

    public void setBand100Switch(int band100Switch) {
        this.band100Switch = band100Switch;
    }

    public int getLowpassSwitch() {
        return lowpassSwitch;
    }

    public void setLowpassSwitch(int lowpassSwitch) {
        this.lowpassSwitch = lowpassSwitch;
    }

    public int getHighpassSwitch() {
        return highpassSwitch;
    }

    public void setHighpassSwitch(int highpassSwitch) {
        this.highpassSwitch = highpassSwitch;
    }

    public int getSmoothSwitch() {
        return smoothSwitch;
    }

    public void setSmoothSwitch(int smoothSwitch) {
        this.smoothSwitch = smoothSwitch;
    }

    public double getGainMultiple() {
        return gainMultiple;
    }
}
