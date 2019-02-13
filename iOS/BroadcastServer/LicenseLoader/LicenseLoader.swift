//
//  LicenseLoader.swift
//  BroadcastServer
//
//  Created by 板垣勇次 on 2018/08/07.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation
import JKEGCommonLib

/// ライセンスファイルロードクラス
public class LicenseLoader {
    /// ライセンスファイルの拡張子
    private static let licenseFileExtention = ".vnclicense"
    
    // MARK: - Private methods
    
    /// ライセンスファイル読み込む
    ///
    /// - Parameter filePath: ファイルパス
    /// - Returns: ライセンスファイルテキスト
    private class func loadFile(_ filePath: String) -> String? {
        var liceseText: String?
        if let data = FileManager.default.contents(atPath: filePath) {
            do {
                // AESで復号化する
                let decryptedData = try AESUtility.decrypt(encryptedData: data, passKeyBytes: VncLicenseEncryptionConst.passKey, initialVectorBytes: VncLicenseEncryptionConst.initialVector)
                
                // 文字列に変換する
                liceseText = String(data: decryptedData, encoding: .utf8)
            } catch {
                AppLogger.error("License file decrypt failed...")
            }
        }
        return liceseText
    }
    
    // MARK: - Public methods
    
    /// 暗号化済みライセンスファイルをロードし、復号化してテキストを取得する。
    ///
    /// - Parameters:
    ///   - searchPath: ライセンスファイル検索パス
    ///   - fileName: ロードするライセンスファイル名。指定しない場合はnilを設定（最初に見つかったファイルをロードする）。
    /// - Returns: 復号化したライセンステキスト
    public class func loadLicense(searchPath: String, fileName: String?) -> String? {
        AppLogger.debug()
        
        var liceseText: String?
        
        // 指定ファイル名あるとき
        if let fileName = fileName {
            // ファイルパス作成
            let fullPath = searchPath + "/" + fileName
            
            // ファイル読み込む
            liceseText = loadFile(fullPath)
        } else {
            // 指定ファイル名が無いので、拡張子で検索して最初に見つかったファイルを読み込む
            
            // FileManager取得
            do {
                // ライセンスファイルパス取得1
                let pathContents: [String] = try FileManager.default.contentsOfDirectory(atPath: searchPath)
                
                // ライセンスファイルの拡張子でフィルターする
                let licensePaths = pathContents.filter { (path) -> Bool in
                    return path.hasSuffix(licenseFileExtention)
                }
                
                // ライセンスファイルあるとき
                if !licensePaths.isEmpty {
                    for path in licensePaths {
                        AppLogger.debug("License file = " + path)
                        
                        // ファイルパス作成
                        let fullPath = searchPath + "/" + path
                        
                        // ファイル読み込む
                        liceseText = loadFile(fullPath)
                    }
                }
            } catch {
                AppLogger.error("License file search failed...")
            }
        }
        return liceseText
    }
}
