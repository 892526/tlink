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
            // Code:0 この文言は実際に表示しないため、文言ファイルにありません。
            displayString = "No Error"
        case VNCServerErrorResources:
            // Code:1
            displayString = Localize.localizedString("TID_5136")
        case VNCServerErrorState:
            // Code:2 (Androidと異なります)
            displayString = Localize.localizedString("TID_5158")
        case VNCServerErrorPermissionDenied:
            // Code:3
            displayString = Localize.localizedString("TID_5138")
        case VNCServerErrorNetworkUnreachable:
            // Code:20
            displayString = Localize.localizedString("TID_5139")
        case VNCServerErrorHostUnreachable:
            // Code:21
            displayString = Localize.localizedString("TID_5187")
        case VNCServerErrorConnectionRefused:
            // Code:22
            displayString = Localize.localizedString("TID_5178")
        case VNCServerErrorDNSFailure:
            // Code:23
            displayString = Localize.localizedString("TID_5250")
        case VNCServerErrorAddressInUse:
            // Code:24
            displayString = Localize.localizedString("TID_5143")
        case VNCServerErrorBadPort:
            // Code:25
            displayString = Localize.localizedString("TID_5144")
        case VNCServerErrorDisconnected:
            // Code:26
            displayString = Localize.localizedString("TID_5145")
        case VNCServerErrorConnectionTimedOut:
            // Code:27
            displayString = Localize.localizedString("TID_5146")
        case VNCServerErrorBearerAuthenticationFailed:
            // Code:28 (Androidと異なります)
            displayString = Localize.localizedString("TID_5147")
        case VNCServerErrorUSBNotConnected:
            // Code:30
            displayString = Localize.localizedString("TID_5148")
        case VNCServerErrorUnderlyingLibraryNotFound:
            // Code:31
            displayString = Localize.localizedString("TID_5155")
        case VNCServerErrorBearerConfigurationNotProvided:
            // Code:32 (iOSのみ)
            displayString = Localize.localizedString("TID_5150")
        case VNCServerErrorBearerConfigurationInvalid:
            // Code:33 (iOSのみ)
            displayString = Localize.localizedString("TID_5151")
        case VNCServerErrorBearerLoadFailed:
            // Code:34 (iOSのみ)
            displayString = Localize.localizedString("TID_5134")
        case VNCServerErrorProtocolMismatch:
            // Code:40
            displayString = Localize.localizedString("TID_5160")
        case VNCServerErrorLoginRejected:
            // Code:41
            displayString = Localize.localizedString("TID_5253")
        case VNCServerErrorNotLicensedForViewer:
            // Code:42
            displayString = Localize.localizedString("TID_5252")
        case VNCServerErrorConnectionClosed:
            // Code:43
            displayString = Localize.localizedString("TID_5254")
        case VNCServerErrorInvalidCommandString:
            // Code:44
            displayString = Localize.localizedString("TID_5157")
        case VNCServerErrorUnsupportedAuth:
            // Code:45
            displayString = Localize.localizedString("TID_5154")
        case VNCServerErrorKeyTooBig:
            // Code:46
            displayString = Localize.localizedString("TID_5159")
        case VNCServerErrorBadCrypt:
            // Code:47
            displayString = Localize.localizedString("TID_5255")
        case VNCServerErrorNoEncodings:
            // Code:48
            displayString = Localize.localizedString("TID_5162")
        case VNCServerErrorBadPixelformat:
            // Code:49
            displayString = Localize.localizedString("TID_5256")
        case VNCServerErrorBearerNotFound:
            // Code:50
            displayString = Localize.localizedString("TID_5152")
        case VNCServerErrorSignatureRejected:
            // Code:51
            displayString = Localize.localizedString("TID_5142")
        case VNCServerErrorInsufficientBufferSpace:
            // Code:52
            displayString = Localize.localizedString("TID_5165")
        case VNCServerErrorLicenseNotValid:
            // Code:53
            displayString = Localize.localizedString("TID_5166")
        case VNCServerErrorFeatureNotLicensed:
            // Code:54
            displayString = Localize.localizedString("TID_5167")
        case VNCServerErrorInvalidParameter:
            // Code:60
            displayString = Localize.localizedString("TID_5168")
        case VNCServerErrorKeyGeneration:
            // Code:63
            displayString = Localize.localizedString("TID_5169")
        case VNCServerErrorUnableToStartService:
            // Code:64
            displayString = Localize.localizedString("TID_5170")
        case VNCServerErrorAlreadyExists:
            // Code:65
            displayString = Localize.localizedString("TID_5171")
        case VNCServerErrorTooManyExtensions:
            // Code:66 (iOSのみ)
            displayString = Localize.localizedString("TID_5172")
        case VNCServerErrorReset:
            // Code:67 (iOSのみ)
            displayString = Localize.localizedString("TID_5173")
        case VNCServerErrorDataRelayProtocolError:
            // Code:80
            displayString = Localize.localizedString("TID_5174")
        case VNCServerErrorUnknownDataRelaySessionId:
            // Code:81
            displayString = Localize.localizedString("TID_5259")
        case VNCServerErrorBadChallenge:
            // Code:82
            displayString = Localize.localizedString("TID_5260")
        case VNCServerErrorDataRelayChannelTimeout:
            // Code:83
            displayString = Localize.localizedString("TID_5177")
        case VNCServerErrorUserRefusedConnection:
            // Code:100
            displayString = Localize.localizedString("TID_5156")
        case VNCServerErrorCommandFetchFailed:
            // Code:101
            displayString = Localize.localizedString("TID_5163")
        case VNCServerErrorFailed:
            // Code:102
            displayString = Localize.localizedString("TID_5261")
        case VNCServerErrorNotImplemented:
            // Code:103 (Androidと異なる)
            displayString = Localize.localizedString("TID_5181")
        case VNCServerErrorCommandSuperseded:
            // Code:106
            displayString = Localize.localizedString("TID_5265")
        case VNCServerErrorEnvironment:
            // Code:107
            displayString = Localize.localizedString("TID_5183")
        case VNCServerErrorCaptureFrameBufferNotImplemented:
            // Code:120
            displayString = Localize.localizedString("TID_5184")
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
