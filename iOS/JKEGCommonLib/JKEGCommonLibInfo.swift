//
//  .swift
//  JKEGCommonLib
//
//  Created by 板垣勇次 on 2018/08/21.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

public class JKEGCommonLibInfo {
    // JKEGCommonLibのバンドルID
    public static let bundleId = "com.jvckenwood-eng.product.JKEGCommonLib"
    
    /// JKEGCommonLibバンドルのバージョンを取得する
    public static var version: String {
        if let bundle = Bundle(identifier: JKEGCommonLibInfo.bundleId) {
            let versionString: String = bundle.object(forInfoDictionaryKey: "CFBundleVersion") as! String
            return versionString
        }
        return String.Empty
    }
}
