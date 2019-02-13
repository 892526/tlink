//
//  BaseSafariViewController.swift
//  JKVncServer
//
//  Created by 板垣勇次 on 2018/07/24.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import JKEGCommonLib
import SafariServices
import UIKit

class BaseSafariViewController: SFSafariViewController, SFSafariViewControllerDelegate {
    var loadingView: LoadingView?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
        
        // SFSafariViewControllerのデリゲートセット
        delegate = self
        
        // ステータスバーにアクティビティーインジケーター表示開始
        UIApplication.shared.isNetworkActivityIndicatorVisible = true
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillAppear(animated)
        delegate = nil
        
        // ステータスバーにアクティビティーインジケーター表示終了
        hideActivityIndicator()
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
    
    // MARK: - SFSafariViewControllerDelegate methods
    
    func safariViewControllerDidFinish(_ controller: SFSafariViewController) {
        AppLogger.debug()
        
        // ステータスバーにアクティビティーインジケーター表示終了
        hideActivityIndicator()
    }
    
    func safariViewController(_ controller: SFSafariViewController, didCompleteInitialLoad didLoadSuccessfully: Bool) {
        AppLogger.debug()
        
        // ステータスバーにアクティビティーインジケーター表示終了
        hideActivityIndicator()
    }
    
    // MARK: - private methods
    
    private func showActivityIndicator() {
        if !UIApplication.shared.isNetworkActivityIndicatorVisible {
            UIApplication.shared.isNetworkActivityIndicatorVisible = true
        }
    }
    
    private func hideActivityIndicator() {
        if UIApplication.shared.isNetworkActivityIndicatorVisible {
            UIApplication.shared.isNetworkActivityIndicatorVisible = false
        }
    }
}
