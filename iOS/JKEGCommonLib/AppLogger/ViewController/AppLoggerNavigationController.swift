//
//  AppLoggerNavigationController.swift
//  JKCommon
//
//  Created by 板垣勇次 on 2018/06/18.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import UIKit

public class AppLoggerNavigationController: UINavigationController {
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
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
    
    /// RootViewController取得する。
    ///
    /// - Returns: RootViewControllerインスタンス
    private func rootViewController() -> UIViewController {
        let viewController: UIViewController = viewControllers[0]
        return viewController
    }
    
    /// インスタンス生成する
    ///
    /// - Returns: JKLoggerNavigationControllerのインスタンス
    public class func createInstance() -> AppLoggerNavigationController {
        // バンドル取得
        let bundle = Bundle(for: AppLoggerNavigationController.self)
        // Storyboardロード
        let storyboard: UIStoryboard = UIStoryboard(name: "AppLogger", bundle: bundle)
        // JKLoggerNavigationControllerをインスタンス化
        let navController = storyboard.instantiateViewController(withIdentifier: "AppLoggerNavigationController") as! AppLoggerNavigationController
        
        return navController
    }
    
    /// セットアップする。
    ///
    /// - Parameter logger: AppLoggerインスタンス
    public func setup(logger: AppLogger) {
        // RootViewControllerにログ文字列渡す
        let viewController: AppLoggerViewController = rootViewController() as! AppLoggerViewController
        viewController.logDataType = .appLogger
        viewController.logger = logger
    }
    
    /// セットアップする。
    ///
    /// - Parameter logText: ログ文字列（テキスト）
    public func setup(logText: String) {
        // RootViewControllerにログ文字列渡す
        let viewController: AppLoggerViewController = rootViewController() as! AppLoggerViewController
        viewController.logDataType = .text
        viewController.logText = logText
    }
}
