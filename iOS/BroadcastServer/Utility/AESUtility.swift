//
//  AESUtility.swift
//  TestArgment
//
//  Created by 板垣勇次 on 2018/06/28.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

/// AESユーティリティクラス
/// このクラスは、「Security.framework」のリンクが必要です。
public class AESUtility {
    /// AES例外エラー
    ///
    /// - encryptFailed: 暗号化失敗
    /// - decryptFailed: 復号化失敗
    /// - otherFailed: その他失敗
    /// - argumentError: 引数エラー
    public enum AESError: Error {
        case encryptFailed(String, Any)
        case decryptFailed(String, Any)
        case otherFailed(String, Any)
        case argumentError(String, Any)
    }
    
    // アルゴリズム（AES）
    private static let settingAlgoritm: CCAlgorithm = UInt32(kCCAlgorithmAES)
    // オプション（暗号利用モード = CBC（指定なしでCBC）、パティング = PKCS7）
    private static let settingOptions: CCOptions = UInt32(kCCOptionPKCS7Padding)
    
    /// 暗号化します。
    ///
    /// - Parameters:
    ///   - data: 暗号化するデータ
    ///   - passKey: パスキー
    ///   - initialVector: 初期化ベクター
    /// - Returns: 暗号化したデータ
    /// - Throws: 暗号化失敗したとき例外を発生します。
    class func encrypt(data: Data, passKeyBytes: [UInt8], initialVectorBytes: [UInt8]) throws -> Data {
        // 暗号化後のデータのサイズを計算
        let cryptLength = size_t(Int(ceil(Double(data.count / kCCBlockSizeAES128)) + 1.0) * kCCBlockSizeAES128)
        
        // 暗号化データ格納領域確保
        var cryptData = Data(count: cryptLength)
        var numBytesEncrypted: size_t = 0
        
        // 暗号化する
        let cryptStatus = cryptData.withUnsafeMutableBytes { cryptBytes in
            data.withUnsafeBytes { dataBytes in
                CCCrypt(CCOperation(kCCEncrypt),
                        settingAlgoritm,
                        settingOptions,
                        passKeyBytes, passKeyBytes.count,
                        initialVectorBytes,
                        dataBytes, data.count,
                        cryptBytes, cryptLength,
                        &numBytesEncrypted)
            }
        }
        
        if Int32(cryptStatus) != Int32(kCCSuccess) {
            throw AESError.encryptFailed("Encrypt Failed", cryptStatus)
        }
        return cryptData
    }
    
    /// 復号化します。
    ///
    /// - Parameters:
    ///   - encryptedData: 暗号化済みデータ
    ///   - passKey: パスキー
    ///   - initialVector: 初期化ベクター
    /// - Returns: 復号化したデータ
    /// - Throws: 復号化失敗したとき例外を発生します。
    class func decrypt(encryptedData: Data, passKeyBytes: [UInt8], initialVectorBytes: [UInt8]) throws -> Data {
        let clearLength = size_t(encryptedData.count + kCCBlockSizeAES128)
        var decryptData = Data(count: clearLength)
        
        var numBytesEncrypted: size_t = 0
        
        // 復号
        let cryptStatus = decryptData.withUnsafeMutableBytes { clearBytes in
            encryptedData.withUnsafeBytes { dataBytes in
                CCCrypt(CCOperation(kCCDecrypt),
                        settingAlgoritm,
                        settingOptions,
                        passKeyBytes, passKeyBytes.count,
                        initialVectorBytes,
                        dataBytes, encryptedData.count,
                        clearBytes, clearLength,
                        &numBytesEncrypted)
            }
        }
        
        if Int32(cryptStatus) != Int32(kCCSuccess) {
            // 復号化エラーの場合は例外発生
            throw AESError.decryptFailed("Decrypt Failed", cryptStatus)
        }
        
        // パディングデータ除去
        let noPaddingDecryptData = decryptData.subdata(in: Range(0 ..< numBytesEncrypted))
        
        return noPaddingDecryptData
    }
}
