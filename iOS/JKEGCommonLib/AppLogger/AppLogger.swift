//
//  AppLogger.swift
//  TestArgment
//
//  Created by 板垣勇次 on 2018/06/20.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

/// ログ収集クラス
public class AppLogger: NSObject {

    // MARK: - private function
    
    /// メッセージ種別
    ///
    /// - debug: デバッグメッセージ
    /// - info: 情報メッセージ
    /// - warning: ワーニングメッセージ
    /// - error: エラーメッセージ
    private enum OutputMessageType: String {
        case debug = "DBG"
        case info = "INF"
        case warning = "WAR"
        case error = "ERR"
    }
    
    // 排他制御用オブジェクト
    private var lockObject: NSLock = NSLock()
    
    // ログ格納フォルダ名
    private let logFolderName: String = "log"
    
    /// 日付フォーマッター
    private var dateFormatter: DateFormatter = DateFormatter()
    
    /// ファイル出力するかどうか
    private var enableFileOutput: Bool = false
    /// 情報メッセージ出力するかどうか
    private var enableInfoMessage: Bool = true
    /// ワーニングメッセージ出力するかどうか
    private var enableWarningMessage: Bool = true
    /// エラーメッセージ出力するかどうか
    private var enableErrorMessage: Bool = true
    
    /// ログファイル書き込み
    private var logWriter: LogFileWriter?
    
    /// ログ文字列バッファ
    private var logBuffer: String = String.Empty
    
    /// ログ格納行数
    private var logLines: UInt = 0
    
    // 2000行
    private let maxLogLines: UInt = 2000
    
    /// シングルトンインスタンス取得する
    public class var sharedInstance: AppLogger {
        /**
         * シングルトンインスタンス構造体
         */
        struct SingletonInstance {
            // JKLoggerインスタンス生成
            static let instance: AppLogger = AppLogger()
        }
        // シングルトンインスタンス返す
        return SingletonInstance.instance
    }
    
    /// 初期化します
    override init() {
        super.init()
        
        #if ENABLE_LOG
            // ログ格納用フォルダセットアップ
            // setupLogFolder()
            
            // 日付フォーマット指定
            dateFormatter.dateFormat = "yyyy/MM/dd HH:mm:ss.SSS"
            
            // ロケールを日本語
            dateFormatter.locale = Locale(identifier: "ja_JP")
            
            // 日付文字列取得
            let dateString = Date.toString("yyyy-MM-dd HH:mm:ss.SSS")
            
            // ログ・ファイルヘッダー情報出力
            appendMessage("#####################################")
            appendMessage("# LOG START")
            appendMessage("# Start Time : \(dateString) (JST)")
            appendMessage("# ------------------------------------")
            appendMessage("# Bundle Name: \(ApplicationUtility.bundleName())")
            appendMessage("# Budle ID   : \(ApplicationUtility.bundleIdentifier())")
            appendMessage("# version    : \(ApplicationUtility.version())")
            appendMessage("# build      : \(ApplicationUtility.buildVersion())")
            appendMessage("#####################################")
        #endif // ENABLE_LOG
    }
    
    /// ログ格納用フォルダをセットアップする
    private func setupLogFolder() {
        #if ENABLE_LOG
            // ログファイルパス取得
            let path = PathUtility.documentFolderPath().appendingPathComponent(logFolderName)
            
            if !PathUtility.fileExist(path) {
                // ログフォルダパスがない場合は、フォルダ作成
                if !PathUtility.createDirectory(path: path) {
                    print("JKLogger.setupLogFolder() >> failed...")
                }
            }
        #endif // ENABLE_LOG
    }
    
    /// ログ文字列をバッファに追加
    ///
    /// - Parameter message: ログメッセージ
    /// - Returns: 合計ログ行数
    private func appendMessage(_ message: String) {
        defer {
            // ロック解除
            lockObject.unlock()
        }
        
        // ロック取得
        lockObject.lock()
        
        if logLines > maxLogLines {
            print("AppLogger.appendMessage() >> message overflow !!!")
            return
        }
        
        // メッセージ追加
        logBuffer.append(message + "\n")
        logLines += 1
    }
    
