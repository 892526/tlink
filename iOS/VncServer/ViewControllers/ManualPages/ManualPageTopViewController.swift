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
        
        // タイトル表示(チュートリアル)
        title = Localize.localizedString("TID_5292")
        
        // 初期設定
        setButtonTilte(button: buttonInitiOperation, titleText: Localize.localizedString("TID_5302"))
        // 画面共有開始
        setButtonTilte(button: buttonStartOperation, titleText: Localize.localizedString("TID_5303"))
        // 画面共有停止-1
        setButtonTilte(button: buttonStopOperation01, titleText: Localize.localizedString("TID_5304"))
        // 画面共有停止-2
        setButtonTilte(button: buttonStopOperation02, titleText: Localize.localizedString("TID_5305"))
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
        // 説明文言
        let pageDescriptions = [
            Localize.localizedString("TID_5308"),
            Localize.localizedString("TID_5309"),
            Localize.localizedString("TID_5310"),
            Localize.localizedString("TID_5311"),
            Localize.localizedString("TID_5312"),
            Localize.localizedString("TID_5313")
        ]
        
        // 表示画像ファイル名
        let pageImageNames = [
            "iOS_Tutorial_InitialSetting_01",
            "iOS_Tutorial_InitialSetting_02",
            "iOS_Tutorial_InitialSetting_03",
            "iOS_Tutorial_InitialSetting_04",
            "iOS_Tutorial_BluetoothParing_02_01",
            "iOS_Tutorial_BluetoothParing_02_02"
        ]
        
        // ページ種別
        let pageTypes = [
            ManualPageInfo.ManualPageType.normal,
            ManualPageInfo.ManualPageType.normal,
            ManualPageInfo.ManualPageType.normal,
            ManualPageInfo.ManualPageType.normal,
            ManualPageInfo.ManualPageType.headUnit
        ]
        
        // ページ情報リスト作成する
        var pageInfos: [ManualPageInfo] = []
        for index in 0 ..< pageTypes.count {
            var info: ManualPageInfo?
            if pageTypes[index] == ManualPageInfo.ManualPageType.normal {
                // アプリ説明お用ページ商法
                info = ManualPageInfo(message: pageDescriptions[index], imageName: pageImageNames[index])
            } else {
                // HeadUnit用ページ情報
                info = HUManualPageInfo(fristMesage: pageDescriptions[index], firstImageName: pageImageNames[index],
                                        secondMessage: pageDescriptions[index + 1], secondImageName: pageImageNames[index + 1])
            }
            
            if let info = info {
                // リストに追加
                pageInfos.append(info)
            }
        }
        
        // 詳細画面表示（画面共有の初期設定）
        pushDetailViewController(headerTitle: Localize.localizedString("TID_5302"), pageInfos: pageInfos)
    }
    
    /// 共有開始ボタンタップ
    ///
    /// - Parameter sender: 送信元インスタンス
    @IBAction func showStartView(_ sender: Any) {
        // 説明文言
        let pageDescriptions = [
            Localize.localizedString("TID_5314"),
            Localize.localizedString("TID_5315"),
            Localize.localizedString("TID_5316")
        ]
        
        // 画像ファイル名
        let pageImageNames = [
            "iOS_Tutorial_StartScreenSharing_01",
            "iOS_Tutorial_StartScreenSharing_02",
            "iOS_Tutorial_StartScreenSharing_03"
        ]
        
        // ページ情報リスト作成する
        var pageInfos: [ManualPageInfo] = []
        for index in 0 ..< pageDescriptions.count {
            let info = ManualPageInfo(message: pageDescriptions[index], imageName: pageImageNames[index])
            pageInfos.append(info)
        }
        
        // 詳細画面表示(画面共有を開始する)
        pushDetailViewController(headerTitle: Localize.localizedString("TID_5303"), pageInfos: pageInfos)
    }
    
    /// 共有停止（1）ボタンタップ
    ///
    /// - Parameter sender: 送信元インスタンス
    @IBAction func showStopView(_ sender: Any) {
        // 説明文言
        let pageDescriptions = [
            Localize.localizedString("TID_5317"),
            Localize.localizedString("TID_5318")
        ]
        
        // 画像ファイル名
        let pageImageNames = [
            "iOS_Tutorial_StopScreenSharing1_01",
            "iOS_Tutorial_StopScreenSharing1_02"
        ]
        
        // ページ情報リスト作成する
        var pageInfos: [ManualPageInfo] = []
        for index in 0 ..< pageDescriptions.count {
            let info = ManualPageInfo(message: pageDescriptions[index], imageName: pageImageNames[index])
            pageInfos.append(info)
        }
        
        // 詳細画面表示(画面共有を停止する(1))
        pushDetailViewController(headerTitle: Localize.localizedString("TID_5304"), pageInfos: pageInfos)
    }
    
    /// 共有停止（2）ボタンタップ
    ///
    /// - Parameter sender: 送信元インスタンス
    @IBAction func showStopView2(_ sender: Any) {
        // 説明文言
        let pageDescriptions = [
            Localize.localizedString("TID_5319"),
            Localize.localizedString("TID_5320")
        ]
        
        // 画像ファイル名
        let pageImageNames = [
            "iOS_Tutorial_StopScreenSharing2_01",
            "iOS_Tutorial_StopScreenSharing2_02"
        ]
        
        // ページ情報リスト作成する
        var pageInfos: [ManualPageInfo] = []
        for index in 0 ..< pageDescriptions.count {
            let info = ManualPageInfo(message: pageDescriptions[index], imageName: pageImageNames[index])
            pageInfos.append(info)
        }
        
        // 詳細画面表示(画面共有を停止する(2))
        pushDetailViewController(headerTitle: Localize.localizedString("TID_5305"), pageInfos: pageInfos)
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
    ///   - headerTitle: ナビゲーションバータイトル
    ///   - pageInfos: ページ情報
    private func pushDetailViewController(headerTitle: String, pageInfos: [ManualPageInfo]) {
        AppLogger.debug()
        
        // ストーリーボードロード
        let storyboard = UIStoryboard(name: "PageManagerViewController", bundle: nil)
        
        // ビューコントローラー生成
        if let viewController = storyboard.instantiateInitialViewController() as? PageManagerViewController {
            // ナビゲーションバータイトル
            viewController.title = headerTitle
            
            // ページ情報セット
            viewController.pageInfos = pageInfos
            
            // 戻るボタンのテキスト非表示
            navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: .plain, target: nil, action: nil)
            
            // ビューコントローラー表示(push)
            navigationController?.pushViewController(viewController, animated: true)
        }
    }
}
