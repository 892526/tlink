//
//  UnitTestFileCoordinator.swift
//  VncServerTests
//
//  Created by 板垣勇次 on 2018/08/20.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation
import JKEGCommonLib
import XCTest

public class UnitTestFileCoordinator {
    private static let fileName = "TEST_FILE_NAME.txt"
    private static let testDataText1 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    private static let testDataText2 = "JVCケンウッド - あいうえおかきくけこさしすせそなにぬねのはひふえほ"
    private static let testDataText3 = "東京都八王子市石川町"
    
    private static let fileNameList: [String] = ["TEST_FILE_NAME1.txt", "TEST_FILE_NAME2.txt", "TEST_FILE_NAME3.txt"]
    private static let testDataTextList: [String] = [testDataText1, testDataText2, testDataText3]
    
    public class func testWrite() {
        var result: Bool = false
        
        for index in 0 ..< fileNameList.count {
            if let data = testDataTextList[index].data(using: .utf8) {
                result = FileCoordinatorUtility.writeData(data: data, fileName: fileNameList[index], groupID: AppGroupsManager.groupID)
                XCTAssertTrue(result, "DataFile write error")
            } else {
                XCTFail("text converting error")
            }
        }
    }
    
    public class func testRead() {
        for index in 0 ..< fileNameList.count {
            do {
                if let readData = try FileCoordinatorUtility.readData(fileName: fileNameList[index], groupID: AppGroupsManager.groupID) {
                    AppLogger.debug("OK (Data = \(readData.count) bytes")
                    
                    if !readData.isEmpty {
                        let text = String(data: readData, encoding: .utf8)
                        XCTAssertEqual(text, testDataTextList[index])
                    } else {
                        XCTFail("data read error (no data)")
                    }
                    
                } else {
                    AppLogger.debug("NG")
                }
            } catch FileCoordinatorUtility.ReadAPIError.fileNotFound {
                print("data read error (File not found)")
            } catch FileCoordinatorUtility.ReadAPIError.otherError {
                XCTFail("data read error (otherError)")
            } catch let error {
                XCTFail("data read error (\(error.localizedDescription)")
            }
        }
    }
    
    public class func testRemove() {
        for index in 0 ..< fileNameList.count {
            let result = FileCoordinatorUtility.removeData(fileName: fileNameList[index], groupID: AppGroupsManager.groupID)
            XCTAssertTrue(result, "DataFile remove error")
        }
    }
}
