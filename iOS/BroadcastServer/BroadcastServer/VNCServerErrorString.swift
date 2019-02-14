//
//  VNCServerErrorString.swift
//  BroadcastServer
//
//  Created by 板垣勇次 on 2018/08/22.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation
import JKEGCommonLib

/// VNCServer SDK定義のエラーコードを表示文字列に変換するクラス(デバッグ用)
public class VNCServerErrorString {
    /// エラー表示文字列作成する。
    ///
    /// - Parameters:
    ///   - errorCode: VNCServerエラーコード
    ///   - name: エラー名文字列
    /// - Returns: エラー表示文字列
    private class func makeServerErrorString(errorCode: VNCServerError, name: String) -> String {
        return String("\(name) (Code: \(errorCode.rawValue))")
    }
    
    /// サーバーログレベル文字列作成する。
    ///
    /// - Parameters:
    ///   - level: ログレベル
    ///   - name: レベルカテゴリ名
    /// - Returns: サーバーログレベル文字列
    private class func makeServerLogLevelString(level: VNCServerLogLevel, name: String) -> String {
        return String("\(name) (Code: \(level.rawValue))")
    }
    
    /// サーバーエラー表示文字列を取得する。
    ///
    /// - Parameter errorCode: VNCServerエラーコード
    /// - Returns: エラー表示文字列
    public class func toString(errorCode: VNCServerError) -> String {
        var displayString = String.Empty
        
