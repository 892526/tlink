//
//  BroadcastVNCServer.swift
//  BroadcastServer
//
//  Created by 板垣勇次 on 2018/08/06.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import ExternalAccessory
import JKEGCommonLib
import UIKit

/// AppExtention内のVNCサーバークラス(RealVNC Server SDKを使用しています)
class BroadcastVNCServer: NSObject, VNCServerDelegate, VNCRPCaptureDelegate {
    /// パラメータ定数定義
    class ParamConst {
        // 接続用ベアラー名(Connect)
        static let vncBearerNameConnection: String = "C"
        
        // 接続待ベアラー名(Listen)
        static let vncBearerNameLesten: String = "L"
        
        // VNCServer/Viewer間接続用コマンド名(USB接続用)。フォーマット中の【%@】部分にプロトコルストリングを設定
        static let commandStringUSBFormat: String = "vnccmd:v=1;t=USB;p=%@"
        // static let commandStringUSBFormat: String = "vnccmd:v=1;t=USB;m=C;p=%@"
        
        // VNCServer/Viewer間接続用コマンド名(TCP接続用)
        static let commandStringTCP: String = "vnccmd:v=1;t=L;p=5900"
    }
    
    /// VNCサーバー（RealVNC Server SDKで定義しているクラス）
    private var vncServer: VNCServer?
    
    // キャプチャーデータ設定用排他制御
    private let readyForCaptureCondition: NSRecursiveLock = NSRecursiveLock()
    
    // プロトコルストリング
    private var currentProtocolString: String = String.Empty
    
    // 最新サンプルバッファ
    private var latestSampleBuffer: Unmanaged<CMSampleBuffer>?
    private var timeOfNextFramebuffer: TimeInterval = 0
    private var timeOfNextScreenshot: TimeInterval = 0
    
    // フレームレート(60fps)
    private let frameRateDefault: TimeInterval = 1 / 60.0
    private var frameRate: TimeInterval = 0
    
    // 連携状態
    private var isConnected: Bool = false
    
    /// 更新タイマー
    private var updateTimer: Timer?
    
    /// サーバー動作中
    private var isStarted: Bool = false
    
    // ------------------------------------------------------------------------------------------
    
    // MARK: - Initializer methods
    
    // ------------------------------------------------------------------------------------------
    
    /// シングルトンインスタンス取得
    static var shared: BroadcastVNCServer = {
        BroadcastVNCServer()
    }()
    
    /// 初期化します
    private override init() {
        super.init()
        AppLogger.debug()
        
        /*
         // VNCサーバー作成
         if let srvObj = VNCServer(delegate: self, withRPCapture: self) {
         AppLogger.debug("VNCServer created.")
         
         // ライセンスファイルをロードし、VNCサーバに追加
         if !addLicenseFile(srvObj) {
         // ライセンスファイルエラー
         AppLogger.debug("Failed to add license...")
         }
         
         // ベアラー追加
         addBearer(server: srvObj)
         
         // VNCサーバーのインスタンス保持
         vncServer = srvObj
         
         } else {
         // VNCServer生成失敗
         AppLogger.debug("VNCServer create failed...")
         }
         */
    }
    
