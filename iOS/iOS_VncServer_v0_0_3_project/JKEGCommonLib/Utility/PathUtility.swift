//
//  PathUtility.swift
//  JKCommon
//
//  Created by 板垣勇次 on 2018/06/26.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

// MARK: - パスユーティリティ

public class PathUtility {
    /// ファイルが存在するかチェックする。
    ///
    /// - Parameter path: 確認するファイルのパス
    /// - Returns: 存在する:true、存在しない:false
    public class func fileExist(_ path: String) -> Bool {
        return FileManager.default.fileExists(atPath: path)
    }
    
    /// ディレクトリが存在するかどうかチェックする。
    ///
    /// - Parameter path: 確認するディレクトリパス
    /// - Returns: true:存在する、false:存在しない
    public class func directoryExistAtPath(_ path: String) -> Bool {
        var isDirectory = ObjCBool(true)
        
        let exists = FileManager.default.fileExists(atPath: path, isDirectory: &isDirectory)
        
        return exists && isDirectory.boolValue
    }
    
    /// 指定したディレクトリにあるファイル一覧を取得する
    ///
    /// - Parameters:
    ///   - directoryPath: ディレクトリパス
    ///   - onlyFile: ファイルパスのみ取得（ディレクトリパスは含めない）するかどうか
    /// - Returns: ファイル一覧
    public class func filesAtPath(_ directoryPath: String, onlyFile: Bool) -> [String] {
        do {
            var fullPaths: [String] = [String]()
            
            // ファイル一覧取得
            let paths = try FileManager.default.contentsOfDirectory(atPath: directoryPath)
            
            for path in paths {
                let fullPath = directoryPath.appendingPathComponent(path)
                
                // ディレクトリで、ファイルパスのみ取得したいとき
                if directoryExistAtPath(fullPath) && onlyFile {
                    // 追加しない
                    continue
                }
                
                // パスを追加
                fullPaths.append(fullPath)
            }
            return fullPaths
        } catch {
            #if DEBUG
                print("FileUtility.filesAtPath >> exception >> DIR Path = " + directoryPath)
            #endif // DEBUG
        }
        return []
    }
    
    /// 指定したディレクトリにあるファイル一覧を取得する。ディレクトリは含まない。
    ///
    /// - Parameter directoryPath: ディレクトリパス
    /// - Returns: ファイル一覧
    public class func filesAtPath(_ directoryPath: String) -> [String] {
        return filesAtPath(directoryPath, onlyFile: true)
    }
    
    /// ドキュメントフォルダパスを取得します。
    ///
    /// - Returns: ドキュメントフォルダパス
    public class func documentFolderPath() -> String {
        let docPath = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0]
        return docPath
    }
    
    /// ディレクトリを作成する。
    ///
    /// - Parameter path: 作成するディレクトリパス
    /// - Returns: true:成功、false:失敗
    public class func createDirectory(path: String) -> Bool {
        do {
            try FileManager.default.createDirectory(atPath: path, withIntermediateDirectories: true, attributes: nil)
            return true
        } catch {
            #if DEBUG
                print("PathUtility.createDirectory() >> exception error >> " + path)
            #endif
            return false
        }
    }
}
