//
//  TransparentButton.swift
//  VncServer
//
//  Created by 板垣勇次 on 2018/11/07.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import UIKit

class TransparentButton: UIButton {
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        // 角丸に設定する
        layer.cornerRadius = 10
        layer.masksToBounds = true
        
        // 白色の半透過
        backgroundColor = UIColor.white.withAlphaComponent(0.6)
        
        // 複数行に対応
        if let label = self.titleLabel {
            label.lineBreakMode = .byWordWrapping
            label.numberOfLines = 0
            label.textAlignment = .center
        }
    }
}