    /// 現在のスレッドID文字列取得する。
    ///
    /// - Returns: スレッドID文字列
    private func getCurrentThreadId() -> String {
        // Thread.current.debugDescription
        // <NSThread: 0x101f7f230>{number = 3, name = (null)}
        
        // スレッド情報取得
        let desc = Thread.current.debugDescription
        
        // 切り出し開始位置
        let fromIndex = desc.index(desc.index(of: ":")!, offsetBy: 2)
        
        // 切り出し終了位置
        let toIndex = desc.index(of: ">")
        
        // スレッドID切り出し
        let tid = String(desc[fromIndex ..< toIndex!])
        
        return tid
    }
    
    /// ログ出力します
    ///
    /// - Parameters:
    ///   - message: 出力メッセージ
    ///   - file: ファイル名
    ///   - function: メソッド名
    ///   - line: 行番号
    ///   - date: 日付
    private func write(_ message: String, file: String, function: String, line: UInt, date: Date, type: OutputMessageType) {
        // ファイルパスから拡張子なしファイル名取得
        let fileName = file.lastPathComponent.deletingPathExtension
        // let fileName = file.lastPathComponent
        // 出力メッセージ作成
        let message = String("\(dateFormatter.string(from: date)) [\(fileName) \(function)] [Line:\(line)] [\(type.rawValue)] \(message)")
        
        /*
         let tid = getCurrentThreadId()
         // 出力メッセージ作成
         let message = String("\(dateFormatter.string(from: date)) [\(fileName) \(function)] [Line:\(line)] [TID:\(tid)] [\(type.rawValue)] \(message)")
         */
        
        if enableFileOutput {
            // ファイルにメッセージ追加する
            appendMessage(message)
        }
        
        print(message)
    }
    
    /// ログフォルダパスを取得する
    ///
    /// - Returns: ログフォルダパス
    private func logFolderPath() -> String {
        // return PathUtility.documentFolderPath().appendingPathComponent(logFolderName)
        return PathUtility.documentFolderPath()
    }
    
    // MARK: - public function (instance methods)
    
    /// ログ文字列取得する。
    ///
    /// - Returns: ログ文字列(クローン)
    public func getLogBuffer() -> String {
        defer {
            // ロック解除
            lockObject.unlock()
        }
        
        // ロック開始
        lockObject.lock()
        
        // 文字列のクローンを作成する
        return logBuffer.clone()
    }
    
    /// ログをクリアする
    public func clearLogMessage() {
        defer {
            // ロック解除
            lockObject.unlock()
        }
        
        // ロック開始
        lockObject.lock()
        
        // ログクリア
        logBuffer = String.Empty
        logLines = 0
    }
    
    /// 現時刻からログファイル名を作成する。
    ///
    /// - Returns: ログファイル名
    public func makeLogFileName() -> String {
        let fineName = String(format: "LOG_%@.txt", Date.toString())
        return fineName
    }
    
    /// 現時刻からログファイル名を作成する。
    ///
    /// - Parameter format: ログファイル名フォーマット（日付を入れる場所に"%@"を入れる）
    /// - Returns: ログファイル名
    public func makeLogFileName(format: String) -> String {
        let fineName = String(format: format, Date.toString())
        return fineName
    }
    
    /// 現在時刻をファイル名にして、保存する。
    ///
    /// - Returns: true:成功、false:失敗
    public func saveLog() -> Bool {
        // ファイル名作成
        let fineName = makeLogFileName()
        // ログファイルのフルパス作成
        let filePath = logFolderPath().appendingPathComponent(fineName)
        
        if !logBuffer.isEmpty {
            do {
                // ファイルに保存
                try logBuffer.write(toFile: filePath, atomically: true, encoding: String.Encoding.utf8)
                return true
            } catch {
                print("JKLogger.saveLog >> exception error >> " + filePath)
                return false
            }
        }
        return false
    }
    
    /// ログファイル保存
    ///
    /// - Parameters:
    ///   - fileName: ログファイル名
    ///   - logText: ファイルログ
    /// - Returns: true:成功、false:失敗
    public func saveLog(fileName: String, logText: String) -> Bool {
        // ログファイルのフルパス作成
        let filePath = logFolderPath().appendingPathComponent(fileName)
        
        if !logText.isEmpty {
            do {
                // ファイルに保存
                try logText.write(toFile: filePath, atomically: true, encoding: String.Encoding.utf8)
                return true
            } catch {
                print("JKLogger.saveLog >> exception error >> " + filePath)
                return false
            }
        }
        return false
    }
    