    /// ライセンスファイルをロードし、VNCサーバーに設定する。
    ///
    /// - Parameter server: VNCサーバー
    /// - Returns: true: 成功、false: 失敗
    private func addLicenseFile(_ server: VNCServer) -> Bool {
        AppLogger.debug()
        
        // ライセンスファイルロードする
        if let licenseText = LicenseLoader.loadLicense(searchPath: Bundle.main.bundlePath, fileName: nil) {
            AppLogger.debug("liceseText = \n\(licenseText)")
            
            var sirialData: NSData?
            var sirialDataLength = 0
            
            // ライセンスファイル追加
            let vncError = server.addLicense(licenseText, withSerial: &sirialData)
            if vncError == VNCServerErrorNone {
                #if ENABLE_LOG
                    // シリアルデータを文字列にしてデバッグログで表示
                    var serialString = String.Empty
                    if let sirialData = sirialData {
                        let data = Data(referencing: sirialData)
                        sirialDataLength = data.count
                        
                        for value in data.toByteArray() {
                            if !serialString.isEmpty {
                                serialString.append(",")
                            }
                            serialString.append(String(format: "%02X", value))
                        }
                    }
                    // 追加成功
                    AppLogger.debug("Successfully added license. >> SerialData(Len: \(sirialDataLength)) = \(serialString)")
                #endif // ENABLE_LOG
                
                return true
            } else {
                // 追加失敗
                AppLogger.debug("Failed to add license...")
            }
        }
        return false
    }
    
    /// 各種ベアラー追加
    ///
    /// - Parameter server: VNCサーバーインスタンス
    private func addBearer(server: VNCServer) {
        AppLogger.debug()
        
        // 接続用ベアラー作成(Connect)
        let connectBearer = VNCBearerWrapper(bearerName: ParamConst.vncBearerNameConnection, withInitializer: VNCBearerInitialize_C)
        // 接続待ベアラー作成(Listen)
        let lestenBearer = VNCBearerWrapper(bearerName: ParamConst.vncBearerNameLesten, withInitializer: VNCBearerInitialize_L)
        // USB接続用ベアラー作成
        let usbBearer = VNCUSBBearer()
        
        // ベアラーを追加
        server.add(connectBearer)
        server.add(lestenBearer)
        server.add(usbBearer)
    }
    
    /// キャプチャーサンプルバッファのロック取得
    private func captueBufferLock() {
        // サンプルバッファル用ロック取得
        readyForCaptureCondition.lock()
        // AppLogger.debug("Lock...")
    }
    
    /// キャプチャーサンプルバッファのロック解除
    private func captureBufferUnlock() {
        // サンプルバッファル用ロック解除
        // AppLogger.debug("Unlock...")
        readyForCaptureCondition.unlock()
    }
    
    /// USB接続ベアラーのコマンドストリングを作成する
    ///
    /// - Parameter protocolString: 使用するプロトコルストリング
    /// - Returns: コマンドストリング
    private func makeUsbCommandString(_ protocolString: String) -> String {
        let str = String(format: ParamConst.commandStringUSBFormat, protocolString)
        AppLogger.debug("CmmandString = \(str)")
        return str
    }
    
    // ------------------------------------------------------------------------------------------
    
    // MARK: - VNCServerDelegate methods (RealVNC Server SDK)
    
    // ------------------------------------------------------------------------------------------
    
    /// ユーザー認証要求（Accept or Reject）
    ///
    /// - Parameters:
    ///   - username: ユーザー名（Viewer指定）
    ///   - password: パスワード（Viewer指定）
    func onAuthUsername(_ username: String!, withPassword password: String!) {
        AppLogger.debug()
        
        // 現状、ユーザー認証は行わないので、接続可能とする
        if let server = vncServer {
            server.acceptConnection(true)
        } else {
            AppLogger.error("VNCServer == nil !!!")
        }
    }
    
    /// ユーザー認証要求
    ///
    /// - Parameters:
    ///   - needUsername: Viewerがユーザー名を認証で必要とするかどうか
    ///   - needPassword: viewerがパスワードを認証で必要とするかどうか
    func onAuthNeedUsername(_ needUsername: Bool, needPassword: Bool) {
        AppLogger.debug()
        
        // 現状、ユーザー認証は行わないので、接続可能とする
        if let server = vncServer {
            server.acceptConnection(true)
        } else {
            AppLogger.error("VNCServer == nil !!!")
        }
    }
    
    /// サーバーが接続開始状態に遷移
    func onConnecting() {
        AppLogger.debug()
        // 何もしない
    }
    
