//
//  SampleHandler.swift
//  BroadcastServer
//
//  Created by 板垣勇次 on 2018/08/07.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import JKEGCommonLib
import ReplayKit

class SampleHandler: RPBroadcastSampleHandler {

    // MARK: - Override methods
    
    override func broadcastStarted(withSetupInfo setupInfo: [String: NSObject]?) {
        // User has requested to start the broadcast. Setup info from the UI extension can be supplied but optional.
        
        // デバッグログ初期化
        #if ENABLE_LOG
            // ファイル保存用バッファ有効
            AppLogger.Setting.enabelFileOutput = false
        #else
            // ファイル保存用バッファ無効
            AppLogger.Setting.enabelFileOutput = false
        #endif
        AppLogger.debug()
        
        if AppGroupsManager.loadInitializeState() == false {
            // 未初期化なので、初期化します
            AppGroupsManager.saveInitializedState(value: true)
            AppGroupsManager.reset()
        }
        
        DispatchQueue.main.async {
            // VNCサーバー開始
            BroadcastVNCServer.shared.startServer()
        }
    }
    
    override func broadcastAnnotated(withApplicationInfo applicationInfo: [AnyHashable: Any]) {
        AppLogger.debug()
    }
    
    override func broadcastPaused() {
        // User has requested to pause the broadcast. Samples will stop being delivered.
        AppLogger.debug()
    }
    
    override func broadcastResumed() {
        // User has requested to resume the broadcast. Samples delivery will resume.
        AppLogger.debug()
    }
    
    override func broadcastFinished() {
        // User has requested to finish the broadcast.
        AppLogger.debug()
        
        // VNCサーバー停止
        /*
         AppLogger.debug("stopServer -- START ---")
         BroadcastVNCServer.shared.stopServer()
         AppLogger.debug("stopServer -- END ---")
         */
        
        // デバッグログを共有ファイルに出力
        #if ENABLE_LOG
            AppLogger.debug("exportDebugLog -- START ---")
            exportDebugLog()
            AppLogger.debug("exportDebugLog -- END ---")
        #endif
        
        DispatchQueue.main.async {
            AppLogger.debug("DispatchQueue.main.async -- START ---")
            
            // VNCサーバー停止
            BroadcastVNCServer.shared.stopServer()
            
            AppLogger.debug("DispatchQueue.main.async -- END ---")
        }
    }
    
    override func processSampleBuffer(_ sampleBuffer: CMSampleBuffer, with sampleBufferType: RPSampleBufferType) {
        if sampleBufferType.rawValue == RPSampleBufferType.video.rawValue {
            // AppLogger.debug("RPSampleBufferType.video")
            
            // キャプチャーデータをVNCServerに渡す
            BroadcastVNCServer.shared.processVideoFrame(buffer: sampleBuffer)
        }
        
        #if _ONLY_VIDE_DATA_USE_ // Videoデータのみ使用する
            switch sampleBufferType {
            case RPSampleBufferType.video:
                // Handle video sample buffer
                // AppLogger.debug("RPSampleBufferType.video")
                
                // キャプチャーデータをVNCServerに渡す
                BroadcastVNCServer.shared.processVideoFrame(buffer: sampleBuffer)
                
            case RPSampleBufferType.audioApp:
                // Handle audio sample buffer for app audio
                AppLogger.debug("RPSampleBufferType.audioApp")
                
            case RPSampleBufferType.audioMic:
                // Handle audio sample buffer for mic audio
                AppLogger.debug("RPSampleBufferType.audioMic")
            }
        #endif // _NOT_USED_
    }
    
    // MARK: - Debug methods
    
    /// デバッグログを共有ファイルで書き出す
    private func exportDebugLog() {
        AppLogger.debug()
        if AppLogger.Setting.enabelFileOutput {
            let logString = AppLogger.sharedInstance.getLogBuffer()
            if let data = logString.data(using: .utf8) {
                let result = FileCoordinatorUtility.writeData(data: data, fileName: AppExtentionMessageInfo.appLogFileName, groupID: AppGroupsManager.groupID)
                print("exportDebugLog = \(result)")
            }
        }
    }
}