        switch errorCode {
        case VNCServerErrorNone:
            // Code:0
            displayString = Localize.localizedString("SS_03_204")
        case VNCServerErrorResources:
            // Code:1
            displayString = Localize.localizedString("SS_03_205")
        case VNCServerErrorState:
            // Code:2 (Androidと異なります)
            displayString = Localize.localizedString("SS_03_300")
        case VNCServerErrorPermissionDenied:
            // Code:3
            displayString = Localize.localizedString("SS_03_207")
        case VNCServerErrorNetworkUnreachable:
            // Code:20
            displayString = Localize.localizedString("SS_03_20")
        case VNCServerErrorHostUnreachable:
            // Code:21
            displayString = Localize.localizedString("SS_03_21")
        case VNCServerErrorConnectionRefused:
            // Code:22
            displayString = Localize.localizedString("SS_03_22")
        case VNCServerErrorDNSFailure:
            // Code:23
            displayString = Localize.localizedString("SS_03_23")
        case VNCServerErrorAddressInUse:
            // Code:24
            displayString = Localize.localizedString("SS_03_24")
        case VNCServerErrorBadPort:
            // Code:25
            displayString = Localize.localizedString("SS_03_25")
        case VNCServerErrorDisconnected:
            // Code:26
            displayString = Localize.localizedString("SS_03_26")
        case VNCServerErrorConnectionTimedOut:
            // Code:27
            displayString = Localize.localizedString("SS_03_27")
        case VNCServerErrorBearerAuthenticationFailed:
            // Code:28 (Androidと異なります)
            displayString = Localize.localizedString("SS_03_301")
        case VNCServerErrorUSBNotConnected:
            // Code:30
            displayString = Localize.localizedString("SS_03_217")
        case VNCServerErrorUnderlyingLibraryNotFound:
            // Code:31
            displayString = Localize.localizedString("SS_03_218")
        case VNCServerErrorBearerConfigurationNotProvided:
            // Code:32 (iOSのみ)
            displayString = Localize.localizedString("SS_03_302")
        case VNCServerErrorBearerConfigurationInvalid:
            // Code:33 (iOSのみ)
            displayString = Localize.localizedString("SS_03_303")
        case VNCServerErrorBearerLoadFailed:
            // Code:34 (iOSのみ)
            displayString = Localize.localizedString("SS_03_304")
        case VNCServerErrorProtocolMismatch:
            // Code:40
            displayString = Localize.localizedString("SS_03_219")
        case VNCServerErrorLoginRejected:
            // Code:41
            displayString = Localize.localizedString("SS_03_220")
        case VNCServerErrorNotLicensedForViewer:
            // Code:42
            displayString = Localize.localizedString("SS_03_221")
        case VNCServerErrorConnectionClosed:
            // Code:43
            displayString = Localize.localizedString("SS_03_222")
        case VNCServerErrorInvalidCommandString:
            // Code:44
            displayString = Localize.localizedString("SS_03_223")
        case VNCServerErrorUnsupportedAuth:
            // Code:45
            displayString = Localize.localizedString("SS_03_224")
        case VNCServerErrorKeyTooBig:
            // Code:46
            displayString = Localize.localizedString("SS_03_225")
        case VNCServerErrorBadCrypt:
            // Code:47
            displayString = Localize.localizedString("SS_03_226")
        case VNCServerErrorNoEncodings:
            // Code:48
            displayString = Localize.localizedString("SS_03_227")
        case VNCServerErrorBadPixelformat:
            // Code:49
            displayString = Localize.localizedString("SS_03_228")
        case VNCServerErrorBearerNotFound:
            // Code:50
            displayString = Localize.localizedString("SS_03_229")
        case VNCServerErrorSignatureRejected:
            // Code:51
            displayString = Localize.localizedString("SS_03_230")
        case VNCServerErrorInsufficientBufferSpace:
            // Code:52
            displayString = Localize.localizedString("SS_03_231")
        case VNCServerErrorLicenseNotValid:
            // Code:53
            displayString = Localize.localizedString("SS_03_232")
        case VNCServerErrorFeatureNotLicensed:
            // Code:54
            displayString = Localize.localizedString("SS_03_233")
        case VNCServerErrorInvalidParameter:
            // Code:60
            displayString = Localize.localizedString("SS_03_235")
        case VNCServerErrorKeyGeneration:
            // Code:63
            displayString = Localize.localizedString("SS_03_238")
        case VNCServerErrorUnableToStartService:
            // Code:64
            displayString = Localize.localizedString("SS_03_239")
        case VNCServerErrorAlreadyExists:
            // Code:65
            displayString = Localize.localizedString("SS_03_240")
        case VNCServerErrorTooManyExtensions:
            // Code:66 (iOSのみ)
            displayString = Localize.localizedString("SS_03_305")
        case VNCServerErrorReset:
            // Code:67 (iOSのみ)
            displayString = Localize.localizedString("SS_03_306")
        case VNCServerErrorDataRelayProtocolError:
            // Code:80
            displayString = Localize.localizedString("SS_03_241")
        case VNCServerErrorUnknownDataRelaySessionId:
            // Code:81
            displayString = Localize.localizedString("SS_03_242")
        case VNCServerErrorBadChallenge:
            // Code:82
            displayString = Localize.localizedString("SS_03_243")
        case VNCServerErrorDataRelayChannelTimeout:
            // Code:83
            displayString = Localize.localizedString("SS_03_244")
        case VNCServerErrorUserRefusedConnection:
            // Code:100
            displayString = Localize.localizedString("SS_03_245")
        case VNCServerErrorCommandFetchFailed:
            // Code:101
            displayString = Localize.localizedString("SS_03_246")
        case VNCServerErrorFailed:
            // Code:102
            displayString = Localize.localizedString("SS_03_247")
        case VNCServerErrorNotImplemented:
            // Code:103
            displayString = Localize.localizedString("SS_03_307")
        case VNCServerErrorCommandSuperseded:
            // Code:106
            displayString = Localize.localizedString("SS_03_251")
        case VNCServerErrorEnvironment:
            // Code:107
            displayString = Localize.localizedString("SS_03_252")
        case VNCServerErrorCaptureFrameBufferNotImplemented:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorCaptureFrameBufferNotImplemented")
        default:
            displayString = makeServerErrorString(errorCode: errorCode, name: "Error")
        }
        
        return displayString
    }
    
    /// サーバーログレベル表示文字列を取得する。
    ///
    /// - Parameters:
    ///   - logLevel: ログレベル
    /// - Returns: サーバーログレベル表示文字列
    public class func toString(logLevel: VNCServerLogLevel) -> String {
        var displayString = String.Empty
        
        switch logLevel {
        case VNCSERVER_LOG_CRITICAL:
            displayString = makeServerLogLevelString(level: logLevel, name: "CRITICAL")
        case VNCSERVER_LOG_WARNING:
            displayString = makeServerLogLevelString(level: logLevel, name: "WARNING")
        case VNCSERVER_LOG_NOTICE:
            displayString = makeServerLogLevelString(level: logLevel, name: "NOTICE")
        case VNCSERVER_LOG_INFO:
            displayString = makeServerLogLevelString(level: logLevel, name: "INFO")
        case VNCSERVER_LOG_DEBUG:
            displayString = makeServerLogLevelString(level: logLevel, name: "DEBUG")
        default:
            displayString = makeServerLogLevelString(level: logLevel, name: "UNKNOWN")
        }
        
        return displayString
    }
}
