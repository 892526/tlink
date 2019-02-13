//
//  PleaseConnectView.swift
//  JKVncServer
//
//  Created by 板垣勇次 on 2018/06/29.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import UIKit

class PleaseConnectView: UIView {

    // MARK: instance variable
    
    @IBOutlet weak var animationAreaView: UIView!
    @IBOutlet weak var imageViewTextLight: UIImageView!
    
    // アニメーション中かどうか
    private var isAnimating: Bool = false
    
    // MARK: initializer methods
    
    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
    }
    
    // MARK: public methods
    
    /// XIBをロードしてインスタンス取得する。
    ///
    /// - Returns: ロードしたPleaseConnectViewのインスタンス
    class func loadInstance() -> PleaseConnectView {
        let pleaseConnectView: PleaseConnectView = (Bundle.main.loadNibNamed("PleaseConnectView", owner: self, options: nil)?.first as? PleaseConnectView)!
        return pleaseConnectView
    }
    
    /// アニメーション開始する。
    func startAnimation() {
        if !isAnimating {
            isAnimating = true
            UIView.animate(withDuration: 4.0, delay: 0, options: .repeat, animations: {
                self.imageViewTextLight.transform = self.imageViewTextLight.transform.translatedBy(x: self.bounds.size.width, y: 0)
            }, completion: nil)
        }
    }
    
    /// アニメーション停止する
    func stopAnimation() {
        if isAnimating {
            imageViewTextLight.layer.removeAllAnimations()
            imageViewTextLight.transform = imageViewTextLight.transform.translatedBy(x: -bounds.size.width, y: 0)
            isAnimating = false
        }
    }
}
