//
//  RealVncSdkUtility.swift
//  VncServer
//
//  Created by 板垣勇次 on 2018/08/23.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

/// RealVNC Server SDKユーティリティ
public class RealVncSdkUtility {
    /// ビルドバージョン
    ///
    /// - Returns: ビルドバージョン文字列
    public class func buildVersion() -> String {
        #if USE_IOS_SIMULATOR
            return "---"
        #else
            if let sdkVersion = VNCServer.buildVersion() {
                return sdkVersion
            }
            return String.Empty
        #endif
    }
}
