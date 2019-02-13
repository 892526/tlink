//
//  ApplicationUtility.swift
//  JKCommon
//
//  Created by 板垣勇次 on 2018/06/20.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

// MARK: - アプリケーションユーティリティ

/// アプリケーションユーティリティクラス
public class ApplicationUtility {
    /// バンドルIDを取得する（CFBundleIdentifier）。
    ///
    /// - Returns: バンドルID
    public class func bundleIdentifier() -> String {
        var value: String = String.Empty
        
        let name: String? = Bundle.main.bundleIdentifier
        if name != nil {
            value = name!
        }
        return value
    }
    
    /// バンドル名を取得する（CFBundleName）。
    ///
    /// - Returns: バンドル名
    public class func bundleName() -> String {
        var value: String = String.Empty
        
        let name: String? = Bundle.main.object(forInfoDictionaryKey: "CFBundleName") as? String
        if name != nil {
            value = name!
        }
        return value
    }
    
    /// バンドル表示名を取得する（CFBundleDisplayName）。
    ///
    /// - Returns: バンドル表示名
    public class func bundleDisplayName() -> String {
        var value: String = String.Empty
        
        let name: String? = Bundle.main.object(forInfoDictionaryKey: "CFBundleDisplayName") as? String
        if name != nil {
            value = name!
        }
        return value
    }
    
    /// アプリバージョン取得する（CFBundleShortVersionString）。
    ///
    /// - Returns: アプリバージョン
    public class func version() -> String {
        var value: String = String.Empty
        
        let version: String? = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String
        if version != nil {
            value = version!
        }
        return value
    }
    
    /// ビルドバージョンを取得する（CFBundleVersion）。
    ///
    /// - Returns: ビルドバージョン
    public class func buildVersion() -> String {
        var value: String = String.Empty
        
        let version: String? = Bundle.main.object(forInfoDictionaryKey: "CFBundleVersion") as? String
        if version != nil {
            value = version!
        }
        return value
    }
    
    /// プロトコルストリングを取得する（UISupportedExternalAccessoryProtocols）。
    ///
    /// - Returns: プロトコルストリング一覧
    public class func protocolString() -> [String] {
        var value: [String] = Array()
        
        if let values = Bundle.main.object(forInfoDictionaryKey: "UISupportedExternalAccessoryProtocols") {
            if values is [String] {
                value = values as! [String]
            }
        }
        return value
    }
}
