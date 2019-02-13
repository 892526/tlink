//
//  StringExtension.swift
//  TestArgment
//
//  Created by 板垣勇次 on 2018/06/18.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

// MARK: - Stringクラス拡張

extension String {

    // MARK: - NSSStringにあるメソッドを実装
    
    /// 自分自身を返します
    private var nstr: NSString {
        return (self as NSString)
    }
    
    /// 指定したインデックス以降の文字列を取得する。
    ///
    /// - Parameter index: 切り出し開始位置
    /// - Returns: 切り出し文字列
    public func substring(from index: Int) -> String {
        return nstr.substring(from: index)
    }
    
    /// 指定したインデックスまでの文字列を取得する。
    ///
    /// - Parameter index: 切り出し終了位置
    /// - Returns: 切り出し文字列
    public func substring(to index: Int) -> String {
        return nstr.substring(to: index)
    }
    
    /// 指定した範囲の文字列を取得する。
    ///
    /// - Parameter range: 切り出し範囲
    /// - Returns: 切り出し文字列
    public func substring(with range: NSRange) -> String {
        return nstr.substring(with: range)
    }
    
    /// パス中のサイドのコンポーネントを取得する。
    public var lastPathComponent: String {
        return nstr.lastPathComponent
    }
    
    /// 拡張子を取得する。
    public var pathExtension: String {
        return nstr.pathExtension
    }
    
    /// パス中の最後のコンポーネントを取得する。
    public var deletingLastPathComponent: String {
        return nstr.deletingLastPathComponent
    }
    
    /// パス中の拡張子を削除する。
    public var deletingPathExtension: String {
        return nstr.deletingPathExtension
    }
    
    /// パスを分割する。
    public var pathComponents: [String] {
        return nstr.pathComponents
    }
    
    /// パスにコンポーネントを追加する。
    ///
    /// - Parameter str: 追加コンポーネント
    /// - Returns: コンポーネントを追加したパス
    public func appendingPathComponent(_ str: String) -> String {
        return nstr.appendingPathComponent(str)
    }
    
    /// 拡張子を追加する
    ///
    /// - Parameter str: 追加する拡張子文字列
    /// - Returns: 拡張子追加したパス
    public func appendingPathExtension(_ str: String) -> String? {
        return nstr.appendingPathExtension(str)
    }
    
    // MARK: - 拡張メソッド
    
    /// 文字列のクローンを作成する。
    ///
    /// - Returns: クローン文字列
    public func clone() -> String {
        return String(self)
    }
    
    /// 指定した文字列がnil、または空(サイズ０)かどうか取得します。
    ///
    /// - Parameter value: 評価文字列
    /// - Returns: nilまたは空である:true、nilまたは空ではない:false
    public static func isNirOrEmpty(_ value: String?) -> Bool {
        if (value == nil) || (value == String.Empty) {
            return true
        }
        return false
    }
    
    /// 空文字列を取得します
    public static let Empty: String = ""
}
