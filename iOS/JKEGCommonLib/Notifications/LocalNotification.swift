//
//  LocalNotification.swift
//  JKVncServer
//
//  Created by 板垣勇次 on 2018/07/03.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation
import UserNotifications

/// ローカル通知クラス
public class LocalNotification {
    /// 通知は利用許可状態かチェックする。
    ///
    /// - Parameter completionHandler: 確認結果ハンドラ
    public class func checkNotificationAuthorization(completionHandler: ((_ granted: Bool) -> Void)?) {
        AppLogger.debug()
        
        // 通知の利用許可状態を取得する
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            // デフォルトは許可されていない
            var result: Bool = false
            
            switch settings.authorizationStatus {
            case .authorized:
                AppLogger.debug("authorizationStatus = authorized")
                // 許可されている
                result = true
            case .denied:
                AppLogger.debug("authorizationStatus = denied")
            case .notDetermined:
                AppLogger.debug("authorizationSztatus = notDetermined")
            }
            // 結果を通知
            if let handler = completionHandler {
                handler(result)
            }
        }
    }
    
    /// 通知許可をリクエストする、
    ///
    /// - Parameter completionHandler: 結果ハンドラ
    public class func requestAuthorization(completionHandler: ((_ granted: Bool, _ error: Error?) -> Void)?) {
        AppLogger.debug()
        
        // 通知の利用許可をユーザーに確認する
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert]) { granted, error in
            
            AppLogger.debug("granted = \(granted), error = \(String(describing: error?.localizedDescription))")
            
            // 結果を呼び出しもとに通知
            if let handler = completionHandler {
                handler(granted, error)
            }
        }
    }
    
    /// ローカル通知を表示する。
    ///
    /// - Parameters:
    ///   - requestIdentifier: ローカル通知ID
    ///   - timeInterval: 表示までの遅延時間（単位:秒）
    ///   - title: ローカル通知のタイトル文字列
    ///   - body: ローカル通知のボディ文字列
    ///   - completionHandler: ローカル通リのリクエスト結果ハンドラ(必要ない場合はnilを設定する)
    public class func show(requestIdentifier: String, timeInterval: TimeInterval, title: String = "", body: String = "",
                           userInfo: [AnyHashable: Any]?, completionHandler: ((_ error: Error?) -> Void)?) {
        AppLogger.debug()
        
        let content = UNMutableNotificationContent()
        
        if !title.isEmpty {
            // タイトル文字列あれば設定
            content.title = title
        }
        if !body.isEmpty {
            // ボディ文字列あれば設定
            content.body = body
        }
        if let userInfo = userInfo {
            // ユーザ情報あれば付加する
            content.userInfo = userInfo
        }
        
        // ローかつ通知までの遅延時間
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: timeInterval, repeats: false)
        
        // ローカル通知リクエスト作成
        let request = UNNotificationRequest(identifier: requestIdentifier,
                                            content: content,
                                            trigger: trigger)
        
        // ローカル通知リクエストを通知センターに設定
        UNUserNotificationCenter.current().add(request) { error in
            
            AppLogger.debug("error = \(String(describing: error?.localizedDescription))")
            
            // ローカル通知設定結果を呼び出し元に通知
            if let handler = completionHandler {
                handler(error)
            }
        }
    }
}