    /// サーバーが接続中状態に遷移
    ///
    /// - Parameters:
    ///   - localEndpoint: ローカルエンドポイント
    ///   - remoteEndpoint: リモートエンドポイント
    func onConnected(atLocalEndpoint localEndpoint: String!, toRemoteEndpoint remoteEndpoint: String!) {
        AppLogger.debug()
        
        if let server = vncServer {
            // 接続許可
            server.acceptConnection(true)
            
            // 連携開始をローカル通知で表示する
            showConnectedNotification()
        } else {
            AppLogger.error("VNCServer == nil !!!")
        }
    }
    
    /// サーバーが切断状態に遷移
    func onDisconnected() {
        AppLogger.debug()
        
        if let server = vncServer {
            if isAccessoryConnected {
                server.connect(withCommand: makeUsbCommandString(currentProtocolString))
            } else {
                // 連携終了をローカル通知で表示する
                showDisconnectedNotification()
            }
        } else {
            AppLogger.error("VNCServer == nil !!!")
        }
    }
    
    /// サーバーがリッスン状態に遷移
    ///
    /// - Parameter listeningInfo: リッスン情報
    func onListening(withInfo listeningInfo: String!) {
        AppLogger.debug()
        // 何もしない
    }
    
    /// リモートフィーチャーのチェックに成功
    ///
    /// - Parameters:
    ///   - featureCheckId: チェックしたモートフィーチャーID
    ///   - featureId: ViewerにライセンスされているフィーチャーID
    func onRemoteFeatureCheckSucceeded(_ featureCheckId: UInt, withFeature featureId: UInt) {
        AppLogger.debug()
        // 何もしない
    }
    
    /// リモートフィーチャーのチェックに失敗
    ///
    /// - Parameter featureCheckId: チェックしたモートフィーチャーID
    /// - Returns: この失敗の結果として接続を拒否するかどうか
    func onRemoteFeatureCheckFailed(_ featureCheckId: UInt) -> Bool {
        // 何もしない
        return false
    }
    
    /// 暗号化キー受け入れ、または拒否するためのリモートキーイベント
    ///
    /// - Parameters:
    ///   - remoteKeyData: Viewerからのキーデータ
    ///   - remoteKeySignature: Viewerからのシグネチャーキー
    func onRemoteKey(_ remoteKeyData: Data!, withSignature remoteKeySignature: Data!) {
        AppLogger.debug()
        
        if let server = vncServer {
            server.acceptRemoteKey(true)
        } else {
            AppLogger.error("VNCServer == nil !!!")
        }
    }
    
    /// サーバーがRunning状態に遷移(Viewer/Server間で接続された)
    func onRunning() {
        AppLogger.debug()
        // 何もしない
    }
    
    /// VNCサーバー内でエラー発生
    ///
    /// - Parameter error: エラー情報
    func onServerError(_ error: VNCServerError) {
        AppLogger.error("SERVER ERROR >> VNCServerError: " + VNCServerErrorString.toString(errorCode: error))
        AppLogger.debug("Thread.current = \(Thread.current)")
        
        // 連携中のときは、ローカル通知で表示する
        if isConnected {
            // エラーコード取得
            let errorCode = error.rawValue
            
            // 「エラーなし」通知の場合は、無視する
            if VNCServerErrorNone.rawValue == errorCode {
                return
            }
            
            // エラーなし、USB切断以外の場合は、エラーメッセージを表示する
            if VNCServerErrorUSBNotConnected.rawValue != errorCode {
                #if ENABLE_ERROR_LOG
                    // メッセージ情報作成
                    let info = AppExtentionMessageInfo(date: Date(), type: .internalServerError, message: String.Empty, value: errorCode)
                    
                    // エラー情報セット
                    setErrorInfo(info)
                #endif // ENABLE_ERROR_LOG
                
                // エラーメッセージ
                let subMessage = VNCServerErrorString.toString(errorCode: error)
                
                // ローカル通知
                VNCServerLocalNotifiction.showLocalNotification(type: .error, subMessage: subMessage, code: errorCode)
            } else {
                // USB切断の場合は、接続終了メッセージ表示する
                
                // 連携終了をローカル通知で表示する
                showDisconnectedNotification()
            }
        }
    }
    
