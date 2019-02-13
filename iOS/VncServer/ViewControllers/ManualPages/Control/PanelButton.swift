//
//  PanelButton.swift
//  VncServer
//
//  Created by 板垣勇次 on 2018/10/03.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import UIKit

/// パネルボタンクラス
class PanelButton: UIButton {
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        // 角丸に設定する
        layer.cornerRadius = 10
        layer.masksToBounds = true
        
        // 複数行に対応
        if let label = self.titleLabel {
            label.lineBreakMode = .byWordWrapping
            label.numberOfLines = 0
            label.textAlignment = .center
        }
    }
}
