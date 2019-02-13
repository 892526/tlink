//
//  BroadcastVNCServerSettingExtention.swift
//  BroadcastServer
//
//  Created by 板垣勇次 on 2018/08/10.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation
import JKEGCommonLib

// MARK: - BroadcastVNCServer拡張 - アプリ本体への通知機能

extension BroadcastVNCServer {
    /// 接続状態（連携状態）をセットする
    ///
    /// - Parameter state: 接続状態
    class func setConnectionState(_ state: Bool) {
        AppLogger.debug()
        // AppLogger.debug(String(format: "Pre Now = %@, New = %@", String(AppGroupsManager.loadConnectionState()), String(state)))
        
        AppGroupsManager.saveConnectionState(state)
        AppGroupsManager.saveConnectionUpdateTime()
        AppGroupsManager.synchronize()
        // AppLogger.debug(String(format: "End Now = %@, New = %@", String(AppGroupsManager.loadConnectionState()), String(state)))
    }
    
    /// メッセージ情報を設定にセットする
    ///
    /// - Parameters:
    ///   - date: 日付
    ///   - type: メッセージ種別
    ///   - addtionalMessage: メッセージ文字列
    ///   - additionalIntValue: 数値
    class func setMessageInfo(date: Date, type: AppExtentionMessageInfo.MessageType, addtionalMessage: String = "", additionalIntValue: UInt32 = 0) {
        AppLogger.debug()
        
        // メッセージ情報作成
        let info = AppExtentionMessageInfo(date: date, type: type, message: addtionalMessage, value: additionalIntValue)
        
        // メッセージ情報追加
        AppGroupsManager.saveErrorInfo(info)
        AppGroupsManager.synchronize()
    }
}
