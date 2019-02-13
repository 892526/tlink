//
//  UserAgreementViewController.swift
//  VncServer
//
//  Created by 板垣勇次 on 2018/11/01.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import JKEGCommonLib
import UIKit
import WebKit

/// 利用規約表示用ビューコントローラークラス
class UserAgreementViewController: UIViewController, WKUIDelegate, WKNavigationDelegate {
    /// WebKit
    private var webView: WKWebView!
    
    /// ヘッダタイトル
    private var headerTitle: String = ""
    
    /// コンテンツURL
    private var contentUrl: URL?
    
    /// 同意ボタンタイトル
    private var agreeTitle: String = ""
    
    /// observeの結果を格納
    private var keyValueObservations = [NSKeyValueObservation]()
    
    /// コンテンツロード完了
    private var isContentLoaded: Bool = false
    
    /// 同意済みかどうか
    private var isAgreed: Bool = false
    
    /// 同意ボタン
    @IBOutlet weak var buttonAgree: UIBarButtonItem!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
        
        // タイトル設定
        setupTitleView(headerTitle)
        
        // 同意ボタンタイトル設定
        buttonAgree.title = agreeTitle
        
        // WKWebView設定
        let webConfiguration = WKWebViewConfiguration()
        webConfiguration.preferences.minimumFontSize = 5
        
        // WKWebView作成
        webView = WKWebView(frame: .zero, configuration: webConfiguration)
        webView.uiDelegate = self
        webView.navigationDelegate = self
        view = webView
        
        // スクロール監視開始
        addScrollObserving()
        
        // HHTMLファイルロードする
        loadHtml(contentUrl!)
        
        // ツールバー非表示
        navigationController?.setToolbarHidden(true, animated: false)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        // ヘッダタイトル更新
        updateTitleView()
    }
    
    deinit {
        // WebKitのデリゲート外す
        webView.uiDelegate = nil
        webView.navigationDelegate = nil
        
        // スクロール監視停止
        removeScrollObserving()
        
        // 回転監視停止
        removeNotification()
        navigationItem.titleView = nil
    }
    
    /*
     // MARK: - Navigation
     
     // In a storyboard-based application, you will often want to do a little preparation before navigation
     override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
     // Get the new view controller using segue.destination.
     // Pass the selected object to the new view controller.
     }
     */
    
    // MARK: - Actions
    
    /// [同意]ボタンタップイベント
    ///
    /// - Parameter sender: [同意]ボタンのインスタンス
    @IBAction func tappedAgree(_ sender: Any) {
        AppLogger.debug()
        
        // 同意済みにする
        isAgreed = true
        
        // 呼び出し元画面に戻す
        performSegue(withIdentifier: "unwindToTopViewControllerWithSegueFromUserAgreement", sender: self)
    }
    
    // MARK: - webkitView navigation delegate
    
    // 読み込み開始
    func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
        AppLogger.debug()
    }
    
    // 読み込み完了
    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        AppLogger.debug()
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
            // ロード完了
            self.isContentLoaded = true
        }
    }
    
    // 読み込み失敗
    func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
        AppLogger.debug()
    }
    
    // MARK: - KVO Setting and Observing
    
    /// KVOを追加
    private func addScrollObserving() {
        // コンテンツオフセット変更通知を受け取る
        let keyValueObservation = webView.scrollView.observe(\.contentOffset, options: [.new, .old]) { _, change in
            if change.newValue == nil {
                return
            }
            
            // 最下端までスクロールしたか
            let target = self.webView.scrollView.contentOffset.y + self.webView.scrollView.frame.size.height
            
            if self.webView.scrollView.contentSize.height <= target {
                if self.isContentLoaded {
                    // コンテンツロード完了しているて、ツールバーが非表示のとき
                    if self.navigationController?.isToolbarHidden == true {
                        // ツールバー表示する
                        self.navigationController?.setToolbarHidden(false, animated: true)
                        
                        AppLogger.debug("BBB target = \(target)")
                    }
                } else {
                    AppLogger.debug("AAA target = \(target)")
                }
            }
        }
        keyValueObservations.append(keyValueObservation)
    }
    
    /// KVOを解除
    private func removeScrollObserving() {
        // コンテンツオフセット変更通知を解除
        for keyValueObservation in keyValueObservations {
            keyValueObservation.invalidate()
        }
        keyValueObservations.removeAll()
    }
    
    // MARK: Private methods
    
    /// HTMLファイルをロードする
    ///
    /// - Parameter url: HTMLファイルURL
    func loadHtml(_ url: URL) {
        let request: URLRequest = URLRequest(url: url)
        webView.load(request)
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
    
    // MARK: - Public methods
    
    /// セットアップする。
    ///
    /// - Parameters:
    ///   - title: タイトル文字列
    ///   - url: コンテンツURL
    ///   - agreeButtonTitle: 同意ボタンタイトル
    public func setup(title: String, url: URL, agreeButtonTitle: String) {
        headerTitle = title
        contentUrl = url
        agreeTitle = agreeButtonTitle
    }
    
    /// 同意済みかどうか取得する(true: 同意済み、false: 同意済みでない)
    public var agreed: Bool {
        return isAgreed
    }
}
