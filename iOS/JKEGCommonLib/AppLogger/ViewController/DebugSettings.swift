//
//  DebugSettings.swift
//  JKEGCommonLib
//
//  Created by 板垣勇次 on 2018/08/21.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

/// デバッグ用設定クラス
public class DebugSettings {
    private static let settingFileName = "DebugSettings.plist"
    
    /// デバッグ設定をロードする。
    ///
    /// - Returns: デバッグ設定。無い場合は、nilを返します
    public class func loadSetting() -> DebugSettingInfo? {
        let filePath = PathUtility.documentFolderPath().appendingPathComponent(settingFileName)
        if FileManager.default.fileExists(atPath: filePath) {
            do {
                let loadData = try Data(contentsOf: URL(fileURLWithPath: filePath))
                
                let info = NSKeyedUnarchiver.unarchiveObject(with: loadData) as! DebugSettingInfo
                return info
            } catch let exception {
                AppLogger.debug(exception.localizedDescription)
            }
        }
        return nil
    }
    
    /// デバッグ設定を保存する。
    ///
    /// - Parameter info: 保存するデバッグ設定
    /// - Returns: true: 成功、false: 失敗
    public class func saveSetting(info: DebugSettingInfo) -> Bool {
        var result: Bool = false
        
        let filePath = PathUtility.documentFolderPath().appendingPathComponent(settingFileName)
        if FileManager.default.fileExists(atPath: filePath) {
            do {
                try FileManager.default.removeItem(atPath: filePath)
            } catch let exception {
                AppLogger.debug(exception.localizedDescription)
            }
        }
        
        // 保存
        result = NSKeyedArchiver.archiveRootObject(info, toFile: filePath)
        return result
    }
}

/// デバッグ設定情報
public class DebugSettingInfo: NSObject, NSCoding {
    public func encode(with aCoder: NSCoder) {
        aCoder.encode(accountName, forKey: "accountName")
        aCoder.encode(accountAddress, forKey: "accountAddress")
    }
    
    public required init?(coder aDecoder: NSCoder) {
        accountName = aDecoder.decodeObject(forKey: "accountName") as! String
        accountAddress = aDecoder.decodeObject(forKey: "accountAddress") as! String
    }
    
    /// アカウント名
    public var accountName: String = "Yuji Itagaki"
    
    /// メールアドレス
    public var accountAddress: String = "itagaki.yuji@jvckenwood.com"
    
    public override init() {
    }
    
    public init(name: String, address: String) {
        accountName = name
        accountAddress = address
    }
}