    /// サーバーログを受信する
    ///
    /// - Parameters:
    ///   - message: ログメッセージ
    ///   - level: ログレベル
    func onServerLog(_ message: String!, with level: VNCServerLogLevel) {
        var mes = String.Empty
        if message != nil {
            mes = message
        }
        
        AppLogger.debug("SERVER LOG >> LogLevel: \(VNCServerErrorString.toString(logLevel: level)), Message: \(mes)")
    }
    
    // ------------------------------------------------------------------------------------------
    
    // MARK: - VNCRPCaptureDelegate methods (RealVNC Server SDK)
    
    // ------------------------------------------------------------------------------------------
    
    /// 画面が変更されたかどうかの問い合わせ
    ///
    /// - Returns: 画面が変更されたかどうか
    func hasScreenChanged() -> Bool {
        // AppLogger.debug()
        
        // サンプルバッファル用ロック取得
        captueBufferLock()
        defer {
            // このメソッドを抜けるときに、アンロックする
            captureBufferUnlock()
        }
        
        return (latestSampleBuffer != nil) && (Date.timeIntervalSinceReferenceDate >= timeOfNextFramebuffer)
    }
    
    /// キャプチャーデータ取得
    ///
    /// - Returns: 最新のキャプチャーデータ
    func captureScreen() -> Unmanaged<CMSampleBuffer>! {
        // AppLogger.debug()
        
        // サンプルバッファル用ロック取得
        captueBufferLock()
        return latestSampleBuffer
    }
    
    /// キャプチャー終了（ドキュメントに詳細なし）
    func captureFinished() {
        // AppLogger.debug()
        // サンプルバッファル用ロック解除
        captureBufferUnlock()
        
        // フレームレートを設定
        timeOfNextFramebuffer = Date.timeIntervalSinceReferenceDate
        timeOfNextFramebuffer += frameRate
    }
    
    // ------------------------------------------------------------------------------------------
    
    // MARK: - External Accessory delegate methods
    
    // ------------------------------------------------------------------------------------------
    
    /// プロトコルストリングを取得する
    ///
    /// - Returns: プロトコルストリング文字列
    func getProtocolString() -> String {
        AppLogger.debug()
        
        let values = ApplicationUtility.protocolString()
        
        if !values.isEmpty {
            AppLogger.debug("ProtocolStrings in Info.plist >> \(values)")
            
            #if ENABLE_LOG
                
                let index = AppGroupsManager.loadProtocolStringIndex()
                if index < values.count {
                    // 本番用、デバック用
                    return values[index]
                }
            #else
                
                // 複数ある場合は先頭のプロトコルストリングを取得する
                return values[0]
                
            #endif // ENABLE_LOG
        }
        
        return String.Empty
    }
    
    /// アクセサリ接続中かどうか取得する
    // - Returns: true: 接続中状態、false: 未接続状態
    var isAccessoryConnected: Bool {
        var result = false
        
        if !currentProtocolString.isEmpty {
            // 接続アクセサリ一覧取得
            let accessories = EAAccessoryManager.shared().connectedAccessories
            
            // 接続アクセサリ一覧から、対象のプロトコルストリングがあるか確認
            for info in accessories {
                if info.protocolStrings.contains(currentProtocolString) {
                    // 対象のプロトコルストリングがある
                    result = true
                    break
                }
            }
        }
        
        return result
    }
    
