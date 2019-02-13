//
//  DateExtention.swift
//  JKCommon
//
//  Created by 板垣勇次 on 2018/06/19.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

// MARK: - Dateクラス拡張

extension Date {
    /// 指定した日付フォーマットで整形した文字列を取得する。
    ///
    /// - Parameter format: 出力する日付フォーマット。デフォルトは"yyyy-MM-dd_HH-mm-ss.SSS"。
    /// - local: ロケール言語コード。(例)"ja-JP"。
    /// - Returns: 日付文字列
    public static func toString(_ format: String = "yyyy-MM-dd_HH-mm-ss.SSS", local: String = "ja-JP") -> String {
        // フォーマッター作成
        let formatter = DateFormatter()
        // ロケール指定
        formatter.locale = Locale(identifier: local)
        // 出力フォーマット指定
        formatter.dateFormat = format
        
        // フォーマットに沿った日付文字列取得
        return formatter.string(from: Date())
    }
    
    /// 指定した日付フォーマットで整形した文字列を取得する。
    ///
    /// - Parameters:
    ///   - format: 出力する日付フォーマット。デフォルトは"yyyy-MM-dd_HH-mm-ss.SSS"
    ///   - local: ロケール言語コード。デフォルトロケールは"ja-JP"。
    /// - Returns: 日付文字列
    public func toString(_ format: String = "yyyy-MM-dd_HH-mm-ss.SSS Z", local: String = "ja-JP") -> String {
        // フォーマッター作成
        let formatter = DateFormatter()
        // ロケール指定
        formatter.locale = Locale(identifier: local)
        // 出力フォーマット指定
        formatter.dateFormat = format
        
        // フォーマットに沿った日付文字列取得
        return formatter.string(from: self)
    }
}
