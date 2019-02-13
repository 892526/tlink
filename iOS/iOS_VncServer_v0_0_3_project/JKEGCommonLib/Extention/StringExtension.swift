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
    
    public func substring(from index: Int) -> String {
        return nstr.substring(from: index)
    }
    
    public func substring(to index: Int) -> String {
        return nstr.substring(to: index)
    }
    
    public func substring(with range: NSRange) -> String {
        return nstr.substring(with: range)
    }
    
    public var lastPathComponent: String {
        return nstr.lastPathComponent
    }
    
    public var pathExtension: String {
        return nstr.pathExtension
    }
    
    public var deletingLastPathComponent: String {
        return nstr.deletingLastPathComponent
    }
    
    public var deletingPathExtension: String {
        return nstr.deletingPathExtension
    }
    
    public var pathComponents: [String] {
        return nstr.pathComponents
    }
    
    public func appendingPathComponent(_ str: String) -> String {
        return nstr.appendingPathComponent(str)
    }
    
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
