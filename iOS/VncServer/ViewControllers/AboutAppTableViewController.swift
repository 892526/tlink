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

class AboutAppTableViewController: UITableViewController {
    /// セクション番号定義
    ///
    /// - general: 一般
    /// - version: バージョン
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
    /// - DebugFunction: デバッグ機能
    enum AboutAppTableViewGeneralRowIndex: Int {
        case overview = 0
        case faq = 1
        case termsOfUse = 2
        case openSourceLicense = 3
    }
    
    /// デバッグ機能行番号定義
    ///
    /// - appLog: アプリログ
    /// - appExtentionLog: AppExtentionログ
    enum DebugFunctionRowIndex: Int {
        case sdkVersion = 0
        case protocolString = 1
        case appLog = 2
        case appExtentionLog = 3
    }
    
    @IBOutlet weak var labelVersionValue: UILabel!
    @IBOutlet weak var labelSdkVersionValue: UILabel!
    @IBOutlet weak var labelProtocolString: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false
        
        // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
        // self.navigationItem.rightBarButtonItem = self.editButtonItem
        
        // 戻るボタンのテキスト表示なし
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: UIBarButtonItemStyle.plain, target: nil, action: nil)
        
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
        switch indexPath.section {
        // 一般設定
        case AboutAppTableViewSectionIndex.general.rawValue:
            switch indexPath.row {
            // FAQサイト表示
            case AboutAppTableViewGeneralRowIndex.faq.rawValue:
                showFaqWebPage()
                
            default:
                print("undefine row index (General) ... >> rowIndex = " + String(indexPath.row))
            }
        // デバッグ設定
        case AboutAppTableViewSectionIndex.debugFunction.rawValue:
            
            #if ENABLE_LOG
                
                switch indexPath.row {
                // アプリログ
                case DebugFunctionRowIndex.appLog.rawValue:
                    // デバッグログビューコントローラー表示する
                    showDebugLogViewController()
                    
                // AppExtentionログ
                case DebugFunctionRowIndex.appExtentionLog.rawValue:
                    // デバッグログビューコントローラー表示する
                    showBroadcastServerLog()
                    
                default:
                    print("undefine row index (Debug Function) ... >> rowIndex = " + String(indexPath.row))
                }
                
            #endif // ENABLE_LOG
            
        // その他
        default:
            print("no section index ...")
        }
        
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
            return 4
        // バージョンセクション
        case AboutAppTableViewSectionIndex.version.rawValue:
            return 1
        // デバッグ設定セクション
        case AboutAppTableViewSectionIndex.debugFunction.rawValue:
            return 4
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
                if let destViewController = segue.destination as? ProtocolStringTableViewController {
                    destViewController.selectedIndex = AppGroupsManager.loadProtocolStringIndex()
                }
            }
        }
        
        @IBAction func unwindAboutAppTopViewController(for unwindSegue: UIStoryboardSegue, towardsViewController subsequentVC: UIViewController) {
            if unwindSegue.source.isKind(of: ProtocolStringTableViewController.self) {
                if let destViewController = unwindSegue.source as? ProtocolStringTableViewController {
                    print("\(destViewController.selectedIndex)")
                    AppGroupsManager.saveProtocolStringIndex(index: destViewController.selectedIndex)
                    updateProtocolString()
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
    
    // MARK: - Debug function
    
    #if ENABLE_LOG
        
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
                AlertMessageUtility.show(owner: self, title: "LOG", message: "Broadcasr Serverログがありません。", type: .typeOk) { _ in
                }
            }
        }
        
        func updateProtocolString() {
            let values = ["本番用", "デバッグ用"]
            
            let index = AppGroupsManager.loadProtocolStringIndex()
            if index < values.count {
                labelProtocolString.text = values[index]
            } else {
                labelProtocolString.text = "???"
            }
        }
        
    #endif // ENABLE_LOG
}
