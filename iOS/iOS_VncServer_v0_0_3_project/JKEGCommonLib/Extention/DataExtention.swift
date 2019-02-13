//
//  DataExtention.swift
//  JKEGCommonLib
//
//  Created by 板垣勇次 on 2018/08/22.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

// MARK: - Dataクラス拡張

extension Data {
    /// バイト配列を取得する。
    ///
    /// - Returns: バイト配列
    public func toByteArray() -> [UInt8] {
        return withUnsafeBytes {
            [UInt8](UnsafeBufferPointer(start: $0, count: self.count / MemoryLayout<UInt8>.stride))
        }
    }
}
