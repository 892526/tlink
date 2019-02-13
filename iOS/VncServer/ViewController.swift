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
    
    @IBOutlet weak var messageLabel: MessageLabel!
    @IBOutlet weak var imageViewWallpaper: UIImageView!
    
    @IBOutlet weak var imageViewAppIcon: UIImageView!
    @IBOutlet weak var imageVIewAppName: UIImageView!
    @IBOutlet weak var imageViewAvxIcon: UIImageView!
    
    @IBOutlet weak var buttonShareScreen: UIButton!
    @IBOutlet weak var buttonAboutApp: UIButton!
    
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
        
        // 各種Notification登録
        addNotifications()
        
        // 共通フレームワークバージョン表示
        let version = JKEGCommonLibInfo.version
        AppLogger.debug("JKEGCommonLib framework Version = " + version)
        
        // SDKバージョン表示
        let sdkVersion = RealVncSdkUtility.buildVersion()
        AppLogger.debug("RealVNC Server SDK Version = " + sdkVersion)
        
        // ローカル通知受信開始
        addLocalNotificationReceiver()
        
        // アクセサリ通信通知受信開始
        updateProtocolString()
        
        // 本アプリケーションについて
        buttonAboutApp.setTitle(Localize.localizedString("SS_01_002"), for: .normal)
        // 画面共有方法
        buttonShareScreen.setTitle(Localize.localizedString("SS_01_011"), for: .normal)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        AppLogger.debug()
        
        // 利用規約の表示が必要かどうか取得する
        let state = needShowUserAgreement()
        if state {
            // 利用規約表示する
            showUserAgreement()
        } else {
            // ローカル通知許諾確認ダイアログ表示
            confirmLocalNotificationPermission()
        }
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        AppLogger.debug()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        AppLogger.debug()
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
        
        if segue.source.isKind(of: UserAgreementViewController.self) {
            AppLogger.debug("From User Agreement")
            // 利用規約同意画面からの戻りのとき
            
            // 同意済みの場合は、設定を同意済みにする
            let userAgreementViewController = segue.source as! UserAgreementViewController
            if userAgreementViewController.agreed {
                // 同意済みにする
                AppGroupsManager.saveUserAgreementState(state: true)
                
                // ローカル通知許諾確認
                confirmLocalNotificationPermission()
            }
        } else {
            updateProtocolString()
        }
    }
    
    // MARK: - Rotation notification
    
    /// 表示方向変更通知
    ///
    /// - Parameter notification: 通知情報
    @objc func onOrientationDidChange(notification: NSNotification) {
        // デバイスの向きを取得
        let orientation: UIInterfaceOrientation = UIApplication.shared.statusBarOrientation
        AppLogger.debug("orientation = \(orientation)")
        
        if (orientation == UIInterfaceOrientation.landscapeLeft) || (orientation == UIInterfaceOrientation.landscapeRight) {
            // ランドスケープ用画像設定
            imageViewWallpaper.image = UIImage(named: "Wallpaper_Landscape")
            
            // アプリアイコン
            imageViewAppIcon.image = UIImage(named: "TLinkAppIcon_Landscape")
            // アプリ名ロゴ
            imageVIewAppName.image = UIImage(named: "AppNameLogo_Landscape")
            // AVXアイコン
            imageViewAvxIcon.image = UIImage(named: "AvxIcon_Landscape")
        } else {
            // ポートレート用画像設定
            imageViewWallpaper.image = UIImage(named: "Wallpaper")
            
            // アプリアイコン
            imageViewAppIcon.image = UIImage(named: "TLinkAppIcon")
            // アプリ名ロゴ
            imageVIewAppName.image = UIImage(named: "AppNameLogo")
            // AVXアイコン
            imageViewAvxIcon.image = UIImage(named: "AvxIcon")
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
    
    // MARK: - Private methods
    
    /// 各種Notification登録
    private func addNotifications() {
        // 回転通知
        NotificationCenter.default.addObserver(self, selector: #selector(onOrientationDidChange(notification:)),
                                               name: UIDevice.orientationDidChangeNotification, object: nil)
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
                message = Localize.localizedString("SS_02_003")
            // message = NSLocalizedString("SS_ERR-001", comment: "SS_ERR-001")
            case .licenseError:
                // ライセンス認証に失敗
                message = Localize.localizedString("SS_02_003")
            // message = NSLocalizedString("SS_ERR-002", comment: "SS_ERR-002")
            case .userAuthError:
                // ユーザー認証失敗
                message = Localize.localizedString("SS_02_003")
            // message = NSLocalizedString("SS_ERR-003", comment: "SS_ERR-003")
            case .remoteFeatureFailed:
                // リモートフィーチャー不一致
                message = Localize.localizedString("SS_02_003")
            // message = NSLocalizedString("SS_ERR-004", comment: "SS_ERR-004")
            case .internalServerError:
                // サーバー内部エラー
                message = Localize.localizedString("SS_02_003")
                // message = NSLocalizedString("SS_ERR-005", comment: "SS_ERR-005")
                
                if info.additionalMessage.isEmpty {
                    message += "\n\(info.additionalMessage) [Code:\(info.additionalIntValue)])"
                } else {
                    message += "\n[Code:\(info.additionalIntValue)])"
                }
            }
        }
        return message
    }
    
    // MARK: - User Agreement
    
    /// 利用規約の許諾ほ状態を確認し、利用規約の表示が必要か確認する。
    ///
    /// - Returns: true: 利用規約の表示必要、false: 利用規約の表示不要
    func needShowUserAgreement() -> Bool {
        // 許諾状態を取得する
        let state = AppGroupsManager.loadUserAgreementState()
        AppLogger.debug("value = \(state)")
        return !state
    }
    
    /// 利用規約を表示します。
    func showUserAgreement() {
        AppLogger.debug()
        
        // ストーリーボードからインタンス作成
        let storyboard = UIStoryboard(name: "UserAgreement", bundle: nil)
        let navController = storyboard.instantiateInitialViewController() as! UINavigationController
        navController.modalTransitionStyle = .coverVertical
        
        // RootViewController取得
        let viewController = navController.visibleViewController as! UserAgreementViewController
        
        // 利用規約ファイルパス生成
        let filePath = Bundle.main.path(forResource: "terms_of_service", ofType: "html")
        let url: URL = URL(fileURLWithPath: filePath!)
        
        // 利用規約ビューコントローラーセットアップ("アプリケーション利用規約", "同意する")
        viewController.setup(title: Localize.localizedString("SS_01_004"), url: url, agreeButtonTitle: Localize.localizedString("SS_01_010"))
        
        // モーダル表示
        present(navController, animated: true, completion: nil)
    }
    
    // MARK: - Local Notification Permission
    
    /// ローカル通知許諾を確認する
    ///
    /// - Parameter delayTime: 遅延実行時間
    func confirmLocalNotificationPermission(_ delayTime: TimeInterval) {
        AppLogger.debug()
        
        DispatchQueue.main.asyncAfter(deadline: .now() + delayTime) {
            // ローカル通知の許可をユーザーに問い合わせる
            LocalNotification.confirmLocalNotificationPermission()
        }
    }
    
    /// ローカル通知許諾を確認する
    func confirmLocalNotificationPermission() {
        AppLogger.debug()
        
        // 0.5秒後に表示
        confirmLocalNotificationPermission(0.5)
    }
}
