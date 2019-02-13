//
//  PageManagerViewController.swift
//  VncServer
//
//  Created by 板垣勇次 on 2018/09/06.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import JKEGCommonLib
import UIKit

class PageManagerViewController: UIViewController {
    @IBOutlet weak var pageController: UIPageControl!
    
    private var pageViewController: PageViewController?
    
    public var pageTitles: [String]?
    public var pageDescriptions: [String]?
    public var pageImageNames: [String]?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
        
        pageController.currentPage = 0
        
        // 閉じるボタン設定
        navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: .stop, target: self, action: #selector(closeView))
        
        // 回転通知受信設定
        addNotification()
        
        // 文字列が長い場合に入りきるように動的にフォントサイズを変更するようにUILabelを作成して
        // NavigationTileViewに設定する
        let titleLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 200, height: 40))
        titleLabel.font = UIFont.boldSystemFont(ofSize: 17.0)
        titleLabel.text = title
        titleLabel.adjustsFontSizeToFitWidth = true
        titleLabel.backgroundColor = UIColor.clear
        titleLabel.textAlignment = .center
        titleLabel.numberOfLines = 2
        titleLabel.sizeToFit()
        
        navigationItem.titleView = titleLabel
        
        // 元のタイトルをクリア
        title = nil
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: - Navigation
    
    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
        
        if segue.destination.isKind(of: PageViewController.self) {
            let viewController = segue.destination as! PageViewController
            pageViewController = viewController
            
            pageController.numberOfPages = (pageTitles?.count)!
            
            viewController.setup(titles: pageTitles!, descriptions: pageDescriptions!, imageNames: pageImageNames!, indexChanged: { index in
                print("SelectedIndex = \(index)")
                self.pageController.currentPage = index
            })
        }
    }
    
    @objc func closeView() {
        // 閉じる
        dismiss(animated: true, completion: nil)
    }
    
    // MARK: - Action
    
    /// UIPageControlをタップしたときのイベント
    ///
    /// - Parameter sender: UIPageControlのインスタンス
    @IBAction func pageControlValueChanged(_ sender: Any) {
        let pageCtrl = sender as! UIPageControl
        AppLogger.debug("CurrentPage = \(pageCtrl.currentPage)")
        
        if let pageVC = pageViewController {
            pageVC.showPage(pageCtrl.currentPage)
        }
    }
    
    // MARK: - Rotation notification
    
    /// 回転通知登録
    func addNotification() {
        AppLogger.debug()
        
        // 回転通知
        NotificationCenter.default.addObserver(self, selector: #selector(onOrientationDidChange(notification:)),
                                               name: UIDevice.orientationDidChangeNotification, object: nil)
    }
    
    /// 回転通知解除
    func removeNotification() {
        AppLogger.debug()
        NotificationCenter.default.removeObserver(self, name: UIDevice.orientationDidChangeNotification, object: nil)
    }
    
    /// 表示方向変更通知
    ///
    /// - Parameter notification: 通知情報
    @objc func onOrientationDidChange(notification: NSNotification) {
        // デバイスの向きを取得
        let orientation: UIInterfaceOrientation = UIApplication.shared.statusBarOrientation
        AppLogger.debug("orientation = \(orientation)")
        
        if let pageVC = pageViewController {
            pageVC.rebuildPage()
        }
        
        if let titleViewControl = navigationItem.titleView {
            let titleView = titleViewControl as! UILabel
            let frameValue = getTitleViewFrameSize()
            
            if orientation.isPortrait {
                titleView.frame = frameValue
                titleView.sizeToFit()
            } else {
                titleView.frame = frameValue
                titleView.sizeToFit()
            }
        }
    }
    
    /// タイトルサイズ取得する
    ///
    /// - Returns: タイトルエリアサイズ
    private func getTitleViewFrameSize() -> CGRect {
        let value = view.frame.width * 0.7
        if UIDevice.current.orientation.isPortrait {
            return CGRect(x: 0, y: 0, width: value, height: 40.0)
        } else {
            return CGRect(x: 0, y: 0, width: value, height: 30.0)
        }
    }
}
