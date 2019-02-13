//
//  AccessoryConnectedView.swift
//  JKVncServer
//
//  Created by 板垣勇次 on 2018/07/02.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import JKEGCommonLib
import UIKit

class AccessoryConnectedView: UIView {
    @IBOutlet weak var animationAreaView: UIImageView!
    
    // アニメーション中かどうか
    private var isAnimating: Bool = false
    
    // MARK: initializer methods
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        // Nibをロードします
        // loadNib()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        
        // Nibをロードします
        // loadNib()
    }
    
    /*
     /// Nibをロードしてビューに追加します
     func loadNib() {
     AppLogger.debug()
     
     if let views = Bundle.main.loadNibNamed("AccessoryConnectedView", owner: self, options: nil) {
     if let view = views.first as? UIView {
     view.frame = bounds
     addSubview(view)
     }
     }
     }
     */
    
    // MARK: public methods
    
    /// XIBをロードしてインスタンス取得する。
    ///
    /// - Returns: ロードしたAccessoryConnectedViewのインスタンス
    class func loadInstance() -> AccessoryConnectedView {
        let connectView: AccessoryConnectedView = (Bundle.main.loadNibNamed("AccessoryConnectedView", owner: self, options: nil)?.first as? AccessoryConnectedView)!
        return connectView
    }
    
    /// アニメーション開始する。
    func startAnimation() {
        AppLogger.debug()
        /*
         if !isAnimating {
         isAnimating = true
         
         /*
          UIView.animateKeyframes(withDuration: 2.0, delay: 0, options: [.repeat, .autoreverse], animations: {
          // 0.0秒かけてαを0に これを入れないとアニメーションが止まってしまう
          UIView.addKeyframe(withRelativeStartTime: 0.0, relativeDuration: 0.0, animations: {
          self.highlightedView.alpha = 0
          })
          
          UIView.addKeyframe(withRelativeStartTime: 0.0, relativeDuration: 0.5, animations: {
          self.highlightedView.alpha = 1
          })
          }, completion: nil)
          */
         
         UIView.animate(withDuration: 2.0, delay: 0, options: [.repeat, .autoreverse], animations: {
         self.highlightedView.alpha = 0
         }, completion: nil)
         }
         */
    }
    
    /// アニメーション停止する
    func stopAnimation() {
        AppLogger.debug()
        /*
         if isAnimating {
         highlightedView.layer.removeAllAnimations()
         highlightedView.alpha = 1
         isAnimating = false
         }
         */
    }
}
