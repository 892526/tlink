//
//  Localize.swift
//  JKEGCommonLib
//
//  Created by 板垣勇次 on 2018/08/23.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

public class Localize {
    /// 指定したキーのローカライズ文字列を取得する。
    ///
    /// - Parameter name: 文字列キー
    /// - Returns: ローカライズ文字列
    public class func localizedString(_ name: String) -> String {
        if let bundle = Bundle(identifier: JKEGCommonLibInfo.bundleId) {
            // "Localizable.string"ファイルを参照する
            return NSLocalizedString(name, tableName: "Localizable", bundle: bundle, value: name, comment: name)
        }
        return name
    }
}