    /// 保存されているログファイルをすべて削除する。
    public func clearSaveLogFiles() {
        // ログフォルダパス取得
        let folderPath = logFolderPath()
        // ログフォルダ内のファイル一覧取得
        let paths = PathUtility.filesAtPath(folderPath)
        
        print("JKLogger.clearSaveLogFiles() >> fileCount = \(paths.count)")
        
        // ログファイルを削除する
        for path in paths {
            if !FileUtility.removeFile(path) {
                print("JKLogger.removeFile() >> error")
            }
        }
    }
    
    // MARK: - public function (class methods)
    
    /// 出力設定
    public class Setting {
        /// ファイル出力するかどうか指定する
        public static var enabelFileOutput: Bool {
            set {
                AppLogger.sharedInstance.enableFileOutput = newValue
            }
            get {
                return AppLogger.sharedInstance.enableFileOutput
            }
        }
        
        /// 情報メッセージの出力が有効かどうか指定する
        public static var enableInfoMessage: Bool {
            set {
                AppLogger.sharedInstance.enableInfoMessage = newValue
            }
            get {
                return AppLogger.sharedInstance.enableInfoMessage
            }
        }
        
        /// ワーニングメッセージの出力が有効かどうか指定する
        public static var enableWarningMessage: Bool {
            set {
                AppLogger.sharedInstance.enableWarningMessage = newValue
            }
            get {
                return AppLogger.sharedInstance.enableWarningMessage
            }
        }
        
        /// エラーメッセージの出力が有効かどうか指定する
        public static var enableErrorMessage: Bool {
            set {
                AppLogger.sharedInstance.enableErrorMessage = newValue
            }
            get {
                return AppLogger.sharedInstance.enableErrorMessage
            }
        }
    }
    
    /// 保持しているログ文字列を取得する。
    ///
    /// - Returns: ログ文字列(クローン文字列)
    public class func logMessage() -> String {
        return AppLogger.sharedInstance.logBuffer.clone()
    }
    
    /// 保持しているログ文字列をクリアする
    public class func clearLogMessage() {
        AppLogger.sharedInstance.clearLogMessage()
    }
    
    /// デバッグメッセージ出力する（デバッグビルド時のみ有効）。
    ///
    /// - Parameters:
    ///   - message: 出力メッセージ
    ///   - file: ファイル名
    ///   - function: メソッド名
    ///   - line: 行番号
    public class func debug(_ message: String = "", file: String = #file, function: String = #function, line: UInt = #line) {
        #if ENABLE_LOG // デバッグビルド時のみ有効
            AppLogger.sharedInstance.write(message, file: file, function: function, line: line, date: Date(), type: .debug)
        #endif // DEBUG
    }
    
    /// 情報メッセージ出力する。
    ///
    /// - Parameters:
    ///   - message: 出力メッセージ
    ///   - file: ファイル名
    ///   - function: メソッド名
    ///   - line: 行番号
    public class func info(_ message: String = "", file: String = #file, function: String = #function, line: UInt = #line) {
        #if ENABLE_LOG
            if Setting.enableInfoMessage {
                AppLogger.sharedInstance.write(message, file: file, function: function, line: line, date: Date(), type: .info)
            }
        #endif // ENABLE_LOG
    }
    
    /// ワーニングメッセージ出力する。
    ///
    /// - Parameters:
    ///   - message: 出力メッセージ
    ///   - file: ファイル名
    ///   - function: メソッド名
    ///   - line: 行番号
    public class func warning(_ message: String = "", file: String = #file, function: String = #function, line: UInt = #line) {
        #if ENABLE_LOG
            if Setting.enableWarningMessage {
                AppLogger.sharedInstance.write(message, file: file, function: function, line: line, date: Date(), type: .warning)
            }
        #endif // ENABLE_LOG
    }
    
    /// エラーメッセージ出力する。
    ///
    /// - Parameters:
    ///   - message: 出力メッセージ
    ///   - file: ファイル名
    ///   - function: メソッド名
    ///   - line: 行番号
    public class func error(_ message: String = "", file: String = #file, function: String = #function, line: UInt = #line) {
        #if ENABLE_LOG
            if Setting.enableErrorMessage {
                AppLogger.sharedInstance.write(message, file: file, function: function, line: line, date: Date(), type: .error)
            }
        #endif // ENABLE_LOG
    }
}
