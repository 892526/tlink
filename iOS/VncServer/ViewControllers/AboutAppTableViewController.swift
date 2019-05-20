//
//  AboutAppTableViewController.swift
//  JKVncServer
//
//  Created by 板垣勇次 on 2018/06/29.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import JKEGCommonLib
import SafariServices
import UIKit

/// 「本アプリケーションについて」表示用テーブルビュークラス
class AboutAppTableViewController: UITableViewController {
    // セルテキスト
    @IBOutlet weak var lableAppOverview: UILabel!
    @IBOutlet weak var labelTermsOfUse: UILabel!
    @IBOutlet weak var labelOpenSourceLicenses: UILabel!
    @IBOutlet weak var labelVersion: UILabel!
    @IBOutlet weak var switchErrorMessageDebug: UISwitch!
    
    /// セクション番号定義
    ///
    /// - general: 一般
    /// - versi on: バージョン
    /// - debugFunction: デバッグ機能
    enum AboutAppTableViewSectionIndex: Int {
        case general = 0
        case version = 1
        case debugFunction = 2
    }
    
    /// 一般設定の行番号定義
    ///
    /// - Overview: アプリケーション概要
    /// - TermsOfUse: 利用規約
    /// - OSS: オープンソースライセンス
    enum AboutAppTableViewGeneralRowIndex: Int {
        case overview = 0
        case termsOfUse = 1
        case openSourceLicense = 2
    }
    
    /// デバッグ機能行番号定義
    ///
    /// - appLog: アプリログ
    /// - appExtentionLog: AppExtentionログ
    enum DebugFunctionRowIndex: Int {
        case sdkVersion = 0
        case protocolString = 1
        case frameRate = 2
        case errorMessageDebug = 3
        case initializeUseSettings = 4
        case appLog = 5
        case appExtentionLog = 6
        case maxIndex = 7
    }
    
    @IBOutlet weak var labelVersionValue: UILabel!
    @IBOutlet weak var labelSdkVersionValue: UILabel!
    @IBOutlet weak var labelProtocolString: UILabel!
    @IBOutlet weak var labelFrameRateValue: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false
        
        // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
        // self.navigationItem.rightBarButtonItem = self.editButtonItem
        
