//
//  AppExtentionMessageInfo.swift
//  JKEGCommonLib
//
//  Created by 板垣勇次 on 2018/08/09.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

/// AppExtentionからのメッセージ情報クラス
open class AppExtentionMessageInfo: NSObject, NSCoding {
    /// 動作ログ・ファイル名
    public static let appLogFileName: String = "JKVncServerLogFile.txt"
    
    // MARK: - Enum
    
    /// メッセージ種別
    ///
    /// - undefined: 未定義
    /// - connect: Viewerと接続された
    /// - disconnect: Viewerと切断された
    /// - unknownError: 不明なエラー
    /// - licenseError: サーバーライセンスファイルエラー
    /// - userAuthError: ユーザー認証エラー
    /// - remoteFeatureFailed: リモートフィーチャーエラー
    /// - internalServerError: サーバー内エラー
    
    public enum MessageType: Int32 {
        case undefined = 0
        
        case connect = 1
        case disconnect = 2
        
        case unknownError = 10
        case licenseError = 11
        case userAuthError = 12
        case remoteFeatureFailed = 13
        case internalServerError = 14
    }
    
    // MARK: - Property
    
    /// メッセージ発生日付文字列
    public var dateString: String = String.Empty
    
    /// メッセージ種別
    public var messageType: MessageType = MessageType.undefined
    
    /// 付加メッセージ文字列
    public var additionalMessage: String = String.Empty
    
    /// 付加数値
    public var additionalIntValue: UInt32 = 0
    
    // MARK: - NSCoding delegate methods
    
    public override init() {
        super.init()
    }
    
    /// 初期化します
    ///
    /// - Parameters:
    ///   - date: 日付
    ///   - type: メッセージ種別
    ///   - message: メッセージ文字列（デフォルトは空文字）
    ///   - value: 数値（デフォルトは0）
    public init(date: Date, type: MessageType, message: String = "", value: UInt32 = 0) {
        super.init()
        
        dateString = date.toString()
        messageType = type
        additionalMessage = message
        additionalIntValue = value
    }
    
    /// パラメータをデコードする。
    ///
    /// - Parameter aDecoder: デコード情報
    @objc public required init?(coder aDecoder: NSCoder) {
        // メッセージ日付文字列を復元
        let dateStr = aDecoder.decodeObject(forKey: "dateString") as? String
        if let dateStr = dateStr {
            dateString = dateStr
        }
        
        // メッセージ種別を復元
        let mesTypeValue = aDecoder.decodeInt32(forKey: "messageType")
        if let mesType = MessageType(rawValue: mesTypeValue) {
            messageType = mesType
        }
        
        // 付加メッセージを復元
        let addMes = aDecoder.decodeObject(forKey: "additionalMessage") as? String
        if let addMes = addMes {
            additionalMessage = addMes
        }
        
        // 付加数値
        let intValue = aDecoder.decodeInt32(forKey: "additionalIntValue")
        additionalIntValue = UInt32(bitPattern: intValue) // Int32 -> UInt32に変換
    }
    
    /// パラメータをエンコードする。
    ///
    /// - Parameter aCoder: エンコード情報
    @objc public func encode(with aCoder: NSCoder) {
        aCoder.encode(dateString, forKey: "dateString")
        aCoder.encode(messageType.rawValue, forKey: "messageType")
        aCoder.encode(additionalMessage, forKey: "additionalMessage")
        
        let intValue = Int32(bitPattern: additionalIntValue) // UInt32 -> Int32に変換
        aCoder.encode(intValue, forKey: "additionalIntValue")
    }
}
