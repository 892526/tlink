//
//  VNCServerLocalNotificationUserInfo.swift
//  JKEGCommonLib
//
//  Created by 板垣勇次 on 2018/08/23.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

/// ローカル通知用ユーザ情報
public class VNCServerLocalNotificationUserInfo: NSObject, NSCoding {
    /// エンコードする
    ///
    /// - Parameter aCoder: エンコードオブジェクト
    public func encode(with aCoder: NSCoder) {
        aCoder.encode(type.hashValue)
        aCoder.encode(title)
        aCoder.encode(message)
        aCoder.encode(code)
    }
    
    /// 初期化する
    ///
    /// - Parameter aDecoder: デコードオブジェクト
    public required init?(coder aDecoder: NSCoder) {
        type = VNCServerLocalNotifiction.VncServerNotificationType(rawValue: UInt32(aDecoder.decodeInt32(forKey: DataNeme.type)))!
        title = aDecoder.decodeObject(forKey: DataNeme.title) as! String
        title = aDecoder.decodeObject(forKey: DataNeme.title) as! String
        message = aDecoder.decodeObject(forKey: DataNeme.message) as! String
        code = UInt32(aDecoder.decodeInt32(forKey: DataNeme.type))
    }
    
    /// データ名
    public class DataNeme {
        /// ローカル通知種別
        public static let type = "type"
        /// タイトル文字列
        public static let title = "title"
        /// 通知メッセージ
        public static let message = "message"
        /// 通知付加コード
        public static let code = "code"
    }
    
    /// 通知種別
    public var type: VNCServerLocalNotifiction.VncServerNotificationType
    
    /// 通知タイトル
    public var title: String
    
    /// 通知メッセージ
    public var message: String
    
    /// 通知付加コード
    public var code: UInt32
    
    /// 初期化します。
    override init() {
        type = VNCServerLocalNotifiction.VncServerNotificationType.undefine
        title = String.Empty
        message = String.Empty
        code = 0
    }
    
    /// 初期化します。
    ///
    /// - Parameters:
    ///   - type: 通知種別
    ///   - title: 通知タイトル
    ///   - message: 通知メッセージ
    ///   - code: 通知付加コード
    init(type: VNCServerLocalNotifiction.VncServerNotificationType, title: String, message: String, code: UInt32) {
        self.type = type
        self.title = title
        self.message = message
        self.code = code
    }
}