    /// アクセサリ接続通知の受信を登録する。
    func addExternalAccessoryNotification() {
        AppLogger.debug()
        
        // アクセサリ接続/切断通知用Notification登録
        NotificationCenter.default.addObserver(self, selector: #selector(handleAccessoryAttach(_:)), name: NSNotification.Name.EAAccessoryDidConnect, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(handleAccessoryDetach(_:)), name: NSNotification.Name.EAAccessoryDidDisconnect, object: nil)
        
        // アクセサリ接続/切断通知の受信を開始する
        EAAccessoryManager.shared().registerForLocalNotifications()
        
        if isAccessoryConnected {
            // 既に対象のデバイスが接続中であれば、接続通知発行する
            NotificationCenter.default.post(name: Notification.Name.EAAccessoryDidConnect, object: nil)
        }
    }
    
    /// アクセサリ切断通知の受信を解除する。
    func removeExternalAccessoryNotification() {
        AppLogger.debug()
        
        // アクセサリ接続/切断通知の受信解除する
        EAAccessoryManager.shared().unregisterForLocalNotifications()
        
        // アクセサリ接続/切断通知をNotification解除
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name.EAAccessoryDidConnect, object: nil)
        NotificationCenter.default.removeObserver(self, name: Notification.Name.EAAccessoryDidDisconnect, object: nil)
    }
    
    /// アクセサリ接続通知ハンドラ（EAAccessoryDidConnect）
    ///
    /// - Parameter notification: 通知情報
    @objc private func handleAccessoryAttach(_ notification: Notification) {
        #if ENABLE_LOG
            if let accessory = notification.object as? EAAccessory {
                let accessoryInfoMessage = accessoryInfoString(accessory)
                AppLogger.debug("VNCServer: Accessory connected. >> \n\(accessoryInfoMessage)")
            } else {
                AppLogger.debug("VNCServer: Accessory connected.")
            }
        #endif // ENABLE_LOG
        
        if let server = vncServer {
            if server.isActive {
                // VNCサーバーがアクティブならばリセット
                server.reset()
                AppLogger.debug("Call \"VNCServer.reset()\"")
            } else {
                // VNCサーバーがアクティブでないので、VNCサーバーに接続する
                server.connect(withCommand: makeUsbCommandString(currentProtocolString))
                AppLogger.debug("Call \"connect(USB)\"")
            }
        } else {
            // VNCサーバーインスタンスが無い
            AppLogger.error("VNCServer == nil !!!")
        }
    }
    
    /// アクセサリ切断通知ハンドラ（EAAccessoryDidDisconnect）
    ///
    /// - Parameter notification: 通知情報
    @objc private func handleAccessoryDetach(_ notification: Notification) {
        #if ENABLE_LOG
            if let accessory = notification.object as? EAAccessory {
                let accessoryInfoMessage = accessoryInfoString(accessory)
                AppLogger.debug("VNCServer: Accessory disconnected. >> \n\(accessoryInfoMessage)")
            } else {
                AppLogger.debug("VNCServer: Accessory disconnected.")
            }
        #endif // ENABLE_LOG
        
        // 連携終了を通知する
        showDisconnectedNotification()
    }
    
    /// アクセサリ情報文字列を作成する(デバッグ用)。
    ///
    /// - Parameter accessory: アクセサリ情報
    /// - Returns: アクセサリ情報文字列
    private func accessoryInfoString(_ accessory: EAAccessory) -> String {
        var message = String()
        
        message.append("--- Accessory Information ---\n")
        message.append("manufacturer     : \(accessory.manufacturer)\n")
        message.append("modelNumber      : \(accessory.modelNumber)\n")
        message.append("serialNumber     : \(accessory.serialNumber)\n")
        message.append("hardwareRevision : \(accessory.hardwareRevision)\n")
        message.append("firmwareRevision : \(accessory.firmwareRevision)\n")
        message.append("protocolStrings  : \(accessory.protocolStrings)\n")
        message.append("connectionID     : \(accessory.connectionID)\n")
        message.append("-----------------------------\n")
        
        return message
    }
    
