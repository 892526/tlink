//
//  BaseWebViewController.swift
//  JKVncServer
//
//  Created by 板垣勇次 on 2018/06/29.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

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
}
