//
//  AppLoggerViewController.swift
//  JKCommon
//
//  Created by 板垣勇次 on 2018/06/18.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import MessageUI
import UIKit
import WebKit

// MARK: ログ表示用ViewControllerクラス

public class AppLoggerViewController: UIViewController, WKUIDelegate, WKNavigationDelegate, MFMailComposeViewControllerDelegate {
    /// ログデータ種別
    ///
    /// - AppLogger: AppLoggerデータ
    /// - Text: テキストデータ
    public enum LogDataType {
        case appLogger
        case text
    }
    
    /// ログデータ種別
    public var logDataType: LogDataType = LogDataType.appLogger
    
    /// ログデータテキスト
    public var logText: String?
    
    /// ログオブジェクト
    public var logger: AppLogger?
    
    private var textTitle: String = "LOG"
    
    private var webView: WKWebView!
    @IBOutlet weak var buttonLogClear: UIBarButtonItem!
    @IBOutlet weak var buttonLogSave: UIBarButtonItem!
    
    private var currentLogData: Data = Data()
    
    /// E-Mailの件名
    private var textEMailSubject: String = "iOS Appログファイル"
    
    /// E-Mailのメッセージ
    private var textEMailMessageBody: String = "iOSアプリの動作ログファイルです。"
    
    private let appExtentionLogFileNameFormat = "LOG_BroadcastServer_%@.txt"
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
        
        title = textTitle
        
        // WKWebView設定
        let webConfiguration = WKWebViewConfiguration()
        webConfiguration.preferences.minimumFontSize = 5
        
        // WKWebView作成
        webView = WKWebView(frame: .zero, configuration: webConfiguration)
        webView.uiDelegate = self
        webView.navigationDelegate = self
        view = webView
        
        // WebViewにテキストを設定する
        updateDisplay()
        