    // ------------------------------------------------------------------------------------------
    
    // MARK: - Local Notification methods
    
    // ------------------------------------------------------------------------------------------
    
    /// 連携開始をローカル通知で表示する
    private func showConnectedNotification() {
        AppLogger.debug("Thread.current = \(Thread.current)")
        
        if !isConnected {
            isConnected = true
            
            #if ENABLE_ERROR_LOG
                // 接続状態セット
                BroadcastVNCServer.setConnectionState(true)
            #endif // ENABLE_ERROR_LOG
            
            // ローカル通知発行（Connect）
            VNCServerLocalNotifiction.showLocalNotification(type: .connect)
            
            #if ENABLE_ERROR_LOG
                // 更新タイマー開始
                startUpdateTimer()
            #endif // ENABLE_ERROR_LOG
        } else {
            AppLogger.debug("already connected...")
        }
    }
    
    /// 連携終了をローカル通知で表示する
    private func showDisconnectedNotification() {
        AppLogger.debug("Thread.current = \(Thread.current)")
        
        if isConnected {
            isConnected = false
            
            #if ENABLE_ERROR_LOG
                // 更新タイマー停止
                stopUpdateTimer()
                // 未接続状態セット
                BroadcastVNCServer.setConnectionState(false)
            #endif // ENABLE_ERROR_LOG
            
            // ローカル通知発行（Disconnect）
            VNCServerLocalNotifiction.showLocalNotification(type: .disconnect)
            
        } else {
            AppLogger.debug("already disconnected...")
        }
    }
    
    // ------------------------------------------------------------------------------------------
    
    // MARK: - Private methods
    
    // ------------------------------------------------------------------------------------------
    
    /// 保持している最新サンプルバッファをリリースする
    private func sampleBufferRelease() {
        // 既にあればリリースする
        if let buf = latestSampleBuffer {
            buf.release()
            latestSampleBuffer = nil
        }
    }
    
    /// 最新サンプルバッファを保持する。
    ///
    /// - Parameter buffer: 保持するサンプルバッファ
    private func sampleBufferRetain(_ buffer: CMSampleBuffer) {
        // 保持する（Retainする）
        latestSampleBuffer = Unmanaged<CMSampleBuffer>.passRetained(buffer)
    }
    
    #if ENABLE_ERROR_LOG
        /// 更新タイマー開始
        private func startUpdateTimer() {
            AppLogger.debug()
            if updateTimer == nil {
                AppLogger.debug("START")
                updateTimer = Timer.scheduledTimer(withTimeInterval: 1.5, repeats: true, block: { _ in
                    // AppLogger.debug("startUpdateTimer::Thread.current = \(Thread.current)")
                    
                    // 接続状態セット
                    BroadcastVNCServer.setConnectionState(self.isConnected)
                    
                })
            }
        }
        
        /// 更新タイマー停止
        private func stopUpdateTimer() {
            AppLogger.debug()
            if let timer = updateTimer {
                timer.invalidate()
                updateTimer = nil
                AppLogger.debug("STOP")
            }
        }
    #endif // ENABLE_ERROR_LOG
    
    /// フレームレートアップ
    private func setupFrameRate() {
        #if ENABLE_LOG
            // デバッグ用にフレームレート変更できるようにする！
            
            // フレームレート設定値取得
            let index = AppGroupsManager.loadFrameRateIndex()
            if index == 1 {
                // 30 fps
                frameRate = 1 / 30.0
            } else if index == 2 {
                // 15 fps
                frameRate = 1 / 15.0
            } else if index == 3 {
                // 5 fps
                frameRate = 1 / 5.0
            } else {
                // 60 fps
                frameRate = frameRateDefault
            }
        #else
            // 本番は、60fps固定
            // 60 fps
            frameRate = frameRateDefault
            
        #endif // ENABLE_LOG
    }
    
    // ------------------------------------------------------------------------------------------
    
