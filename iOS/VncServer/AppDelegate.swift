//
//  AppDelegate.swift
//  VncServer
//
//  Created by 板垣勇次 on 2018/08/06.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import JKEGCommonLib
import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        
        #if ENABLE_LOG
            // ログ有効時にのみ設定
            AppLogger.Setting.enabelFileOutput = true
            AppLogger.debug()
        #endif // ENABLE_LOG
        
        if AppGroupsManager.loadInitializeState() == false {
            // 未初期化なので、初期化します
            AppGroupsManager.saveInitializedState(value: true)
            AppGroupsManager.reset()
        }
        
        // ローカル通知の許可をユーザーに問い合わせる
        LocalNotification.checkNotificationAuthorization { granted in
            AppLogger.debug("checkNotificationAuthorization >> granted = " + String(granted))
            if !granted {
                LocalNotification.requestAuthorization(completionHandler: { granted, error in
                    var errorMessage: String = String.Empty
                    if let errorObj = error {
                        errorMessage = errorObj.localizedDescription
                    }
                    AppLogger.debug("requestAuthorization >> granted = " + String(granted) + ", error = " + errorMessage)
                })
            }
        }
        return true
    }
    
    func applicationWillResignActive(_ application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
    }
    
    func applicationDidEnterBackground(_ application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    }
    
    func applicationWillEnterForeground(_ application: UIApplication) {
        // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
    }
    
    func applicationDidBecomeActive(_ application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }
    
    func applicationWillTerminate(_ application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    }
}
