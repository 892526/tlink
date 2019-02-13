//
//  BaseWebViewController.swift
//  JKVncServer
//
//  Created by 板垣勇次 on 2018/06/29.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import JKEGCommonLib
import UIKit
import WebKit

class BaseWebViewController: UIViewController, WKUIDelegate, WKNavigationDelegate {
    private var webView: WKWebView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: .plain, target: nil, action: nil)
        
        // WKWebView設定
        let webConfiguration = WKWebViewConfiguration()
        webConfiguration.preferences.minimumFontSize = 5
        
        // WKWebView作成
        webView = WKWebView(frame: .zero, configuration: webConfiguration)
        webView.uiDelegate = self
        webView.navigationDelegate = self
        view = webView
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        // タイトルテキスト更新
        updateTitleView()
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        AppLogger.debug()
        
        // 回転通知解除
        removeNotification()
        navigationItem.titleView = nil
    }
    
    override func didReceiveMemoryWarning() {
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
    
    /// HTMLファイルをロードする
    ///
    /// - Parameter url: HTMLファイルURL
    func loadHtml(url: URL) {
        let request: URLRequest = URLRequest(url: url)
        webView.load(request)
    }
    
    /// HTML文字列をロードする
    ///
    /// - Parameter htmlString: HTML文字列
    func loadHtmlString(htmlString: String) {
        webView.loadHTMLString(htmlString, baseURL: nil)
    }
    
    // MARK: - Rotation notification
    
    /// 回転通知登録
    func addNotification() {
        AppLogger.debug()
        
        // 回転通知
        NotificationCenter.default.addObserver(self, selector: #selector(onOrientationDidChange(notification:)),
                                               name: UIDevice.orientationDidChangeNotification, object: nil)
    }
    
    /// 回転通知解除
    func removeNotification() {
        AppLogger.debug()
        NotificationCenter.default.removeObserver(self, name: UIDevice.orientationDidChangeNotification, object: nil)
    }
    
    /// 表示方向変更通知
    ///
    /// - Parameter notification: 通知情報
    @objc func onOrientationDidChange(notification: NSNotification) {
        AppLogger.debug()
        
        updateTitleView()
    }
    
    /// タイトルビューを更新する
    private func updateTitleView() {
        // デバイスの向きに応じたタイトルエリアを変更する
        if let titleViewControl = navigationItem.titleView {
            // デバイスの向きを取得
            let orientation: UIInterfaceOrientation = UIApplication.shared.statusBarOrientation
            AppLogger.debug("orientation = \(orientation)")
            
            let titleView = titleViewControl as! UILabel
            let frameValue = getTitleViewFrameSize()
            AppLogger.debug("orientation = \(orientation.rawValue), frameValue = \(frameValue)")
            
            if orientation.isPortrait {
                titleView.frame = frameValue
                titleView.sizeToFit()
            } else {
                titleView.frame = frameValue
                titleView.sizeToFit()
            }
        }
    }
    
    /// タイトルサイズ取得する
    ///
    /// - Returns: タイトルエリアサイズ
    private func getTitleViewFrameSize() -> CGRect {
        var value = view.frame.width * 0.77
        if value == 0.0 {
            value = 200.0
        }
        AppLogger.debug("width = \(value)")
        let orientation: UIInterfaceOrientation = UIApplication.shared.statusBarOrientation
        if orientation.isPortrait {
            return CGRect(x: 0, y: 0, width: value, height: 40.0)
        } else {
            return CGRect(x: 0, y: 0, width: value, height: 30.0)
        }
    }
    
    // MARK: - public methods
    
    /// タイトルビューをセットアップする
    ///
    /// - Parameter titleText: タイトル
    public func setupTitleView(_ titleText: String) {
        let viewRect = getTitleViewFrameSize()
        // 文字列が長い場合に入りきるように動的にフォントサイズを変更するようにUILabelを作成して
        // NavigationTileViewに設定する
        let titleLabel = UILabel(frame: viewRect)
        titleLabel.font = UIFont.boldSystemFont(ofSize: 16.0)
        titleLabel.text = titleText
        titleLabel.adjustsFontSizeToFitWidth = true
        titleLabel.backgroundColor = UIColor.clear
        titleLabel.textAlignment = .center
        titleLabel.numberOfLines = 2
        titleLabel.sizeToFit()
        
        navigationItem.titleView = titleLabel
        
        // 元のタイトルをクリア
        title = nil
        
        /// 回転通知登録
        addNotification()
    }
}
