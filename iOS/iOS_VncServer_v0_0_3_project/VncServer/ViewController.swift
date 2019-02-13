//
//  ViewController.swift
//  JKVncServer
//
//  Created by 板垣勇次 on 2018/06/29.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import ExternalAccessory
import JKEGCommonLib
import UIKit
import UserNotifications

class ViewController: BaseViewController, UNUserNotificationCenterDelegate {
    var isConnected: Bool = false
    
    var pleaseConnectView: PleaseConnectView?
    var accessoryConnectedView: AccessoryConnectedView?
    
    @IBOutlet weak var messageLabel: MessageLabel!
    
    // 最新のエラーメッセージ
    private var latestDisplayMessage: String = String.Empty
    
    private let fileName = "TEST_FILE_NAME.txt"
    private let testDataText = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    
    // 画面キャプチャー監視
    private var screenCaptureObserver: ScreenCaptureObserver = ScreenCaptureObserver()
    
    // 表示更新タイマー
    private var displayUpdateTimer: Timer?
    private var lastConnectionUpdateTime: String?
    private var updateCheckCount: Int = 0
    private let updateCheckCountMax: Int = 3
    
    // プロトコルストリング
    private var currentProtocolString: String = String.Empty
    
    private var startupBaseView: UIView?
    private var startupLogoImageView: UIImageView?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.        AppLogger.debug()
        
        /* ダブルタップで表示切り替え（デバッグ用）
         let doubleTap = UITapGestureRecognizer(target: self, action: #selector(tappdDouble(sender:)))
         doubleTap.numberOfTapsRequired = 2
         view.addGestureRecognizer(doubleTap)
         */
        
        // 表示メッセージ取得
        latestDisplayMessage = getMessageString()
        
        // アクセサリ接続中表示View追加
        addAccessoryConnectedView()
        
        messageLabel.text = latestDisplayMessage
        messageLabel.alpha = 1
        messageLabel.isHidden = false
        
        // 各種Notification登録
        addNotifications()
        
        // 共通フレームワークバージョン表示
        let version = JKEGCommonLibInfo.version
        AppLogger.debug("JKEGCommonLib framework Version = " + version)
        
        // SDKバージョン表示
        let sdkVersion = RealVncSdkUtility.buildVersion()
        AppLogger.debug("RealVNC Server SDK Version = " + sdkVersion)
        
        /*
         if FileCoordinatorUtility.removeData(fileName: fileName, groupID: SharedSetting.suitName) {
         AppLogger.debug("OK")
         } else {
         AppLogger.debug("NG")
         }
         
         if let data = testDataText.data(using: .utf8) {
         if FileCoordinatorUtility.writeData(data: data, fileName: fileName, groupID: SharedSetting.suitName) {
         AppLogger.debug("OK")
         } else {
         AppLogger.debug("NG")
         }
         }
         
         do {
         if let readData = try FileCoordinatorUtility.readData(fileName: fileName, groupID: SharedSetting.suitName) {
         AppLogger.debug("OK (Data = \(readData.count) bytes")
         } else {
         AppLogger.debug("NG")
         }
         } catch FileCoordinatorUtility.ReadAPIError.fileNotFound {
         AppLogger.debug("fileNotFound")
         } catch FileCoordinatorUtility.ReadAPIError.otherError {
         AppLogger.debug("otherError")
         } catch let error {
         AppLogger.debug("\(error.localizedDescription)")
         }
         */
        
        /*
         if FileCoordinatorUtility.readData(fileName: fileName, groupID: SharedSetting.suitName) != nil {
         AppLogger.debug("Read OK")
         } else {
         AppLogger.debug("Read Error(no file)")
         }
         
         if let data = testDataText.data(using: .utf8) {
         FileCoordinatorUtility.writeData(data: data, fileName: fileName, groupID: SharedSetting.suitName)
         
         FileCoordinatorUtility.readData(fileName: fileName, groupID: SharedSetting.suitName)
         }
         */
        
        // ローカル通知受信開始
        addLocalNotificationReceiver()
        
        // スクリーンキャプチャー監視開始
        screenCaptureObserver.start { isCaptured in
            AppLogger.debug("screenCaptureObserver.isCaptured = \(isCaptured)")
            
            // 表示更新
            self.updateDisplayStatus()
        }
        
        // アクセサリ通信通知受信開始
        updateProtocolString()
        addExternalAccessoryNotification()
        
