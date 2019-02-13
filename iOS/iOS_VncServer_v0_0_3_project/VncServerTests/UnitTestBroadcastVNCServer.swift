//
//  UnitTestBroadcastVNCServer.swift
//  VncServerTests
//
//  Created by 板垣勇次 on 2018/08/07.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation
import XCTest

class UnitTestBroadcastVNCServer {
    class func loadPlaneLicenseFile() -> String? {
        var licenseText: String?
        
        if let path = Bundle.main.path(forResource: "jvck-development-server-exp20190426-2b3f12e8-e71e-4c6e-be16-f26ba0f34b65", ofType: "vnclicense") {
            do {
                let data = try Data(contentsOf: URL(fileURLWithPath: path))
                licenseText = String(data: data, encoding: .utf8)
            } catch {
                print("do not load file...")
            }
        }
        
        return licenseText
    }
    
    class func testLoadLicense() {
        let licenseText = LicenseLoader.loadLicense(searchPath: Bundle.main.bundlePath, fileName: "RealVNCServer.vnclicense")
        
        if let orgText = loadPlaneLicenseFile() {
            XCTAssertEqual(licenseText, orgText, "Not match data contents...")
        } else {
            XCTFail("Not match data contents...")
        }
    }
    
    class func testInitialize() {
        // let broadcasrServer = BroadcastVNCServer()
    }
}
