//
//  AppGroupsManager.swift
//  JKEGCommonLib
//
//  Created by 板垣勇次 on 2018/08/30.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

/// ファイル共有アクセスクラス
public class AppGroupsManager: NSObject {
    #warning("TODO: AppStore用は、本番用バンドルID、プロトコルストリングをInfo.plistに設定してください!")
    #warning("TODO: AppStore用は、本番用App GroupIDを設定してください!")
    
    /// ファイル共有アクセス名(App Groupd ID)
    
    /// 開発用App Group ID
    public static let groupID = "group.com.jvckenwood-eng.product.kleasylink"
    
    /// 本番用App Grroup ID
    // public static let groupID = "group.com.jvckenwood.ce.kleasylink"
    
    /// 初期化済みかどうか
    static let intializeState = "SHARED_USER_DEFAULTS_KEY_INITIALIZED"
    
    /// 接続状態（連携状態）
    static let connectionState = "SHARED_USER_DEFAULTS_KEY_CONNECTION_STATE"
    static let connectionStateUpdateTime = "SHARED_USER_DEFAULTS_KEY_CONNECTION_STATE_UPDATE_TIME"
    
    /// AppExtention内で発生したメッセージ
    static let messageInfo = "SHARED_USER_DEFAULTS_KEY_MESSAGE_INFO"
    
    /// プロトコルストリングインデックス
    static let protocolStringIndex = "SHARED_USER_DEFAULTS_KEY_PROTOCOLSTRING_INDEX"
    
    /// フレームレートインデックス
    static let frameRateIndex = "SHARED_USER_DEFAULTS_KEY_FRAMERATE_INDEX"
    
    /// 利用規約許諾状態
    static let userAgreementState = "SHARED_USER_DEFAULTS_KEY_USER_AGREEMENT_STATE"
    
    /// エラーメッセージデバッグ
    static let errorMessageDebug = "SHARED_USER_DEFAULTS_KEY_ERROR_MESSAGE_DEBUG"
    
    /// 同期する
    public class func synchronize() {
        if let userDefaults = UserDefaults(suiteName: groupID) {
            userDefaults.synchronize()
        }
    }
    
    /// 設定値を初期化する
    public class func reset() {
        AppGroupsManager.saveConnectionState(false)
        AppGroupsManager.saveConnectionUpdateTime(nil)
        AppGroupsManager.saveErrorInfo(nil)
        AppGroupsManager.saveUserAgreementState(false)
        
        AppGroupsManager.saveProtocolStringIndex(index: 0) // 本番用優先
        AppGroupsManager.saveFrameRateIndex(index: 0) // 60 fps
        AppGroupsManager.saveErrorMessageDebug(false) // エラーメッセージデバッグ無効
    }
    
    /// 初期化済みかどうか設定する。
    ///
    /// - Parameter value: 初期化済みかどうか
    public class func saveInitializedState(value: Bool) {
        if let userDefaults = UserDefaults(suiteName: groupID) {
            userDefaults.set(value, forKey: intializeState)
        }
    }
    
    /// 初期化済みかどうか取得する。
    ///
    /// - Returns: 初期化済みかどうか（true:初期化済み、false:み初期化）
    public class func loadInitializeState() -> Bool {
        if let userDefaults = UserDefaults(suiteName: groupID) {
            return userDefaults.bool(forKey: intializeState)
        }
        return false
    }
    
    // MARK: - Connection State
    
    /// 接続状態を保存する（UserDefaults）
    ///
    /// - Parameter state: 接続状態
    public class func saveConnectionState(_ state: Bool) {
        if let userDefaults = UserDefaults(suiteName: groupID) {
            userDefaults.set(state, forKey: connectionState)
        }
    }
    
    /// 接続状態を取得する（UserDefaults）
    ///
    /// - Returns: 接続状態
    public class func loadConnectionState() -> Bool {
        if let userDefaults = UserDefaults(suiteName: groupID) {
            return userDefaults.bool(forKey: connectionState)
        }
        return false
    }
    
    /// 接続状態更新時間を保存する（UserDefaults）
    ///
    /// - Parameter time: 更新時間文字列
    public class func saveConnectionUpdateTime(_ time: String? = Date.toString()) {
        if let userDefaults = UserDefaults(suiteName: groupID) {
            userDefaults.set(time, forKey: connectionStateUpdateTime)
        }
    }
    
