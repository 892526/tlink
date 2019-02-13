//
//  VNCServerLocalNotificationUtility.swift
//  JKEGCommonLib
//
//  Created by 板垣勇次 on 2018/08/23.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

/// ローカル通知のユーザ情報クラス
public class VNCServerLocalNotificationUtility {
    /// ユーザ情報を作成する。
    ///
    /// - Parameters:
    ///   - type: ローカル通知種別
    ///   - title: 通知タイトル文字列
    ///   - message: 通知メッセージ
    ///   - code: 通知付加コード
    /// - Returns: ユーザ情報
    public class func encodeUserInfo(type: VNCServerLocalNotifiction.VncServerNotificationType, title: String, message: String, code: UInt32) -> [AnyHashable: Any] {
        let info: [AnyHashable: Any] = [
            VNCServerLocalNotificationUserInfo.DataNeme.type: type.rawValue,
            VNCServerLocalNotificationUserInfo.DataNeme.title: title,
            VNCServerLocalNotificationUserInfo.DataNeme.message: message,
            VNCServerLocalNotificationUserInfo.DataNeme.code: code
        ]
        return info
    }
    
    /// ユーザ情報をロードする。
    ///
    /// - Parameter userInfo: ユーザ情報
    /// - Returns: ユーザ情報クラス
    public class func decodeUserInfo(userInfo: [AnyHashable: Any]) -> VNCServerLocalNotificationUserInfo {
        let info = VNCServerLocalNotificationUserInfo()
        
        info.type = VNCServerLocalNotifiction.VncServerNotificationType(rawValue: userInfo[VNCServerLocalNotificationUserInfo.DataNeme.type] as! UInt32)!
        info.title = userInfo[VNCServerLocalNotificationUserInfo.DataNeme.title] as! String
        info.message = userInfo[VNCServerLocalNotificationUserInfo.DataNeme.message] as! String
        info.code = userInfo[VNCServerLocalNotificationUserInfo.DataNeme.code] as! UInt32
        
        return info
    }
}