        let info = DebugSettings.loadSetting()
        if info == nil {
            // データが無いので初期値作成
            let result = DebugSettings.saveSetting(info: DebugSettingInfo())
            AppLogger.debug("DebugSettings.saveSetting >> result = \(result)")
        }
    }
    
    public override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    /*
     // MARK: - Navigation
     
     // In a storyboard-based application, you will often want to do a little preparation before navigation
     override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
     // Get the new view controller using segue.destinationViewController.
     // Pass the selected object to the new view controller.
     }
     */
    
    // MARK: - Button Action
    
    /// 閉じるボタンをタップしたとき
    ///
    /// - Parameter sender: 送信元
    @IBAction func tappedDone(_: Any) {
        // ViewControllerを閉じる
        dismiss(animated: true) {
        }
    }
    
    /// 設定ボタンをタップしたとき
    ///
    /// - Parameter sender: 送信元
    @IBAction func tappedSetting(_: Any) {
    }
    
    /// ログクリアボタンをタップしたとき
    ///
    /// - Parameter sender: 送信元
    @IBAction func tappedLogClear(_: Any) {
        // ActionSheet作成
        let alertController = UIAlertController(title: "ログクリア", message: "ログをどのようにしますか？", preferredStyle: UIAlertController.Style.actionSheet)
        
        alertController.addAction(UIAlertAction(title: "現在のログをクリアする", style: UIAlertAction.Style.destructive, handler: { _ in
            
            if self.logDataType == .appLogger {
                // ログをクリアする
                self.logger?.clearLogMessage()
            } else {
                // ログをクリアする
                self.logText = String.Empty
                AppExtentionLoggerUtility.removeLog()
            }
            
            // 表示更新
            self.updateDisplay()
        }))
        alertController.addAction(UIAlertAction(title: "保存ログをすべて削除する", style: UIAlertAction.Style.default, handler: { _ in
            
            // 保存しているすべてのログを削除する
            self.logger?.clearSaveLogFiles()
            
            // Alertメッセージ表示
            AlertMessageUtility.show(owner: self, title: self.textTitle, message: "ログファイルを削除しました。", type: .typeOk, completion: { selectedButtonType in
                
                switch selectedButtonType {
                case .typeOk:
                    print("OK")
                default:
                    print("Unknown...")
                }
            })
        }))
        alertController.addAction(UIAlertAction(title: "キャンセル", style: UIAlertAction.Style.cancel, handler: nil))
        
        // ActionSheet表示
        present(alertController, animated: true, completion: nil)
    }
    
    private let messageAppLoggerNoInstance = "AppLoggerインスタンスエラー"
    private let messageSaveLogSuccess = "ログファイルを保存しました。\n保存ファイルは、iTunesのファイル共有で取得できます。"
    private let messageSaveLogFailed = "ログファイルの保存に失敗しました。"
    private let messageSaveLogNoData = "ログデータがありません。"
    
    /// ログ保存ボタンをタップしたとき
    ///
    /// - Parameter sender: 送信元
    @IBAction func tappedLogSave(_: Any) {
        var displayMessage: String = ""
        
        if logDataType == .appLogger {
            if let loggerObj = logger {
                // ファイルに保存する
                if loggerObj.saveLog() {
                    displayMessage = messageSaveLogSuccess
                } else {
                    displayMessage = messageSaveLogFailed
                }
            } else {
                displayMessage = messageAppLoggerNoInstance
            }
        } else {
            if let log = self.logText {
                let fileName = AppLogger.sharedInstance.makeLogFileName(format: appExtentionLogFileNameFormat)
                if AppLogger.sharedInstance.saveLog(fileName: fileName, logText: log) {
                    displayMessage = messageSaveLogSuccess
                } else {
                    displayMessage = messageSaveLogFailed
                }
            } else {
                displayMessage = messageSaveLogNoData
            }
        }
        
        // Alertメッセージ表示
        AlertMessageUtility.show(owner: self, title: textTitle, message: displayMessage, type: .typeOk, completion: { selectedButtonType in
            
            switch selectedButtonType {
            case .typeOk:
                print("OK")
            default:
                print("Unknown...")
            }
        })
    }
    
    /// E-Mailで送信
    ///
    /// - Parameter sender: 送信元
    @IBAction func tappedEmail(_: Any) {
        if !setupEMail() {
            // エラーメッセージ表示
            AlertMessageUtility.show(owner: self, title: "ERROR", message: "E−mailの初期化に失敗しました。", type: .typeOk, completion: nil)
        }
    }
    
    /// ログ表示更新する
    public func updateDisplay() {
        // AppLoggerデータ使用するとき
        if logDataType == LogDataType.appLogger {
            if let tmp = (logger?.getLogBuffer().data(using: String.Encoding.utf8)) {
                currentLogData = tmp
            }
        } else {
            if let tmp = self.logText?.data(using: .utf8) {
                currentLogData = tmp
            }
        }
        
        // ログ表示
        webView.load(currentLogData, mimeType: "text/plane", characterEncodingName: String.Encoding.utf8.description, baseURL: NSURL() as URL)
    }
    
    // MARK: - WebView delegate
    
    public func webView(_: WKWebView, didFinish _: WKNavigation!) {
        // Webのロード完了後に実行されるメソッド。WKNavigationDelegateのdelegateを通しておくことを忘れないこと
        print("didFinish")
    }
    
    // MARK: - Mail
    
    /// E-Mailセットアップします。
    
    /// E-Mailのメッセージボディ文字列を取得します。
    ///
    /// - Returns: メッセージボディ文字列
    func emailMessageBody() -> String {
        let message: String = String(format: "%@\n\n%@\n%@\n%@\n%@\n%@\n%@\n",
                                     textEMailMessageBody,
                                     "--- App Info. ---",
                                     String(format: " BundleName : \(ApplicationUtility.bundleName())"),
                                     String(format: " BundleID   : \(ApplicationUtility.bundleIdentifier())"),
                                     String(format: " Version    : \(ApplicationUtility.version())"),
                                     String(format: " Build      : \(ApplicationUtility.buildVersion())"),
                                     "-----------------"
        )
        
        return message
    }
    
    /// E-mail設定を行う。
    ///
    /// - Returns: true: 成功、false: 失敗
    func setupEMail() -> Bool {
        // メールを送信できるかチェック
        if MFMailComposeViewController.canSendMail() == false {
            print("Email Send Failed")
            return false
        }
        
        var mailAddress: String = String.Empty
        
        // 送信先メールアドレス設定取得
        let info = DebugSettings.loadSetting()
        if let address = info?.accountAddress {
            mailAddress = address
        }
        
        // ログデータ
        let logData: Data = currentLogData
        // ログファイル名
        let logFileName: String = (logDataType == .appLogger) ?
            (logger?.makeLogFileName())! :
            AppLogger.sharedInstance.makeLogFileName(format: appExtentionLogFileNameFormat)
        
        // メッセージボディ文字列取得
        let messageBody = emailMessageBody()
        
        let mailViewController = MFMailComposeViewController()
        let toRecipients = [mailAddress] // Toのアドレス指定
        // var CcRecipients = ["cc@1gmail.com","Cc2@1gmail.com"] //Ccのアドレス指定
        let ccRecipients = [String]()
        // var BccRecipients = ["Bcc@1gmail.com","Bcc2@1gmail.com"] //Bccのアドレス指定
        let bccRecipients = [String]()
        
        mailViewController.mailComposeDelegate = self
        
        // 件名セット
        mailViewController.setSubject(textEMailSubject)
        // TOアドレスの表示
        mailViewController.setToRecipients(toRecipients)
        // CCアドレスの表示
        mailViewController.setCcRecipients(ccRecipients)
        // BCCアドレスの表示
        mailViewController.setBccRecipients(bccRecipients)
        // メール本文
        mailViewController.setMessageBody(messageBody, isHTML: false)
        
        // 添付データ（ログデータ）
        mailViewController.addAttachmentData(logData, mimeType: "text/plane", fileName: logFileName)
        
        // メーラー起動
        present(mailViewController, animated: true, completion: nil)
        
        return true
    }
    
    /// MailComposerの実行結果通知
    ///
    /// - Parameters:
    ///   - controller: Mailコントローラー
    ///   - result: 結果
    ///   - error: エラー情報
    public func mailComposeController(_: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error _: Error?) {
        switch result {
        case MFMailComposeResult.cancelled:
            print("Email Send Cancelled")
            
        case MFMailComposeResult.saved:
            print("Email Saved as a Draft")
            
        case MFMailComposeResult.sent:
            print("Email Sent Successfully")
            
        case MFMailComposeResult.failed:
            print("Email Send Failed")
        }
        // MailComposerを閉じる
        dismiss(animated: true, completion: nil)
    }
}
