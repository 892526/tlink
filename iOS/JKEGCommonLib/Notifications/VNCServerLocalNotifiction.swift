//
//  BroadcastVNCServerNotifictionExtention.swift
//  BroadcastServer
//
//  Created by 板垣勇次 on 2018/08/09.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

/// VNC Server用ローカル通知用拡張クラス
public class VNCServerLocalNotifiction {
    /// ローカル通知ID
    public static let requestIdentifier = "JK_VNC_SERVER_LOCAL_NOTIFICATION"
    
    /// ローカル通知種別列挙子
    ///
    /// - undefine: 未定義
    /// - connect: Viewerと接続された
    /// - disconnect: Viewerと切断された
    /// - error: エラーが発生した
    public enum VncServerNotificationType: UInt32 {
        case undefine = 0
        case connect = 1
        case disconnect = 2
        case error = 3
    }
    
    /// ローカル通知を表示する。
    ///
    /// - Parameters:
    ///   - type: 通知種別
    ///   - subMessage: 通知サブメッセージ
    public class func showLocalNotification(type: VncServerNotificationType, subMessage: String = String.Empty, code: UInt32 = 0) {
        // 許可されているか確認
        LocalNotification.checkNotificationAuthorization { granted, status in
            AppLogger.debug("granted = \(granted), status = \(status.rawValue)")
            // 許可されれいるとき
            if granted {
                var displayMessage = String.Empty
                
                // 規定メッセージセット
                switch type {
                case .connect:
                    displayMessage = Localize.localizedString("SS_02_001")
                case .disconnect:
                    displayMessage = Localize.localizedString("SS_02_002")
                case .error:
                    displayMessage = Localize.localizedString("SS_02_003")
                default:
                    displayMessage = "???"
                }
                
                // サブメッセージがあれば連結
                if !subMessage.isEmpty {
                    displayMessage += "\n" + subMessage
                }
                
                // ユーザ情報作成
                let userInfo = VNCServerLocalNotificationUtility.encodeUserInfo(type: type, title: String.Empty, message: displayMessage, code: code)
                
                // ローカル通知発行
                LocalNotification.show(requestIdentifier: VNCServerLocalNotifiction.requestIdentifier, timeInterval: 0.25,
                                       title: String.Empty, body: displayMessage, userInfo: userInfo, completionHandler: nil)
            }
        }
    }
}