    /// 接続状態更新時間を取得する（UserDefaults）
    ///
    /// - Returns: 接続状態更新時間
    public class func loadConnectionUpdateTime() -> String? {
        if let userDefaults = UserDefaults(suiteName: groupID) {
            if let value = userDefaults.object(forKey: connectionStateUpdateTime) as? String {
                return value
            }
        }
        return nil
    }
    
    // MARK: - Error Information
    
    /// エラー情報を設定する（UserDefaults）
    ///
    /// - Parameter info: エラー情報
    public class func saveErrorInfo(_ info: AppExtentionMessageInfo?) {
        if let userDefaults = UserDefaults(suiteName: groupID) {
            if let info = info {
                let acvData = NSKeyedArchiver.archivedData(withRootObject: info)
                userDefaults.set(acvData, forKey: messageInfo)
            } else {
                userDefaults.set(nil, forKey: messageInfo)
            }
        }
    }
    
    /// エラー情報を取得する（UserDefaults）
    ///
    /// - Returns: エラー情報
    public class func loadErrorInfo() -> AppExtentionMessageInfo? {
        if let userDefaults = UserDefaults(suiteName: groupID) {
            if let readDataValue = userDefaults.object(forKey: messageInfo) as? Data {
                let value = NSKeyedUnarchiver.unarchiveObject(with: readDataValue) as? AppExtentionMessageInfo
                return value
            }
        }
        return nil
    }
    
    /// 利用規約の許諾状態を取得する。
    ///
    /// - Returns: 許諾状態（true: 許諾、false: 未許諾）
    public class func loadUserAgreementState() -> Bool {
        if let userDefaults = UserDefaults(suiteName: groupID) {
            return userDefaults.bool(forKey: userAgreementState)
        }
        return false
    }
    
    /// 利用規約の許諾状態を保存する。
    ///
    /// - Parameter state: 許諾状態（true: 許諾、false: 未許諾）
    public class func saveUserAgreementState(_ state: Bool) {
        if let userDefaults = UserDefaults(suiteName: groupID) {
            userDefaults.set(state, forKey: userAgreementState)
        }
    }
    
    // MARK: - Debug methods
    
    /// 使用するプロトコルストリングのインデックス番号を取得する
    ///
    /// - Returns: インデックス番号
    public class func loadProtocolStringIndex() -> Int {
        if let userDefaults = UserDefaults(suiteName: groupID) {
            return userDefaults.integer(forKey: protocolStringIndex)
        }
        return 0
    }
    
    /// 使用するプロトコルストリングのインデックス番号を保存する
    ///
    /// - Parameter index: インデックス番号
    public class func saveProtocolStringIndex(index: Int) {
        if let userDefaults = UserDefaults(suiteName: groupID) {
            userDefaults.set(index, forKey: protocolStringIndex)
        }
    }
    
    /// フレームレート設定のインデックス番号を取得する。
    ///
    /// - Returns: インデックス番号
    public class func loadFrameRateIndex() -> Int {
        if let userDefaults = UserDefaults(suiteName: groupID) {
            return userDefaults.integer(forKey: frameRateIndex)
        }
        return 0
    }
    
    /// フレームレート設定のインデックス番号を保存する。
    ///
    /// - Parameter index: インデックス番号
    public class func saveFrameRateIndex(index: Int) {
        if let userDefaults = UserDefaults(suiteName: groupID) {
            userDefaults.set(index, forKey: frameRateIndex)
        }
    }
    
    /// エラーメッセージデバッグ機能の有効/無効を取得する。
    ///
    /// - Returns: true:有効/false:無効
    public class func loadErrorMessageDebug() -> Bool {
        if let userDefaults = UserDefaults(suiteName: groupID) {
            return userDefaults.bool(forKey: errorMessageDebug)
        }
        return false
    }
    
    /// エラーメッセージデバッグ機能の有効/無効を保存する。
    ///
    /// - Parameter enabled: true:有効/false:無効
    public class func saveErrorMessageDebug(_ enabled: Bool) {
        if let userDefaults = UserDefaults(suiteName: groupID) {
            userDefaults.set(enabled, forKey: errorMessageDebug)
        }
    }
}
