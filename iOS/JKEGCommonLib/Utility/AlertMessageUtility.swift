//
//  MessageBox.swift
//  TestArgment
//
//  Created by 板垣勇次 on 2018/06/26.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation
import UIKit

// MARK: - アラートメッセージ表示クラス

/// アラートメッセージ表示クラス
public class AlertMessageUtility {
    /// メッセージボックス種別
    ///
    /// - typeOk: OKボタンのみ
    /// - typeOkCancel: OK/Cancelボタン
    /// - typeCancelOk: Cancel/Okボタン
    /// - typeCancelRetry: Cancel/Retryボタン
    public enum AlertMessageUtilityType {
        case typeOk
        case typeOkCancel
        case typeCancelOk
        case typeCancelRetry
    }
    
    /// メッセージボックスのボタン種別
    ///
    /// - typeOk: OKボタン
    /// - typeCancel: キャンセルボタン
    /// - typeRetry: リトライ
    public enum AlertMessageUtilityButtonType {
        case typeOk
        case typeCancel
        case typeRetry
    }
    
    /// Alertメッセージを表示する。
    ///
    /// - Parameters:
    ///   - owner: オーナービューコントローラー
    ///   - title: メッセージタイトル
    ///   - message: メッセージ本文
    ///   - type: 表示ボタン種別
    ///   - completion: ボタン選択完了ハンドラ。選択したボタンを引数で渡す。
    public class func show(owner: UIViewController, title: String, message: String, type: AlertMessageUtilityType, completion: ((_ selectedButtonType: AlertMessageUtilityButtonType) -> Void)?) {
        // AlertController作成
        let alertController: UIAlertController = UIAlertController(title: title, message: message, preferredStyle: UIAlertController.Style.alert)
        
        switch type {
        case .typeOk:
            // OKボタン
            addActionButton(alertController: alertController, buttonTitle: JKEGCommonString.localizedString("JKEG_COMMON_BUTTON_TITLE_OK"), buttonType: .typeOk, completionHandler: completion)
            
        case .typeOkCancel:
            // OKボタン
            addActionButton(alertController: alertController, buttonTitle: JKEGCommonString.localizedString("JKEG_COMMON_BUTTON_TITLE_OK"), buttonType: .typeOk, completionHandler: completion)
            
            // Cancelボタン
            addActionButton(alertController: alertController, buttonTitle: JKEGCommonString.localizedString("JKEG_COMMON_BUTTON_TITLE_CANCEL"), buttonType: .typeCancel, completionHandler: completion)
            
        case .typeCancelOk:
            // Cancelボタン
            addActionButton(alertController: alertController, buttonTitle: JKEGCommonString.localizedString("JKEG_COMMON_BUTTON_TITLE_CANCEL"), buttonType: .typeCancel, completionHandler: completion)
            
            // OKボタン
            addActionButton(alertController: alertController, buttonTitle: JKEGCommonString.localizedString("JKEG_COMMON_BUTTON_TITLE_OK"), buttonType: .typeOk, completionHandler: completion)
            
        case .typeCancelRetry:
            // OKボタン
            addActionButton(alertController: alertController, buttonTitle: JKEGCommonString.localizedString("JKEG_COMMON_BUTTON_TITLE_OK"), buttonType: .typeOk, completionHandler: completion)
            
            // Retryボタン
            addActionButton(alertController: alertController, buttonTitle: JKEGCommonString.localizedString("JKEG_COMMON_BUTTON_TITLE_RETRY"), buttonType: .typeRetry, completionHandler: completion)
        }
        
        // Alert表示
        owner.present(alertController, animated: true, completion: nil)
    }
    