        addStartupView()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        AppLogger.debug()
        
        startAnimation()
        
        // 表示更新
        updateDisplayStatus()
        
        // 表示更新タイマー開始
        startUpdateTimer()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        AppLogger.debug()
        
        startupViewAnimationStart()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        AppLogger.debug()
        
        stopAnimation()
        
        // 表示更新タイマー停止
        stopUpdateTimer()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    /// この画面の戻る直前に呼び出される
    ///
    /// - Parameter segue: セグエ情報
    @IBAction func unwindToTopViewController(segue: UIStoryboardSegue) {
        AppLogger.debug()
        
        updateProtocolString()
    }
    
    // MARK: - EnterBackground/WillForeground notification
    
    /// フォアグラウンド状態遷移通知
    ///
    /// - Parameter notification: 通知情報
    @objc func viewWillEnterForeground(_ notification: Notification?) {
        AppLogger.debug()
        
        if isViewLoaded && (view.window != nil) {
            AppLogger.debug()
            
            // アニメーション開始
            startAnimation()
            
            // 表示更新
            updateDisplayStatus()
            
            // 表示更新タイマー開始
            startUpdateTimer()
        }
    }
    
    /// バックグラウンド状態通知
    ///
    /// - Parameter notification: 通知情報
    @objc func viewDidEnterBackground(_ notification: Notification?) {
        AppLogger.debug()
        
        if isViewLoaded && (view.window != nil) {
            AppLogger.debug()
            
            // アニメーション停止
            stopAnimation()
            
            // 表示更新タイマー停止
            stopUpdateTimer()
        }
    }
    
    /// アクティブ状態通知
    ///
    /// - Parameter notification: 通知情報
    @objc func applicationDidBecomeActive(_ notification: Notification?) {
        AppLogger.debug()
        
        if isViewLoaded && (view.window != nil) {
            AppLogger.debug()
            
            // アニメーション開始
            startAnimation()
            
            // 表示更新
            updateDisplayStatus()
        }
    }
    
    // MARK: - Local Notification
    
    /// ローカル通知をフォアグラウンドでも受け取るように登録
    private func addLocalNotificationReceiver() {
        UNUserNotificationCenter.current().delegate = self
    }
    
    /// ローカル通知をフォアグラウンドでも受け取る機能を解除
    private func removeLocalNotificationReceiver() {
        UNUserNotificationCenter.current().delegate = nil
    }
    
    /// ローカル通知受信
    ///
    /// - Parameters:
    ///   - center: UNUserNotificationCenterインスタンス
    ///   - notification: UNNotificationインスタンス
    ///   - completionHandler: コンプリーションハンドラ（動作を指定）
    internal func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification,
                                         withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let contentTitle = notification.request.content.title
        let contentMessage = notification.request.content.body
        
        AppLogger.debug("Title = \(contentTitle), Message = \(contentMessage)")
        
        // アラートメッセージのみ表示
        completionHandler([.alert])
        
