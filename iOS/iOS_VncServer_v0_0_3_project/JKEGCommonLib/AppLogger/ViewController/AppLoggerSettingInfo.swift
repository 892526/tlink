//
//  AppLoggerSettingInfo.swift
//  TestArgment
//
//  Created by 板垣勇次 on 2018/06/26.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

public class AppLoggerSettingInfo: NSObject {
    public var accounts: [String] = [String]()
    
    override init() {
        super.init()
    }
    
    public func loadSetting() -> Bool {
        return false
    }
    
    public func saveSetting() -> Bool {
        return false
    }
}
