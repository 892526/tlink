//
//  LoadingView.swift
//  JKVncServer
//
//  Created by 板垣勇次 on 2018/07/24.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import UIKit

class LoadingView: UIView {
    @IBOutlet weak var activityIndicatorView: UIActivityIndicatorView!
    
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
    /// - Returns: ロードしたLoadingViewのインスタンス
    class func createInstance() -> LoadingView {
        let loadingView: LoadingView = (Bundle.main.loadNibNamed("LoadingView", owner: self, options: nil)?.first as? LoadingView)!
        return loadingView
    }
    
    func startAniamting() {
        if activityIndicatorView != nil {
            activityIndicatorView.startAnimating()
        }
    }
    
    func stopAnimating() {
        if activityIndicatorView != nil {
            activityIndicatorView.stopAnimating()
        }
    }
}