        // ステータス表示更新
        updateDisplayStatus()
    }
    
    // MARK: - External Accessory
    
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
    
    /// 使用するプロトコルストリング更新
    private func updateProtocolString() {
        // Info.plistのプロトコルストリング取得
        currentProtocolString = getProtocolString()
        AppLogger.debug("ProtocolString = \(String(describing: currentProtocolString))")
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
                AppLogger.debug("VNCAppMain: Accessory connected. >> \n\(accessoryInfoMessage)")
            } else {
                AppLogger.debug("VNCAppMain: Accessory connected.")
            }
        #endif // ENABLE_LOG
        
        if isAccessoryConnected {
            AppLogger.debug("VNCAppMain: Accessory connected.(\(currentProtocolString))")
        }
    }
    
    /// アクセサリ切断通知ハンドラ（EAAccessoryDidDisconnect）
    ///
    /// - Parameter notification: 通知情報
    @objc private func handleAccessoryDetach(_ notification: Notification) {
        #if ENABLE_LOG
            if let accessory = notification.object as? EAAccessory {
                let accessoryInfoMessage = accessoryInfoString(accessory)
                AppLogger.debug("VNCAppMain: Accessory disconnected. >> \n\(accessoryInfoMessage)")
            } else {
                AppLogger.debug("VNCAppMain: Accessory disconnected.")
            }
        #endif // ENABLE_LOG
        
        if !isAccessoryConnected {
            AppLogger.debug("VNCAppMain: Accessory disconnected.(\(currentProtocolString))")
        }
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
    
    // MARK: - Upate Timer
    
    /// 表示更新タイマー開始
    private func startUpdateTimer() {
        AppLogger.debug()
        
        if displayUpdateTimer == nil {
            displayUpdateTimer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { _ in
                // ステータス表示更新
                self.updateDisplayStatus()
            }
        }
    }
    
    /// 表示更新タイマー停止
    private func stopUpdateTimer() {
        AppLogger.debug()
        
        if let obj = displayUpdateTimer {
            obj.invalidate()
            displayUpdateTimer = nil
        }
    }
    
    // MARK: - Private methods
    
    /// PleaseConnectViewを追加する
    private func addPleaseConnectView() {
        // PleaseConnectビュー作成
        let contentView = PleaseConnectView.loadInstance()
        
        // メインViewrに追加
        view.addSubview(contentView)
        
        // コードで制約を設定するので、オフにしてAutolayoutで管理するようにする
        contentView.translatesAutoresizingMaskIntoConstraints = false
        
        // 画面中央に表示するように成約を設定
        view.addConstraints([
            // 水平方向
            NSLayoutConstraint(item: contentView, attribute: NSLayoutAttribute.centerX, relatedBy: NSLayoutRelation.equal,
                               toItem: self.view, attribute: NSLayoutAttribute.centerX, multiplier: 1.0, constant: 0),
            // 垂直方向
            NSLayoutConstraint(item: contentView, attribute: NSLayoutAttribute.centerY, relatedBy: NSLayoutRelation.equal,
                               toItem: self.view, attribute: NSLayoutAttribute.centerY, multiplier: 1.0, constant: 0)
        ])
        
        // インスタンス保持
        pleaseConnectView = contentView
    }
    
    /// アクセサリ接続中Viewを追加する
    private func addAccessoryConnectedView() {
        // 接続中表示View生成
        let contentView = AccessoryConnectedView.loadInstance()
        
        // メインViewrに追加
        view.addSubview(contentView)
        
        // コードで制約を設定するので、オフにしてAutolayoutで管理するようにする
        contentView.translatesAutoresizingMaskIntoConstraints = false
        
        // 画面中央に表示するように成約を設定
        view.addConstraints([
            // 水平方向
            NSLayoutConstraint(item: contentView, attribute: NSLayoutAttribute.centerX, relatedBy: NSLayoutRelation.equal,
                               toItem: self.view, attribute: NSLayoutAttribute.centerX, multiplier: 1.0, constant: 0),
            // 垂直方向
            NSLayoutConstraint(item: contentView, attribute: NSLayoutAttribute.centerY, relatedBy: NSLayoutRelation.equal,
                               toItem: self.view, attribute: NSLayoutAttribute.centerY, multiplier: 1.0, constant: 0)
        ])
        
        // デフォルトは、アクセサリ接続中表示なし（非表示）
        contentView.alpha = 0
        
        // インスタンス保持
        accessoryConnectedView = contentView
    }
    
    /// 各種Notification登録
    private func addNotifications() {
        let notificationCenter = NotificationCenter.default
        
        // バックグラウンド/フォアグラウンド遷移通知を受信するように設定
        notificationCenter.addObserver(self, selector: #selector(ViewController.viewWillEnterForeground(_:)),
                                       name: NSNotification.Name.UIApplicationWillEnterForeground, object: nil)
        notificationCenter.addObserver(self, selector: #selector(ViewController.viewDidEnterBackground(_:)),
                                       name: NSNotification.Name.UIApplicationDidEnterBackground, object: nil)
        
        // アクティブ通知を受信するとうに設定
        notificationCenter.addObserver(self, selector: #selector(applicationDidBecomeActive(_:)),
                                       name: NSNotification.Name.UIApplicationDidBecomeActive, object: nil)
    }
    
    func startUpdateDisplayStatus() {
        AppLogger.debug("AppGroupsManager.loadConnectionState() = " + String(AppGroupsManager.loadConnectionState()))
        
        isConnected = AppGroupsManager.loadConnectionState()
        if isConnected {
            // ### 接続中 ###
            // 　接続中アニメーション表示
            showConnectedView()
        } else {
            // ### 未接続中 ###
            
            // エラーメッセージ取得
            let errorMessage = getErrorMessage()
            
            if !errorMessage.isEmpty {
                // エラーメッセージ表示
                showMessage(errorMessage)
            } else {
                // EA連携中でないない場合、メッセージ表示
                showMessage(getMessageString())
            }
        }
    }
    
    /// ステータス表示を更新する
    func updateDisplayStatus() {
        AppLogger.debug("AppGroupsManager.loadConnectionState() = " + String(AppGroupsManager.loadConnectionState()))
        
        // エラーメッセージ取得
        let errorMessage = getErrorMessage()
        
        // 連携ステータス取得
        let connectionState = AppGroupsManager.loadConnectionState()
        // 更新日取得
        if let connectionUpdateTime = AppGroupsManager.loadConnectionUpdateTime() {
            if connectionUpdateTime != lastConnectionUpdateTime {
                updateCheckCount = 0
                
                // 前回から更新されている
                lastConnectionUpdateTime = connectionUpdateTime
                
                // EA連携状態変化
                if connectionState != isConnected {
                    // 連携状態取得
                    isConnected = connectionState
                    
                    if isConnected {
                        // EA連携中状態
                        
                        // 　接続中アニメーション表示
                        showConnectedView()
                    } else {
                        // 未接続状態
                        
                        if !errorMessage.isEmpty {
                            // エラーメッセージ表示
                            showMessage(errorMessage)
                        } else {
                            // 通常メッセージ表示
                            showMessage(getMessageString())
                        }
                    }
                } else if !isConnected {
                    // EA未連携状態
                    
                    if !errorMessage.isEmpty {
                        // エラーメッセージ表示
                        showMessage(errorMessage)
                    } else {
                        // EA連携中でないない場合、メッセージ表示
                        showMessage(getMessageString())
                    }
                }
                // 抜ける
                return
            } else {
                // アクセサリ接続中
                if isAccessoryConnected {
                    // 3回チェックする
                    if updateCheckCount < updateCheckCountMax {
                        updateCheckCount += 1
                        return
                    } else {
                        updateCheckCount = 0
                    }
                } else {
                    // アクセサリ接続していないバイアは
                }
            }
        }
        
        // 更新されていない
        if connectionState {
            // EA未連携状態にする
            updateCheckCount = 0
            isConnected = false
            lastConnectionUpdateTime = nil
            AppGroupsManager.saveConnectionState(false)
            AppGroupsManager.saveConnectionUpdateTime(nil)
            AppGroupsManager.synchronize()
            
            if !errorMessage.isEmpty {
                // エラーメッセージ表示
                showMessage(errorMessage)
            } else {
                // EA連携中でないない場合、メッセージ表示
                showMessage(getMessageString())
            }
        } else {
            if !errorMessage.isEmpty {
                // エラーメッセージ表示
                showMessage(errorMessage)
            } else {
                // EA連携中でないない場合、メッセージ表示
                showMessage(getMessageString())
            }
        }
    }
    
    func startAnimation() {
        AppLogger.debug()
        
        accessoryConnectedView?.startAnimation()
        
        /*
         if String.isNirOrEmpty(messageLabel.text) {
         accessoryConnectedView?.startAnimation()
         } else {
         stopAnimation()
         }
         */
    }
    
    func stopAnimation() {
        AppLogger.debug()
        accessoryConnectedView?.stopAnimation()
    }
    
    func showMessage(_ message: String) {
        messageLabel.text = message
        
        if messageLabel.isHidden {
            UIView.animate(withDuration: 0.5, animations: {
                self.accessoryConnectedView?.alpha = 0
                self.messageLabel.alpha = 1
            }) { _ in
                self.messageLabel.isHidden = false
            }
        }
    }
    
    func showConnectedView() {
        accessoryConnectedView?.startAnimation()
        
        UIView.animate(withDuration: 0.5, animations: {
            self.accessoryConnectedView?.alpha = 1
            self.messageLabel.alpha = 0
        }) { _ in
            self.messageLabel.isHidden = true
        }
    }
    
    /// エラーメッセージを取得する。
    ///
    /// - Returns: エラーメッセージ
    func getErrorMessage() -> String {
        var message = String.Empty
        
        // BroadcastServerからのメッセージを取得
        if let info = AppGroupsManager.loadErrorInfo() {
            // メッセージを取得したので、クリア
            AppGroupsManager.saveErrorInfo(nil)
            
            switch info.messageType {
            case .undefined:
                message = String.Empty
            case .connect:
                message = String.Empty
            case .disconnect:
                message = String.Empty
            case .unknownError:
                // 不明なエラー
                message = Localize.localizedString("SS_ERR-001")
            // message = NSLocalizedString("SS_ERR-001", comment: "SS_ERR-001")
            case .licenseError:
                // ライセンス認証に失敗
                message = Localize.localizedString("SS_ERR-002")
            // message = NSLocalizedString("SS_ERR-002", comment: "SS_ERR-002")
            case .userAuthError:
                // ユーザー認証失敗
                message = Localize.localizedString("SS_ERR-003")
            // message = NSLocalizedString("SS_ERR-003", comment: "SS_ERR-003")
            case .remoteFeatureFailed:
                // リモートフィーチャー不一致
                message = Localize.localizedString("SS_ERR-004")
            // message = NSLocalizedString("SS_ERR-004", comment: "SS_ERR-004")
            case .internalServerError:
                // サーバー内部エラー
                message = Localize.localizedString("SS_ERR-005")
                // message = NSLocalizedString("SS_ERR-005", comment: "SS_ERR-005")
                
                if info.additionalMessage.isEmpty {
                    message += "\n\(info.additionalMessage) [Code:\(info.additionalIntValue)])"
                } else {
                    message += "\n[Code:\(info.additionalIntValue)])"
                }
            }
            
            if !message.isEmpty {
                // メッセージ保持
                setErrorMessage(message)
            } else {
                // 既存のエラーメッセージがあれば取得する
                message = getCurrentErrorMessage()
            }
        }
        return message
    }
    
    /// 表示メッセージを取得する
    ///
    /// - Returns: 表示メッセージ
    func getMessageString() -> String {
        var message = String.Empty
        
        // 画面キャプチャー中状態のとき
        if screenCaptureObserver.isCaptured {
            // EA連携中のとき
            if AppGroupsManager.loadConnectionState() {
                message = String.Empty
            } else {
                // 本体とスマートフォンをUSBケーブルで接続してください。
                message = Localize.localizedString("SS_MES-003")
            }
        } else {
            // 画面収録機能で[ブロードキャストを開始]し、本体とスマートフォンをUSBケーブルで接続してください。
            message = Localize.localizedString("SS_MES-001")
        }
        return message
    }
    
    private var currentErrorMessage: String?
    private var errorInfoExpireDate: Date?
    
    private func setErrorMessage(_ message: String) {
        // メッセージ保持
        currentErrorMessage = message
        // 有効期限セット
        errorInfoExpireDate = Date(timeIntervalSinceNow: 5.0)
    }
    
    private func getCurrentErrorMessage() -> String {
        if let expDate = errorInfoExpireDate {
            if expDate < Date() {
                // 有効期限切れなので、破棄
                currentErrorMessage = nil
                errorInfoExpireDate = nil
            } else if let msg = currentErrorMessage {
                // 有効期限内なので、メッセージ使用
                return msg
            }
        }
        return String.Empty
    }
    
    // MARK: - Startup Effect
    
    func addStartupView() {
        startupBaseView = UIView(frame: view.bounds)
        if let baseView = startupBaseView {
            baseView.backgroundColor = UIColor.white
            startupLogoImageView = UIImageView(frame: CGRect(x: 0, y: 0, width: 160, height: 152))
            if let imageView = startupLogoImageView {
                imageView.contentMode = .center
                imageView.image = UIImage(named: "StartupLogo")
                baseView.addSubview(imageView)
                imageView.center = view.center
                view.addSubview(baseView)
            }
        }
    }
    
    func startupViewAnimationStart() {
        if (startupLogoImageView != nil) && (startupBaseView != nil) {
            UIView.animate(withDuration: 0.2,
                           delay: 1.3,
                           options: UIViewAnimationOptions.curveEaseOut,
                           animations: { () in
                               self.startupLogoImageView?.transform = CGAffineTransform(scaleX: 2, y: 2)
                               self.startupLogoImageView?.alpha = 0
                           }, completion: { _ in
                               self.startupLogoImageView?.removeFromSuperview()
                               self.startupBaseView?.removeFromSuperview()
                               self.startupLogoImageView = nil
                               self.startupBaseView = nil
            })
        }
    }
}
