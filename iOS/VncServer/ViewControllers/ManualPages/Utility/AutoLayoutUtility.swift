//
//  AutoLayoutUtility.swift
//  VncServer
//
//  Created by 板垣勇次 on 2018/10/03.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import Foundation
import UIKit

/// Autolayoutのユーティリティクラス
public class AutoLayoutUtility {
    /// Size Class情報取得する。
    ///
    /// - Parameter sizeClass: Size Class情報
    /// - Returns: Size Class種別
    public class func getSizeClassDescription(_ sizeClass: UIUserInterfaceSizeClass) -> String {
        switch sizeClass {
        case .unspecified:
            return "Any"
        case .regular:
            return "Regular"
        case .compact:
            return "Compact"
        }
    }
    
    /// InterfaceIdiomを取得する。
    ///
    /// - Parameter idiom: idiom上フオ
    /// - Returns: InterfaceIdiom種別
    public class func getInterfaceIdiomDescription(_ idiom: UIUserInterfaceIdiom) -> String {
        switch idiom {
        case .unspecified:
            return "Unspecified"
        case .phone:
            return "Phone"
        case .pad:
            return "Pad"
        case .carPlay:
            return "CarPlay"
        case .tv:
            return "TV"
        }
    }
    
    /// ログ出力
    ///
    /// - Parameter traitCollection: traitCollection情報
    public class func logTraitCollection(traitCollection: UITraitCollection) {
        NSLog("Horizontal Size Class: %@", AutoLayoutUtility.getSizeClassDescription(traitCollection.horizontalSizeClass))
        NSLog("Vertical Size Class  : %@", AutoLayoutUtility.getSizeClassDescription(traitCollection.verticalSizeClass))
        NSLog("User Interface Idiom : %@", AutoLayoutUtility.getInterfaceIdiomDescription(traitCollection.userInterfaceIdiom))
        NSLog("Display Scale Factor : %f,", traitCollection.displayScale)
    }
}
