//
//  FileCoordinatorUtility.swift
//  JKEGCommonLib
//
//  Created by 板垣勇次 on 2018/08/20.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

/// 共有ファイルの読み書きを行うユーティリティクラス
public class FileCoordinatorUtility {
    /// 読み込みAPIの例外
    ///
    /// - noError: エラーなし
    /// - fileNotFound: ファイルなし
    /// - otherError: その他エラー
    public enum ReadAPIError: Error {
        case noError
        case fileNotFound
        case otherError
    }
    
    /// 共有ファイルを消す
    ///
    /// - Parameters:
    ///   - fileName: 消去ファイル名
    ///   - groupID: グループID
    /// - Returns: 成功、失敗
    public class func removeData(fileName: String, groupID: String) -> Bool {
        AppLogger.debug()
        var result = false
        
        // App Groups 設定
        if let groupURL = FileManager.default.containerURL(forSecurityApplicationGroupIdentifier: groupID) {
            // 共有ファイル名作成
            let fileURL = groupURL.appendingPathComponent(fileName)
            
            // FileCooordinator生成
            let fileCoordinator = NSFileCoordinator()
            
            // 共有ファイルに消す
            fileCoordinator.coordinate(writingItemAt: fileURL, options: .forDeleting, error: nil) { removeFileUrl in
                do {
                    try FileManager.default.removeItem(at: removeFileUrl)
                    AppLogger.debug("dataRemove success")
                    result = true
                } catch let error {
                    AppLogger.error("dataRemove>> exception error = \(error.localizedDescription)")
                }
            }
        }
        
        return result
    }
    
    /// 共有ファイルに書き込み
    ///
    /// - Parameters:
    ///   - data: 書き込みデータ
    ///   - fileName: 書き込みファイル名
    ///   - groupID: グループID
    /// - Returns: 成功、失敗
    public class func writeData(data: Data, fileName: String, groupID: String) -> Bool {
        AppLogger.debug()
        var result = false
        
        // App Groups 設定
        if let groupURL = FileManager.default.containerURL(forSecurityApplicationGroupIdentifier: groupID) {
            // 共有ファイル名作成
            let fileURL = groupURL.appendingPathComponent(fileName)
            AppLogger.debug("fileURL = \(fileURL)")
            
            // FileCooordinator生成
            let fileCoordinator = NSFileCoordinator()
            
            // 共有ファイルに書き込む
            fileCoordinator.coordinate(writingItemAt: fileURL, options: [], error: nil) { saveFileUrl in
                AppLogger.debug("saveFileUrl = \(saveFileUrl)")
                do {
                    try data.write(to: saveFileUrl, options: Data.WritingOptions.atomic)
                    AppLogger.debug("dataWrite success")
                    result = true
                } catch let error {
                    AppLogger.error("dataWrite >> exception error = \(error.localizedDescription)")
                }
            }
        }
        return result
    }
    
    /// 共有ファイルから読み込み
    ///
    /// - Parameters:
    ///   - fileName: 読み込みファイル名
    ///   - groupID: グループID
    /// - Returns: 読み込みデータ。データ無いん場合は、nil。
    public class func readData(fileName: String, groupID: String) throws -> Data? {
        AppLogger.debug()
        var readData: Data?
        var exceptionCode: ReadAPIError = ReadAPIError.noError
        
        // FileCooordinator生成
        let fileCoordinator = NSFileCoordinator()
        
        // App Groups 設定
        if let groupURL = FileManager.default.containerURL(forSecurityApplicationGroupIdentifier: groupID) {
            // 共有ファイル名作成
            let fileURL = groupURL.appendingPathComponent(fileName)
            
            // 共有ファイルから読み込む
            fileCoordinator.coordinate(readingItemAt: fileURL, options: [], error: nil) { readFileUrl in
                
                do {
                    let data = try Data(contentsOf: readFileUrl)
                    if data.isEmpty {
                        exceptionCode = ReadAPIError.fileNotFound
                    } else {
                        readData = data
                    }
                    
                    AppLogger.debug("dataRead >> success")
                    
                } catch let error {
                    AppLogger.error("dataRead >> exception error = \(error.localizedDescription)")
                    exceptionCode = ReadAPIError.otherError
                }
            }
            AppLogger.debug()
            
            // エラーあるとき
            if exceptionCode != ReadAPIError.noError {
                AppLogger.debug("dataRead >> exceptionCode = \(exceptionCode.localizedDescription)")
                throw exceptionCode
            }
        }
        return readData
    }
}
