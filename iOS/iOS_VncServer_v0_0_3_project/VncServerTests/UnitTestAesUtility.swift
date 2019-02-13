//
//  UnitTestAesUtility.swift
//  AESSampleApp
//
//  Created by 板垣勇次 on 2018/07/31.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import UIKit
import XCTest

/// AESUtilityクラスの単体テストコード
class UnitTestAesUtility: NSObject {
    private static let originalFileName = "jvck-development-server-exp20190426-2b3f12e8-e71e-4c6e-be16-f26ba0f34b65"
    private static let encryptFileName = "RealVNCServer"
    private static let fileExtention = "vnclicense"
    
    private static let encryptSouceFileNames: [String] = [originalFileName, "TEST01", "TEST02", "TEST03"]
    private static let encryptRefereceFileNames: [String] = [encryptFileName, "(Encrypt)TEST01", "(Encrypt)TEST02", "(Encrypt)TEST03"]
    
    private static let decryptSourceFileNames: [String] = [encryptFileName, "(Encrypt)TEST01", "(Encrypt)TEST02", "(Encrypt)TEST03"]
    private static let decryptReferenceFileNames: [String] = [originalFileName, "TEST01", "TEST02", "TEST03"]
    
    /// 暗号化テスト
    ///
    /// - Parameters:
    ///   - fileName: 暗号化するファイル名
    ///   - referenceFileName: 比較用ファイル名
    private class func testEncrypt(fileName: String, referenceFileName: String) {
        print("testEncrypt >> SRC = \(fileName), REF = \(referenceFileName)")
        if let orgData = loadFile(fileName) {
            if let encryptedData = loadFile(referenceFileName) {
                do {
                    let encData = try AESUtility.encrypt(data: orgData, passKeyBytes: VncLicenseEncryptionConst.passKey, initialVectorBytes: VncLicenseEncryptionConst.initialVector)
                    
                    // サイズ比較
                    XCTAssertTrue((encryptedData.count == encData.count), "Not match data length...")
                    // データ比較
                    XCTAssertEqual(encryptedData, encData, "Not match data contents...")
                } catch {
                    XCTFail("testEncrypt failed...")
                }
            } else {
                XCTFail("encryptedData load failed...")
            }
        } else {
            XCTFail("orgData load failed...")
        }
    }
    
    /// 復号化テスト
    ///
    /// - Parameters:
    ///   - fileName: 復号化するファイル名
    ///   - referenceFileName: 比較用ファイル名
    private class func testDecrypt(fileName: String, referenceFileName: String) {
        print("testEncrypt >> SRC = \(fileName), REF = \(referenceFileName)")
        if let orgData = loadFile(referenceFileName) {
            if let encryptedData = loadFile(fileName) {
                do {
                    let decryptData = try AESUtility.decrypt(encryptedData: encryptedData, passKeyBytes: VncLicenseEncryptionConst.passKey, initialVectorBytes: VncLicenseEncryptionConst.initialVector)
                    
                    // サイズ比較
                    XCTAssertTrue((orgData.count == decryptData.count), "Not match data length...")
                    // データ比較
                    XCTAssertEqual(orgData, decryptData, "Not match data contents...")
                } catch {
                    XCTFail("testDecrypt failed...")
                }
            } else {
                XCTFail("encryptedData load failed...")
            }
        } else {
            XCTFail("orgData load failed...")
        }
    }
    
    // MARK: - public methods
    
    /// 暗号化テスト
    class func testEncrypt() {
        for index in 0 ..< encryptSouceFileNames.count {
            testEncrypt(fileName: encryptSouceFileNames[index], referenceFileName: encryptRefereceFileNames[index])
        }
    }
    
    /// 復号化テスト
    class func testDecrypt() {
        for index in 0 ..< decryptSourceFileNames.count {
            testDecrypt(fileName: decryptSourceFileNames[index], referenceFileName: decryptReferenceFileNames[index])
        }
    }
    
    /// ライセンスファイルロードする
    ///
    /// - Returns: ライセンスファイルテキスト
    class func loadLicenseFile() -> String? {
        var licenseText: String?
        
        // ファイルをロードする
        if let loadData = loadFile(encryptFileName) {
            do {
                // 復号化する
                let decryptData = try AESUtility.decrypt(encryptedData: loadData, passKeyBytes:
                    VncLicenseEncryptionConst.passKey, initialVectorBytes: VncLicenseEncryptionConst.initialVector)
                
                // 文字列に変換(UTF8)
                licenseText = String(data: decryptData, encoding: .utf8)
            } catch {
                print("loadLicenseFile >> error...")
            }
        }
        return licenseText
    }
    
    /// バンドル内のファイルをロードします
    ///
    /// - Parameter fileName: ファイル名
    /// - Returns: ロードデータ
    private class func loadFile(_ fileName: String) -> Data? {
        var data: Data?
        if let path = Bundle.main.path(forResource: fileName, ofType: fileExtention) {
            do {
                data = try Data(contentsOf: URL(fileURLWithPath: path))
            } catch {
                print("do not load file...")
            }
        }
        return data
    }
}
