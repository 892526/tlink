//
//  VncServerTests.swift
//  VncServerTests
//
//  Created by 板垣勇次 on 2018/08/06.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

// @testable import VncServer
import XCTest

class VncServerTests: XCTestCase {
    override func setUp() {
        super.setUp()
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }
    
    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
        super.tearDown()
    }
    
    /// AESユーティリティーテスト
    func testAES() {
        // AES暗号化テスト
        UnitTestAesUtility.testEncrypt()
        // AES復号化テスト
        UnitTestAesUtility.testDecrypt()
    }
    
    /// ライセンスファイル読み込みテスト
    func testLicenseLoader() {
        // BroadcastVNCServerクラステスト
        UnitTestBroadcastVNCServer.testLoadLicense()
    }
    
    /// ファイル共有で読み書きテスト
    func testFileCoordinator() {
        UnitTestFileCoordinator.testWrite()
        UnitTestFileCoordinator.testRead()
        UnitTestFileCoordinator.testRemove()
    }
    
    func testPerformanceExample() {
        // This is an example of a performance test case.
        measure {
            // Put the code you want to measure the time of here.
        }
    }
}
