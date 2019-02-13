//
//  FileUtility.swift
//  TestArgment
//
//  Created by 板垣勇次 on 2018/06/26.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

// MARK: - ファイルユーティリティ

/// ファイルユーティリティークラス
public class FileUtility {
    /// ファイル削除する。
    ///
    /// - Parameter path: 削除するファイルのパス
    /// - Returns: true:成功、false:失敗
    public class func removeFile(_ path: String) -> Bool {
        do {
            try FileManager.default.removeItem(atPath: path)
            return true
        } catch {
            #if DEBUG
                print("FileUtility.removeFile() >> exception error >> " + path)
            #endif // DEBUG
            return false
        }
    }
}