    // MARK: - Public methods
    
    // ------------------------------------------------------------------------------------------
    
    /// インスタンス生成する
    public func setup() {
        AppLogger.debug()
    }
    
    /// ブロードキャストサーバー開始する
    public func startServer() {
        AppLogger.debug("---START---")
        if isStarted {
            AppLogger.debug("Already started!!!")
            return
        }
        
        // サーバー動作中状態に繊維
        isStarted = true
        
        // VNCサーバー作成
        if let srvObj = VNCServer(delegate: self, withRPCapture: self) {
            AppLogger.debug("VNCServer created.")
            
            // ライセンスファイルをロードし、VNCサーバに追加
            if !addLicenseFile(srvObj) {
                // ライセンスファイルエラー
                AppLogger.debug("Failed to add license...")
            }
            
            // ベアラー追加
            addBearer(server: srvObj)
            
            // VNCサーバーのインスタンス保持
            vncServer = srvObj
            
        } else {
            // VNCServer生成失敗
            AppLogger.debug("VNCServer create failed...")
        }
        
        // Info.plistのプロトコルストリング取得
        currentProtocolString = getProtocolString()
        AppLogger.debug("ProtocolString = \(String(describing: currentProtocolString))")
        
        // フレームレート設定
        setupFrameRate()
        AppLogger.debug("FrameRate = \(frameRate)")
        
        // 現在時刻保持
        timeOfNextFramebuffer = Date.timeIntervalSinceReferenceDate
        timeOfNextScreenshot = Date.timeIntervalSinceReferenceDate
        
        // アクセサリ接続通知の受信を登録する。
        addExternalAccessoryNotification()
        
        #if DEBUG_ERROR_MESSAGE
            // デバッグ用タイマー開始
            debugStartTimer()
        #endif // DEBUG_ERROR_MESSAGE
        
        AppLogger.debug("---END---")
    }
    
    /// ブロードキャストサーバー停止する
    public func stopServer() {
        AppLogger.debug("---START---")
        
        if !isStarted {
            AppLogger.error("Server not active !!!")
            return
        }
        // サーバー停止中状態に繊維
        isStarted = false
        
        // 連携終了を通知する
        showDisconnectedNotification()
        
        if let server = vncServer {
            // VNCサーバーリセット
            server.reset()
            server.invalidate()
        } else {
            AppLogger.error("VNCServer == nil !!!")
        }
        
        // アクセサリ切断通知の受信を解除する。
        removeExternalAccessoryNotification()
        
        // キャプチャーサンプルバッファリリース
        sampleBufferRelease()
        
        #if DEBUG_ERROR_MESSAGE
            // デバッグ用タイマー停止
            debugStopTimer()
        #endif // DEBUG_ERROR_MESSAGE
        
        AppLogger.debug("---END---")
    }
    
    /// ビデオキャプチャーデータを保持する
    ///
    /// - Parameter buffer: キャプチャーデータ
    public func processVideoFrame(buffer: CMSampleBuffer) {
        // AppLogger.debug()
        
        // サンプルバッファル用ロック取得
        captueBufferLock()
        defer {
            // このメソッドを抜けるときに、アンロックする
            captureBufferUnlock()
        }
        
        if isStarted == false {
            // サーバー起動していないときは、何もしない
            return
        }
        
        // 既にあればリリースする
        sampleBufferRelease()
        
        // 保持する（Retainする）
        sampleBufferRetain(buffer)
    }
    
    /// エラー情報を共有ファイルに保存する
    ///
    /// - Parameter errorInfo: エラー情報
    public func setErrorInfo(_ errorInfo: AppExtentionMessageInfo) {
        // メッセージ情報をセット
        AppGroupsManager.saveErrorInfo(errorInfo)
    }
    
    // -------------------------------------------------------------------------------------------------
    
    // MARK: - Debug methods
    
