//
//  ManualPageTopViewController.swift
//  VncServer
//
//  Created by 板垣勇次 on 2018/09/05.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import JKEGCommonLib
import UIKit

/// 操作マニュアルトップページ用ビューコントローラー
class ManualPageTopViewController: UIViewController {
    /// 初期化設定
    @IBOutlet weak var buttonInitiOperation: PanelButton!
    
    /// 開始方法
    @IBOutlet weak var buttonStartOperation: PanelButton!
    
    /// 終了方法(1)
    @IBOutlet weak var buttonStopOperation01: PanelButton!
    
    /// 終了方法(2)
    @IBOutlet weak var buttonStopOperation02: PanelButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
        
        // タイトル表示
        title = Localize.localizedString("SS_01_011")
        
        // 画面共有の初期化設定方法
        setButtonTilte(button: buttonInitiOperation, titleText: Localize.localizedString("SS_01_102"))
        // 画面共有を開始する方法
        setButtonTilte(button: buttonStartOperation, titleText: Localize.localizedString("SS_01_103"))
        // 画面共有を停止する方法(1)
        setButtonTilte(button: buttonStopOperation01, titleText: Localize.localizedString("SS_01_104"))
        // 画面共有を停止する方法(2)
        setButtonTilte(button: buttonStopOperation02, titleText: Localize.localizedString("SS_01_105"))
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: - Navigation
    
    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
        // if segue.destination is PageManagerViewController {
        // }
    }
    
    // MARK: - Button Actions
    
    /// 初期設定方法ボタンタップ
    ///
    /// - Parameter sender: 送信元インスタンス
    @IBAction func showInitialSettingView(_ sender: Any) {
        // タイトル文言
        let pageTitles = [
            // "(1)[設定]を起動"
            Localize.localizedString("SS_01_102_01_01"),
            // "(2)コントロールセンター表示"
            Localize.localizedString("SS_01_102_02_01"),
            // "(3)カスタマイズを表示",
            Localize.localizedString("SS_01_102_03_01"),
            // "(4)画面収録追加"
            Localize.localizedString("SS_01_102_04_01"),
            // "(5)画面収録追加完了"
            Localize.localizedString("SS_01_102_05_01")
        ]
        // 説明文言
        let pageDescriptions = [
            // "iOSの[設定]を起動します。"
            Localize.localizedString("SS_01_102_01_02"),
            // "iOSの[設定]の[コントロールセンター]をタップします。",
            Localize.localizedString("SS_01_102_02_02"),
            // "[コントロールをカスタマイズ]をタップします。",
            Localize.localizedString("SS_01_102_03_02"),
            // "[画面収録]の[+]ボタンをタップして、追加します。"
            Localize.localizedString("SS_01_102_04_02"),
            // "[画面収録]が[含める]に移動すれば初期設定完了です。"
            Localize.localizedString("SS_01_102_04_02")
        ]
        
        // 表示画像ファイル名
        let pageImageNames = [
            "Setting01",
            "Setting02",
            "Setting03",
            "Setting04",
            "Setting05"
        ]
        
        // 詳細画面表示（画面共有の初期設定）
        pushDetailViewController(title: Localize.localizedString("SS_01_102"), pageTitles: pageTitles, pageDescriptions: pageDescriptions, imageNames: pageImageNames)
    }
    
    /// 共有開始ボタンタップ
    ///
    /// - Parameter sender: 送信元インスタンス
    @IBAction func showStartView(_ sender: Any) {
        // タイトル文言
        let pageTitles = [
            // "(1)コントロールセンター表示"
            Localize.localizedString("SS_01_103_01_01"),
            // "(2)画面収録ボタン"
            Localize.localizedString("SS_01_103_02_01"),
            // "(3)ブロードキャスト開始"
            Localize.localizedString("SS_01_103_03_01"),
            // "(4)USBケーブルを接続"
            Localize.localizedString("SS_01_103_04_01")
        ]
        
        // 説明文言
        let pageDescriptions = [
            // "コントロールセンター表示します。",
            Localize.localizedString("SS_01_103_01_02"),
            // "[画面収録]ボタンをタップします。",
            Localize.localizedString("SS_01_103_02_02"),
            // "アプリケーションリストから[T-Link]を選択し、[ブロードキャストを開始]をタップします。このとき、マイクは[オフ]にします。",
            Localize.localizedString("SS_01_103_03_02"),
            // "画面収録アイコンが赤くなるのを確認したら、USBケーブルを接続します。"
            Localize.localizedString("SS_01_103_04_02")
        ]
        
        // 画像ファイル名
        let pageImageNames = [
            "Start01",
            "Start02",
            "Start03",
            "Start04"
        ]
        // 詳細画面表示(画面共有を開始する)
        pushDetailViewController(title: Localize.localizedString("SS_01_103"), pageTitles: pageTitles, pageDescriptions: pageDescriptions, imageNames: pageImageNames)
    }
    
    /// 共有停止（1）ボタンタップ
    ///
    /// - Parameter sender: 送信元インスタンス
    @IBAction func showStopView(_ sender: Any) {
        // タイトル文言
        let pageTitles = [
            // "(1)ステーターバーをタップ",
            Localize.localizedString("SS_01_104_01_01"),
            // "(2)ブロードキャスト停止"
            Localize.localizedString("SS_01_104_02_01")
        ]
        
        // 説明文言
        let pageDescriptions = [
            // "画面上部のステータスバーをタップします。",
            Localize.localizedString("SS_01_104_01_01"),
            // "ダイアログの[停止]をタップすると、ブロードキャスト停止します。"
            Localize.localizedString("SS_01_104_02_01")
        ]
        
        // 画像ファイル名
        let pageImageNames = [
            "Stop1_01",
            "Stop1_02"
        ]
        // 詳細画面表示(画面共有を停止する(1))
        pushDetailViewController(title: Localize.localizedString("SS_01_104"), pageTitles: pageTitles, pageDescriptions: pageDescriptions, imageNames: pageImageNames)
    }
    
    /// 共有停止（2）ボタンタップ
    ///
    /// - Parameter sender: 送信元インスタンス
    @IBAction func showStopView2(_ sender: Any) {
        // タイトル文言
        let pageTitles = [
            // "(1)コントロールセンター表示",
            Localize.localizedString("SS_01_105_01_01"),
            // "(2)ブロードキャスト停止"
            Localize.localizedString("SS_01_105_02_01")
        ]
        
        // 説明文言
        let pageDescriptions = [
            // "コントロールセンターを表示します。",
            Localize.localizedString("SS_01_105_01_02"),
            // "画面収録アイコンをタップすると、ブロードキャスト停止します。"
            Localize.localizedString("SS_01_105_02_02")
        ]
        
        // 画像ファイル名
        let pageImageNames = [
            "Stop2_01",
            "Stop2_02"
        ]
        // 詳細画面表示(画面共有を停止する(2))
        pushDetailViewController(title: Localize.localizedString("SS_01_105"), pageTitles: pageTitles, pageDescriptions: pageDescriptions, imageNames: pageImageNames)
    }
    
    // MARK: - Private methods
    
    /// ボタンにタイトルを設定する
    ///
    /// - Parameters:
    ///   - button: ボタン
    ///   - titleText: タイトル
    private func setButtonTilte(button: UIButton, titleText: String) {
        button.setTitle(titleText, for: UIControl.State.normal)
    }
    
    /// 詳細ページビューコントローラーを表示(push)する
    ///
    /// - Parameters:
    ///   - title: 画面タイトル
    ///   - pageTitles: ページタイトル
    ///   - pageDescriptions: 説明文
    ///   - imageNames: イメージファイル名
    private func pushDetailViewController(title: String, pageTitles: [String], pageDescriptions: [String], imageNames: [String]) {
        AppLogger.debug()
        
        // ストーリーボードロード
        let storyboard = UIStoryboard(name: "PageManagerViewController", bundle: nil)
        
        // ビューコントローラー生成
        if let viewController = storyboard.instantiateInitialViewController() as? PageManagerViewController {
            // 画面タイトルセット
            viewController.title = title
            // ページタイトルセット
            viewController.pageTitles = pageTitles
            // 説明文セット
            viewController.pageDescriptions = pageDescriptions
            // 画面ファイル名セット
            viewController.pageImageNames = imageNames
            
            // 戻るボタンのテキスト非表示
            navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: .plain, target: nil, action: nil)
            
            // ビューコントローラー表示(push)
            navigationController?.pushViewController(viewController, animated: true)
        }
    }
}