    /// Alertメッセージを表示する。表示ボタン数は任意に指定可能。
    ///
    /// - Parameters:
    ///   - owner: オーナービューコントローラー
    ///   - title: メッセージタイトル
    ///   - message: メッセージ本文
    ///   - buttonTitles: ボタンタイトル（ボタン数指定する）
    ///   - completion: ボタン選択完了ハンドラ。選択したボタンのインデックス番号（0開始）を返す。
    public class func show(owner: UIViewController, title: String, message: String, buttonTitles: [String], completion: ((_ selectedButtonIndex: UInt) -> Void)?) {
        // AlertController作成
        let alertController: UIAlertController = UIAlertController(title: title, message: message, preferredStyle: UIAlertController.Style.alert)
        
        // ボタンインデックス番号（0開始）
        var index: UInt = 0
        
        for title in buttonTitles {
            // Action作成
            let alertAction: JKAlertAction = JKAlertAction(title: title, style: UIAlertAction.Style.default) { action in
                let action: JKAlertAction = action as! JKAlertAction
                
                if let handler = completion {
                    // ボタンタップ通知
                    handler(action.index)
                }
            }
            // インデックス番号設定
            alertAction.index = index
            
            // ボタン登録
            alertController.addAction(alertAction)
            
            // インデックス番号インクリメント
            index += 1
        }
        
        // Alert表示
        owner.present(alertController, animated: true, completion: nil)
    }
    
    /// 入力機能付きAlertViewを表示する。
    ///
    /// - Parameters:
    ///   - owner: オーナービューコントローラー
    ///   - title: タイトル
    ///   - message: メッセージ
    ///   - placeholders: プレイスホルダー
    ///   - completion: ボタン選択完了ハンドラ。入力データを引数で返す。
    public class func showInputView(owner: UIViewController, title: String, message: String, placeholders: [String], completion: ((_ inputValues: [String]) -> Void)?) {
        // AlertController作成
        let alertController: UIAlertController = UIAlertController(title: title, message: message, preferredStyle: UIAlertController.Style.alert)
        
        // Cancelボタン
        addActionButton(alertController: alertController, buttonTitle: JKEGCommonString.localizedString("JKEG_COMMON_BUTTON_TITLE_CANCEL"), buttonType: .typeCancel, completionHandler: nil)
        
        // OKボタン
        addActionButton(alertController: alertController, buttonTitle: JKEGCommonString.localizedString("JKEG_COMMON_BUTTON_TITLE_OK"), buttonType: .typeOk, completionHandler: { handler in
            
            var values = [String]()
            
            if let name = alertController.textFields?.first?.text {
                if let addr = alertController.textFields?.last?.text {
                    values.append(name)
                    values.append(addr)
                }
            }
            
            if let handler = completion {
                handler(values)
            }
        })
        
        //textfiledの追加
        alertController.addTextField(configurationHandler: { (text: UITextField!) -> Void in
            text.placeholder = placeholders[0]
        })
        // 実行した分textfiledを追加される。
        alertController.addTextField(configurationHandler: { (text: UITextField!) -> Void in
            text.placeholder = placeholders[1]
        })
        
        // Alert表示
        owner.present(alertController, animated: true, completion: nil)
    }
    
    // MARK: - Private functions
    
    /// カスタムAlertAction
    private class JKAlertAction: UIAlertAction {
        /// インデックス番号
        public var index: UInt = 0
    }
    
    /// ボタン追加する
    ///
    /// - Parameters:
    ///   - alertController: AlertControllerインスタンス
    ///   - buttonTitle: ボタンタイトル
    ///   - buttonType: ボタン種別
    ///   - completionHandler: ボタン押下イベント
    private class func addActionButton(alertController: UIAlertController, buttonTitle: String, buttonType: AlertMessageUtilityButtonType,
                                       completionHandler: ((_ selectedButtonIndex: AlertMessageUtilityButtonType) -> Void)?) {
        alertController.addAction(UIAlertAction(title: buttonTitle, style: UIAlertAction.Style.default, handler: { _ in
            if let handler = completionHandler {
                // OKボタンタップ通知
                handler(buttonType)
            }
        }))
    }
}