    #if DEBUG_ERROR_MESSAGE
        
        private var debugUpdateTimer: Timer?
        private var debugErrorIndex: Int = -1
        
        private let debugErrorCodes: [VNCServerError] = [
            VNCServerErrorNone, VNCServerErrorResources, VNCServerErrorState, VNCServerErrorPermissionDenied,
            VNCServerErrorNetworkUnreachable, VNCServerErrorHostUnreachable, VNCServerErrorConnectionRefused,
            VNCServerErrorDNSFailure, VNCServerErrorAddressInUse, VNCServerErrorBadPort, VNCServerErrorDisconnected,
            VNCServerErrorConnectionTimedOut, VNCServerErrorBearerAuthenticationFailed,
            VNCServerErrorUSBNotConnected, VNCServerErrorUnderlyingLibraryNotFound,
            VNCServerErrorBearerConfigurationNotProvided, VNCServerErrorBearerConfigurationInvalid,
            VNCServerErrorBearerLoadFailed, VNCServerErrorProtocolMismatch, VNCServerErrorLoginRejected,
            VNCServerErrorNotLicensedForViewer, VNCServerErrorConnectionClosed, VNCServerErrorInvalidCommandString,
            VNCServerErrorUnsupportedAuth, VNCServerErrorKeyTooBig, VNCServerErrorBadCrypt,
            VNCServerErrorNoEncodings, VNCServerErrorBadPixelformat, VNCServerErrorBearerNotFound,
            VNCServerErrorSignatureRejected, VNCServerErrorInsufficientBufferSpace, VNCServerErrorLicenseNotValid,
            VNCServerErrorFeatureNotLicensed, VNCServerErrorInvalidParameter, VNCServerErrorKeyGeneration,
            VNCServerErrorUnableToStartService, VNCServerErrorAlreadyExists, VNCServerErrorTooManyExtensions,
            VNCServerErrorReset, VNCServerErrorDataRelayProtocolError, VNCServerErrorUnknownDataRelaySessionId,
            VNCServerErrorBadChallenge, VNCServerErrorDataRelayChannelTimeout, VNCServerErrorUserRefusedConnection,
            VNCServerErrorCommandFetchFailed, VNCServerErrorFailed, VNCServerErrorNotImplemented,
            VNCServerErrorCommandSuperseded, VNCServerErrorEnvironment, VNCServerErrorCaptureFrameBufferNotImplemented
        ]
        
        /// 通知機能でエラーメッセージ表示
        ///
        /// - Parameter errorCode: エラーコード
        private func debugShowErrorMessage(_ errorCode: VNCServerError) {
            AppLogger.debug("ErrorCode = \(errorCode)")
            onServerError(errorCode)
        }
        
        /// エラーメッセージ表示切り替え用タイマー開始
        private func debugStartTimer() {
            AppLogger.debug()
            isConnected = true
            if debugUpdateTimer == nil {
                // 10秒毎に表示
                debugUpdateTimer = Timer.scheduledTimer(withTimeInterval: 10, repeats: true, block: { _ in
                    let errorCode = self.debugNextErrorCode()
                    self.debugShowErrorMessage(errorCode)
                })
            }
        }
        
        /// エラーメッセージ表示切り替え用タイマー停止
        private func debugStopTimer() {
            AppLogger.debug()
            isConnected = false
            if debugUpdateTimer != nil {
                debugUpdateTimer?.invalidate()
                debugUpdateTimer = nil
            }
        }
        
        /// 次に表示するエラーメッセージのエラーコードを取得する
        ///
        /// - Returns: エラーコード
        private func debugNextErrorCode() -> VNCServerError {
            isConnected = true
            debugErrorIndex += 1
            if debugErrorIndex >= debugErrorCodes.count {
                debugErrorIndex = 0
            }
            return debugErrorCodes[debugErrorIndex]
        }
    #endif // DEBUG_ERROR_MESSAGE
}
