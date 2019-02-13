//
//  AppExtentionLoggerUtility.swift
//  JKEGCommonLib
//
//  Created by 板垣勇次 on 2018/08/21.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

/// AppExtentionの動作ログユーティリティクラス
public class AppExtentionLoggerUtility: NSObject {
    /// 共有ファイルのログを取得する。
    ///
    /// - Returns: ログ文字列
    public class func loadLog() -> String? {
        var logText: String?
        
        do {
            // 読み込む
            if let readData = try FileCoordinatorUtility.readData(fileName: AppExtentionMessageInfo.appLogFileName, groupID: AppGroupsManager.groupID) {
                if let text = String(data: readData, encoding: .utf8) {
                    AppLogger.debug("App Extention Log =\n\(text)\n+++++++++++++++++++++++++++++++++++")
                    logText = text
                }
            }
        } catch FileCoordinatorUtility.ReadAPIError.fileNotFound {
            AppLogger.debug("fileNotFound")
        } catch FileCoordinatorUtility.ReadAPIError.otherError {
            AppLogger.debug("otherError")
        } catch FileCoordinatorUtility.ReadAPIError.noError {
            AppLogger.debug("noError")
        } catch {
            AppLogger.debug("unknown error")
        }
        
        return logText
    }
    
    /// ログを削除する
    public class func removeLog() {
        // 削除する
        let result = FileCoordinatorUtility.removeData(fileName: AppExtentionMessageInfo.appLogFileName, groupID: AppGroupsManager.groupID)
        AppLogger.debug("remove result = \(result)")
    }
}
