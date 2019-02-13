//
//  AppLoggerViewer.swift
//  JKEGCommonDemo
//
//  Created by 板垣勇次 on 2018/06/28.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import UIKit

/// ログ表示ビューワークラス
public class AppLoggerViewer: NSObject {
    /// ログビューワーを表示する。
    ///
    /// - Parameters:
    ///   - owner: オーナーのUIViewControllerインスタンス
    ///   - logger: ログ管理インスタンス
    public class func show(owner: UIViewController, logger: AppLogger) {
        // ログ表示用ビューコントローラー作成
        let navContrller = AppLoggerNavigationController.createInstance()
        navContrller.setup(logger: logger)
        
        // 表示
        owner.present(navContrller, animated: true, completion: nil)
    }
    
    /// ログビューワーを表示する。
    ///
    /// - Parameters:
    ///   - owner: オーナーのUIViewControllerインスタンス
    ///   - logText: ログテキスト
    public class func show(owner: UIViewController, logText: String) {
        // ログ表示用ビューコントローラー作成
        let navContrller = AppLoggerNavigationController.createInstance()
        navContrller.setup(logText: logText)
        
        // 表示
        owner.present(navContrller, animated: true, completion: nil)
    }
}
