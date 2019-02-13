//
//  LogFileWriter.swift
//  TestArgment
//
//  Created by 板垣勇次 on 2018/06/19.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import UIKit

/// ログデータをファイルに書き出すクラス
public class LogFileWriter: NSObject {
    private let maxLogLines: UInt = 10000
    var logLines: UInt = 0
    
    private var fileHandle: FileHandle?
    private var filePath: String?
    
    // MARK: - public function
    
    override init() {
    }
    
    private func saveDirectoryPath() -> String {
        let docUrl = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first
        let logFolderUrl = docUrl?.appendingPathComponent("log")
        var dirPath: String = (logFolderUrl?.absoluteString)!
        
        // "log"フォルダ存在しないとき
        if !FileManager.default.fileExists(atPath: dirPath) {
            // フォルダ作成する
            do {
                try FileManager.default.createDirectory(atPath: dirPath, withIntermediateDirectories: true, attributes: nil)
            } catch {
                print("error")
                dirPath = ""
            }
        }
        return dirPath
    }
    
    func openByDefaultName() -> Bool {
        let fileName = makeLogFileName()
        if !open(fileName) {
            return false
        }
        return true
    }
    
    // MARK: - public function
    
    /// 現在時刻からファイル名を作成する
    ///
    /// - Returns: ファイル名
    private func makeLogFileName() -> String {
        let fileName = "LOG_" + Date.toString() + ".log"
        return fileName
    }
    
    func exceedAvailableLines() -> Bool {
        return (logLines > maxLogLines)
    }
    
    func open(_ fileName: String) -> Bool {
        filePath = saveDirectoryPath() + "/" + fileName
        
        fileHandle = FileHandle(forWritingAtPath: filePath!)!
        
        fileHandle = FileHandle(forWritingAtPath: filePath!)!
        if fileHandle == nil {
            #if DEBUG
                print("LogFile open >> file open failed... (%s)", filePath!)
            #endif
            return false
        }
        #if DEBUG
            print("LogFile open >> file open success. (%s)", filePath!)
        #endif
        return true
    }
    
    func close() {
        #if DEBUG
            print("LogFile close >> %s", filePath!)
        #endif
        if fileHandle != nil {
            fileHandle?.closeFile()
            fileHandle = nil
        }
    }
    
    func writeLine(_ message: String) {
        if fileHandle != nil {
            // 改行コード付加
            let writeString = message + "\n"
            // 保存
            fileHandle?.write(writeString.data(using: .utf8)!)
            logLines += 1
        }
    }
    
    func autoWriteLine(_ message: String) -> Bool {
        if fileHandle == nil {
            if openByDefaultName() {
                return false
            }
        }
        
        // ファイル書き込み
        writeLine(message)
        
        // １ファイルあたり行数超えた
        if exceedAvailableLines() {
            // ファイル閉じる
            close()
            
            // 新規ファイルオープン
            if openByDefaultName() {
                return false
            }
        }
        return true
    }
}
