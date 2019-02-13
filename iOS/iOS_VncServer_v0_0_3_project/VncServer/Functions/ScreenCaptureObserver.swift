//
//  ScreenCaptureObserver.swift
//  VncServer
//
//  Created by 板垣勇次 on 2018/08/24.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation
import JKEGCommonLib

/// 画面キャプチャー動作監視クラス
public class ScreenCaptureObserver: NSObject {
    /// KVOインスタンス管理
    private var keyValueObservation: NSKeyValueObservation?
    
    /// 初期化します
    override init() {
        super.init()
    }
    
    /// 終了処理
    deinit {
        // KVO解除
        stop()
    }
    
    /// 画面キャプチャー状態を取得する。
    public var isCaptured: Bool {
        return UIScreen.main.isCaptured
    }
    
    ///
    /// 監視開始する。
    ///
    /// - Parameter changedHandler: キャプチャー状態変更通知用ハンドラ
    public func start(changedHandler: @escaping (Bool) -> Void) {
        AppLogger.debug()
        
        if keyValueObservation == nil {
            // 監視開始
            keyValueObservation = UIScreen.main.observe(\.isCaptured) { screen, _ in
                changedHandler(screen.isCaptured)
            }
        }
    }
    
    /// 監視停止する。
    public func stop() {
        AppLogger.debug()
        
        // 監視停止
        if let obj = keyValueObservation {
            obj.invalidate()
            keyValueObservation = nil
        }
    }
}