        // 戻るボタンのテキスト表示なし
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: UIBarButtonItem.Style.plain, target: nil, action: nil)
        
        // "本アプリケーションについて"
        title = Localize.localizedString("TID_5189")
        // アプリケーション概要
        lableAppOverview.text = Localize.localizedString("TID_5226")
        // アプリケーション利用規約
        labelTermsOfUse.text = Localize.localizedString("TID_5227")
        // オープンソースライセンス
        labelOpenSourceLicenses.text = Localize.localizedString("TID_5228")
        // バージョン
        labelVersion.text = Localize.localizedString("TID_5229")
        
        // アプリバージョン取得
        var version: String = ApplicationUtility.version()
        
        #if DEBUG
            // デバッグビルド時は、バージョンの後ろに"(DEBUG)"を追加
            version += " (DEBUG)"
        #endif
        
        // アプリバージョンセット
        labelVersionValue.text = version
        
        #if ENABLE_LOG
            // SDKバージョンセット
            labelSdkVersionValue.text = RealVncSdkUtility.buildVersion()
            
            // 使用プロトコルストリング表示
            updateProtocolString()
            
            // フレームレート表示
            updateFrameRate()
            
            // エラーメッセージデバッグ更新
            updateErrorMessageDebug()
        #endif // ENABLE_LOG
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: - Table view data source
    
    /// テーブルビューのセル選択通知
    ///
    /// - Parameters:
    ///   - tableView: テーブルビューのインスタンス
    ///   - indexPath: 選択行情報
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        #if ENABLE_LOG
            
            // デバッグ設定
            if indexPath.section == AboutAppTableViewSectionIndex.debugFunction.rawValue {
                switch indexPath.row {
                // アプリログ
                case DebugFunctionRowIndex.appLog.rawValue:
                    // デバッグログビューコントローラー表示する
                    showDebugLogViewController()
                    
                // AppExtentionログ
                case DebugFunctionRowIndex.appExtentionLog.rawValue:
                    // デバッグログビューコントローラー表示する
                    showBroadcastServerLog()
                    
                // ユーザー設定初期化
                case DebugFunctionRowIndex.initializeUseSettings.rawValue:
                    
                    confirmInitializeUserSettings()
                default:
                    print("undefine row index (Debug Function) ... >> rowIndex = " + String(indexPath.row))
                }
            }
            
        #endif // ENABLE_LOG
        
        tableView.deselectRow(at: indexPath, animated: true)
    }
    
    /// テーブルビューのセクション数を設定する
    ///
    /// - Parameter tableView: テーブルビューのインスタンス
    /// - Returns: セクション数
    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        #if ENABLE_LOG
            // デバッグ機能使用するときは、３セクション
            return 3
        #else
            // デバッグ機能使用しないときは、２セクション
            return 2
        #endif
    }
    
    /// テーブルビューの各セクション内の行数
    ///
    /// - Parameters:
    ///   - tableView: テーブルビューインスタンス
    ///   - section: テーブルビューのセクション番号
    /// - Returns: 行数
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        switch section {
        // 一般設定セクション
        case AboutAppTableViewSectionIndex.general.rawValue:
            return 3
        // バージョンセクション
        case AboutAppTableViewSectionIndex.version.rawValue:
            return 1
        // デバッグ設定セクション
        case AboutAppTableViewSectionIndex.debugFunction.rawValue:
            return DebugFunctionRowIndex.maxIndex.rawValue
        // その他
        default:
            return 0
        }
    }
    
    /*
     override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
     let cell = tableView.dequeueReusableCell(withIdentifier: "reuseIdentifier", for: indexPath)
     
     // Configure the cell...
     
     return cell
     }
     */
    
    /*
     // Override to support conditional editing of the table view.
     override func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
     // Return false if you do not want the specified item to be editable.
     return true
     }
     */
    
    /*
     // Override to support editing the table view.
     override func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
     if editingStyle == .delete {
     // Delete the row from the data source
     tableView.deleteRows(at: [indexPath], with: .fade)
     } else if editingStyle == .insert {
     // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
     }
     }
     */
    
    /*
     // Override to support rearranging the table view.
     override func tableView(_ tableView: UITableView, moveRowAt fromIndexPath: IndexPath, to: IndexPath) {
     
     }
     */
    
    /*
     // Override to support conditional rearranging of the table view.
     override func tableView(_ tableView: UITableView, canMoveRowAt indexPath: IndexPath) -> Bool {
     // Return false if you do not want the item to be re-orderable.
     return true
     }
     */
    
    // MARK: - Navigation
    
    #if ENABLE_LOG
        // In a storyboard-based application, you will often want to do a little preparation before navigation
        override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
            // Get the new view controller using segue.destinationViewController.
            // Pass the selected object to the new view controller.
            
            if segue.destination.isKind(of: ProtocolStringTableViewController.self) {
                // プロトコルストリング設定
                if let destViewController = segue.destination as? ProtocolStringTableViewController {
                    destViewController.selectedIndex = AppGroupsManager.loadProtocolStringIndex()
                }
            } else if segue.destination.isKind(of: FrameRateTableViewController.self) {
                // フレームレート設定
                if let destViewController = segue.destination as? FrameRateTableViewController {
                    destViewController.selectedIndex = AppGroupsManager.loadFrameRateIndex()
                }
            }
        }
        
        @IBAction func unwindAboutAppTopViewController(for unwindSegue: UIStoryboardSegue, towardsViewController subsequentVC: UIViewController) {
            // プロトコルストリング変更
            if unwindSegue.source.isKind(of: ProtocolStringTableViewController.self) {
                if let destViewController = unwindSegue.source as? ProtocolStringTableViewController {
                    print("\(destViewController.selectedIndex)")
                    AppGroupsManager.saveProtocolStringIndex(index: destViewController.selectedIndex)
                    updateProtocolString()
                }
            } else if unwindSegue.source.isKind(of: FrameRateTableViewController.self) {
                // フレームレート設定変更
                if let destViewController = unwindSegue.source as? FrameRateTableViewController {
                    print("\(destViewController.selectedIndex)")
                    AppGroupsManager.saveFrameRateIndex(index: destViewController.selectedIndex)
                    updateFrameRate()
                }
            }
        }
        
    #endif // ENABLE_LOG
    
    // MARK: - Private function
    
    /// SafariViewControllerでWebページを表示する。
    ///
    /// - Parameter urlString: WebページURL
    /// - Parameter titleBarText: タイトルバーテキスト
    private func showSafariViewController(urlString: String, titleBarText: String) {
        if let faqUrl = NSURL(string: urlString) {
            // SafariViewController生成
            let safariViewController = BaseSafariViewController(url: faqUrl as URL)
            safariViewController.title = titleBarText
            
            // モーダル表示する場合
            present(safariViewController, animated: true, completion: nil)
        }
    }
    
    #if _NOT_USED_FAQ_ // FAQ削除（2018/10/15）
        /// FAQのWebサイトを表示する
        private func showFaqWebPage() {
            let urlString = "http://www.kenwood.com/jp/products/car_audio/app/kenwood_music_info/faq.html"
            
            // SafariViewControllerで開く場合！！！
            
            let titleBarText = "よくあるご質問(FAQ)"
            // SafariViewControllerで表示する
            showSafariViewController(urlString: urlString, titleBarText: titleBarText)
            
            /*
             // Safari Browserで開く場合 !!!
             if let url = URL(string: urlString) {
             UIApplication.shared.open(url, options: [:], completionHandler: nil)
             }
             */
        }
    #endif // _NOT_USED_FAQ_
    
    // MARK: - Debug function
    
    #if ENABLE_LOG
        
        /// ユーザ設定を初期化するかどうかアラート表示
        private func confirmInitializeUserSettings() {
            AppLogger.debug()
            
            let buttonTextArray = ["Cancel", "Intialize"]
            AlertMessageUtility.show(owner: self, title: "", message: "Initialize user settings?", buttonTitles: buttonTextArray) { buttonIndex in
                AppLogger.debug("\(buttonIndex)")
                // "Intialize"選択
                if buttonIndex == 1 {
                    // ユーザ設定を初期化する
                    AppGroupsManager.reset()
                    
                    DispatchQueue.main.async {
                        // 使用プロトコルストリング表示
                        self.updateProtocolString()
                        
                        // フレームレート表示
                        self.updateFrameRate()
                        
                        // エラーメッセージデバッグ更新
                        self.updateErrorMessageDebug()
                    }
                    
                    AlertMessageUtility.show(owner: self, title: "",
                                             message: "User settings initialied.",
                                             type: AlertMessageUtility.AlertMessageUtilityType.typeOk,
                                             completion: nil)
                }
            }
        }
        
        /// デバッグログ表示用ビューコントローラを表示する
        private func showDebugLogViewController() {
            AppLoggerViewer.show(owner: self, logger: AppLogger.sharedInstance)
        }
        
        /// Broadcast Serverログ保存する
        ///
        /// - Parameter logText: ログテキストデータ
        /*
         private func saveBroadcastServerLog(_ logText: String) {
         let fileName = AppLogger.sharedInstance.makeLogFileName(format: appExtentionLogFileNameFormat)
         if AppLogger.sharedInstance.saveLog(fileName: fileName, logText: logText) {
         AppLogger.debug("AppExtention Debug Log >> BroadcastServerLog saved.")
         } else {
         AppLogger.debug("AppExtention Debug Log >> BroadcastServerLog save failed.")
         }
         }
         */
        
        /// Broadcast Serverログをビューで表示する
        private func showBroadcastServerLog() {
            // 共有ファイルのログ取得
            if let logText = AppExtentionLoggerUtility.loadLog() {
                // 一度保存する
                // saveBroadcastServerLog(logText)
                
                // ログビュー表示
                AppLoggerViewer.show(owner: self, logText: logText)
            } else {
                AlertMessageUtility.show(owner: self, title: "LOG", message: "Broadcast Server log is missing.", type: .typeOk) { _ in
                }
            }
        }
        
        func updateProtocolString() {
            let values = ["For Production", "For Debug"]
            
            let index = AppGroupsManager.loadProtocolStringIndex()
            if index < values.count {
                labelProtocolString.text = values[index]
            } else {
                labelProtocolString.text = "???"
            }
        }
        
        func updateFrameRate() {
            let values = FrameRateTableViewController.itemNames
            let index = AppGroupsManager.loadFrameRateIndex()
            if index < values.count {
                labelFrameRateValue.text = values[index]
            } else {
                labelFrameRateValue.text = "???"
            }
        }
        
        @IBAction func switchChanged(_ sender: Any) {
            AppLogger.debug()
            AppGroupsManager.saveErrorMessageDebug(switchErrorMessageDebug.isOn)
        }
        
        func updateErrorMessageDebug() {
            AppLogger.debug()
            let value = AppGroupsManager.loadErrorMessageDebug()
            switchErrorMessageDebug.setOn(value, animated: false)
        }
    #endif // ENABLE_LOG
}
