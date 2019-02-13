//
//  VNCServerErrorString.swift
//  BroadcastServer
//
//  Created by 板垣勇次 on 2018/08/22.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation

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
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorNone")
        case VNCServerErrorResources:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorResources")
        case VNCServerErrorPermissionDenied:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorPermissionDenied")
        case VNCServerErrorNetworkUnreachable:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorNetworkUnreachable")
        case VNCServerErrorHostUnreachable:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorHostUnreachable")
        case VNCServerErrorConnectionRefused:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorConnectionRefused")
        case VNCServerErrorDNSFailure:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorDNSFailure")
        case VNCServerErrorAddressInUse:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorAddressInUse")
        case VNCServerErrorBadPort:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorBadPort")
        case VNCServerErrorDisconnected:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorDisconnected")
        case VNCServerErrorConnectionTimedOut:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorConnectionTimedOut")
        case VNCServerErrorBearerAuthenticationFailed:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorBearerAuthenticationFailed")
        case VNCServerErrorUSBNotConnected:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorUSBNotConnected")
        case VNCServerErrorUnderlyingLibraryNotFound:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorUnderlyingLibraryNotFound")
        case VNCServerErrorBearerConfigurationNotProvided:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorBearerConfigurationNotProvided")
        case VNCServerErrorBearerConfigurationInvalid:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorBearerConfigurationInvalid")
        case VNCServerErrorBearerLoadFailed:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorBearerLoadFailed")
        case VNCServerErrorProtocolMismatch:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorProtocolMismatch")
        case VNCServerErrorLoginRejected:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorLoginRejected")
        case VNCServerErrorNotLicensedForViewer:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorNotLicensedForViewer")
        case VNCServerErrorConnectionClosed:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorConnectionClosed")
        case VNCServerErrorUnsupportedAuth:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorUnsupportedAuth")
        case VNCServerErrorKeyTooBig:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorKeyTooBig")
        case VNCServerErrorBadCrypt:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorBadCrypt")
        case VNCServerErrorNoEncodings:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorNoEncodings")
        case VNCServerErrorBadPixelformat:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorBadPixelformat")
        case VNCServerErrorBearerNotFound:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorBearerNotFound")
        case VNCServerErrorSignatureRejected:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorSignatureRejected")
        case VNCServerErrorInsufficientBufferSpace:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorInsufficientBufferSpace")
        case VNCServerErrorLicenseNotValid:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorLicenseNotValid")
        case VNCServerErrorFeatureNotLicensed:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorFeatureNotLicensed")
        case VNCServerErrorInvalidParameter:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorInvalidParameter")
        case VNCServerErrorKeyGeneration:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorKeyGeneration")
        case VNCServerErrorUnableToStartService:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorUnableToStartService")
        case VNCServerErrorAlreadyExists:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorAlreadyExists")
        case VNCServerErrorTooManyExtensions:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorTooManyExtensions")
        case VNCServerErrorReset:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorReset")
        case VNCServerErrorDataRelayProtocolError:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorDataRelayProtocolError")
        case VNCServerErrorUnknownDataRelaySessionId:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorUnknownDataRelaySessionId")
        case VNCServerErrorBadChallenge:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorBadChallenge")
        case VNCServerErrorDataRelayChannelTimeout:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorDataRelayChannelTimeout")
        case VNCServerErrorUserRefusedConnection:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorUserRefusedConnection")
        case VNCServerErrorCommandFetchFailed:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorCommandFetchFailed")
        case VNCServerErrorFailed:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorFailed")
        case VNCServerErrorNotImplemented:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorNotImplemented")
        case VNCServerErrorCommandSuperseded:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorCommandSuperseded")
        case VNCServerErrorEnvironment:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorEnvironment")
        case VNCServerErrorCaptureFrameBufferNotImplemented:
            displayString = makeServerErrorString(errorCode: errorCode, name: "VNCServerErrorCaptureFrameBufferNotImplemented")
        default:
            displayString = makeServerErrorString(errorCode: errorCode, name: "Unknown Error Code")
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
