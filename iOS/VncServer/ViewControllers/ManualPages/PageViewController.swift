//
//  PageViewController.swift
//  TestPageViewController
//
//  Created by 板垣勇次 on 2018/09/04.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import UIKit

/// １つのページビューコントローラークラス
class PageViewController: UIPageViewController, UIPageViewControllerDelegate, UIPageViewControllerDataSource {
    @IBOutlet weak var pageController: UIPageControl!
    var manualViewController: PageContentViewController?
    var pageViewControllers: [PageContentViewController]?
    
    var manualPageInfos: [ManualPageInfo]?
    
    /// ページタイトル
    public var pageTitles: [String]?
    
    /// 説明文
    public var pageDescriptions: [String]?
    
    /// 画像ファイル名
    public var pageImageNames: [String]?
    
    /// 表示インデックス変更ハンドラ
    private var indexChangedHandler: ((Int) -> Void)?
    
    /// ロードか完了
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
        
        let viewController = pageViewControllers![0]
        
        setViewControllers([viewController], direction: .forward, animated: true, completion: nil)
        dataSource = self as UIPageViewControllerDataSource
        delegate = self as UIPageViewControllerDelegate
        
        view.layer.backgroundColor = UIColor.clear.cgColor
    }
    
    /// メモリワーニング
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: - Navigation
    
    /*
     // In a storyboard-based application, you will often want to do a little preparation before navigation
     override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
     // Get the new view controller using segue.destinationViewController.
     // Pass the selected object to the new view controller.
     
     }
     */
    
    // MARK: - PageViewController Delegate methods
    
    /// ページ変更通知（前のページ）
    ///
    /// - Parameters:
    ///   - pageViewController: ページビューコントローラー
    ///   - viewController: 前のページ
    /// - Returns: 次に表示するページのビューコントローラー
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerBefore viewController: UIViewController) -> UIViewController? {
        // 右にスワイプしたので、ページを戻す
        if let vcs = self.pageViewControllers {
            // 表示インデックス計算
            let fromVC = viewController as! PageContentViewController
            let nextIndex = fromVC.index - 1
            
            // 範囲内ならば、該当のビューコントローラー表示
            if nextIndex >= 0 {
                if vcs.count > nextIndex {
                    let nextVC = vcs[nextIndex]
                    nextVC.view.setNeedsUpdateConstraints()
                    nextVC.view.setNeedsDisplay()
                    return nextVC
                    // return vcs[nextIndex]
                }
            }
        }
        
        return nil
    }
    
    /// ページ変更通知（次のページ）
    ///
    /// - Parameters:
    ///   - pageViewController: ページビューコントローラー
    ///   - viewController: <#viewController description#>
    /// - Returns: 次に表示するページのビューコントローラー
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerAfter viewController: UIViewController) -> UIViewController? {
        // 左にスワイプしたので、ページを進める
        if let vcs = self.pageViewControllers {
            // 表示インデックス計算
            let fromVC = viewController as! PageContentViewController
            let nextIndex = fromVC.index + 1
            
            // 範囲内ならば、該当のビューコントローラー表示
            if nextIndex < count {
                if vcs.count > nextIndex {
                    let nextVC = vcs[nextIndex]
                    nextVC.view.setNeedsUpdateConstraints()
                    nextVC.view.setNeedsDisplay()
                    return nextVC
                    // return vcs[nextIndex]
                }
            }
        }
        return nil
    }
    
    /// ページ変更完了通知
    ///
    /// - Parameters:
    ///   - pageViewController: ページビューコントローラー
    ///   - finished: アニメーション完了状態
    ///   - previousViewControllers: <#previousViewControllers description#>
    ///   - completed: <#completed description#>
    func pageViewController(_ pageViewController: UIPageViewController, didFinishAnimating finished: Bool, previousViewControllers: [UIViewController],
                            transitionCompleted completed: Bool) {
        let prevVC = previousViewControllers[0] as! PageContentViewController
        if let viewController = pageViewController.viewControllers?.first as? PageContentViewController {
            print("didFinishAnimating >> index = \(viewController.index), PreIndex = \(prevVC.index)")
            
            if let handler = indexChangedHandler {
                handler(viewController.index)
            }
        }
    }
    
    /*
     func pageViewControllerSupportedInterfaceOrientations(_ pageViewController: UIPageViewController) -> UIInterfaceOrientationMask {
     return [.portrait, .landscapeLeft, .landscapeRight]
     }
     
     func pageViewControllerPreferredInterfaceOrientationForPresentation(_ pageViewController: UIPageViewController) -> UIInterfaceOrientation {
     if UIApplication.shared.statusBarOrientation.isPortrait {
     return UIInterfaceOrientation.portrait
     } else {
     return UIInterfaceOrientation.landscapeRight
     }
     }
     */
    
    // MARK: - private methods
    
    /// ページ数取得する。
    private var count: Int {
        if let vcs = self.pageViewControllers {
            return vcs.count
        }
        return 0
    }
    
    /// 現在表示中のページインデックスひ取得する。
    private var currentIndex: Int {
        return (viewControllers?.first as! PageContentViewController).index
    }
    
    /// インデック番号から該当のビューコントローラーを取得する。
    ///
    /// - Parameter index: インデックス番号
    /// - Returns: ビューコントローラー
    private func getViewControllerAtIndex(_ index: Int) -> PageContentViewController? {
        if (pageViewControllers?.count)! > index {
            return pageViewControllers![index]
        }
        return pageViewControllers?[0]
    }
    
    /// 指定した名前のストーリーボードのビューコントローラをロードする。
    ///
    /// - Parameter name: ストーリーボード名
    /// - Returns: ビューコントローラー
    private func loadViewControllerForName(_ name: String) -> PageContentViewController {
        let storyboard = UIStoryboard(name: name, bundle: nil)
        return storyboard.instantiateInitialViewController() as! PageContentViewController
    }
    
    /// 全ページビルドする。
    ///
    /// - Parameter manualPageInfos: ページ情報リスト
    private func buildPages(_ manualPageInfos: [ManualPageInfo]) {
        // ビューコントローラー配列初期化
        pageViewControllers = []
        
        // ページ数分作成
        for index in 0 ..< manualPageInfos.count {
            // ページ情報取得
            let pageInfo = manualPageInfos[index]
            
            // ページ種別からロードするストリーボードを選択する
            var resourceName = "PageContentViewController"
            if pageInfo.pageType() == ManualPageInfo.ManualPageType.headUnit {
                resourceName = "PageContentHUViewController"
            }
            
            // 1ページ分のビューコントローラ作成
            let viewController = loadViewControllerForName(resourceName)
            
            // ビューコントローラをセットアップする
            viewController.setup(index: index, pageInfo: pageInfo)
            
            // リストにう追加
            pageViewControllers?.append(viewController)
        }
    }
    
    // MARK: - Public methods
    
    /// セットアップする。
    ///
    /// - Parameters:
    ///   - manualPageInfos: マニュアルページ情報リスト
    ///   - indexChanged: インデックス変更通知メソッド
    public func setup(manualPageInfos: [ManualPageInfo], indexChanged: @escaping (Int) -> Void) {
        // インデックス変更通知ハンドラ保持
        indexChangedHandler = indexChanged
        
        // ページ情報リスト
        self.manualPageInfos = manualPageInfos
        
        // 　ページ構築
        buildPages(manualPageInfos)
    }
    
    /// ページ再構築
    public func rebuildPage() {
        if let infos = manualPageInfos {
            // 現在表示中のインデックス番号
            let nowIndex = currentIndex
            
            // 　ページ再構築
            buildPages(infos)
            
            // カレントビューをセット
            let viewController = pageViewControllers![nowIndex]
            setViewControllers([viewController], direction: .forward, animated: false, completion: nil)
        }
    }
    
    /// 指定舌ページインデックスのページを表示する。
    ///
    /// - Parameter index: 表示するページインデックス番号
    public func showPage(_ index: Int) {
        if currentIndex == index {
            // 表示中のページの場合は処理しない
            return
        }
        
        if let vcs = pageViewControllers {
            // 範囲内ならば切り替える
            if vcs.count > index {
                let viewController = vcs[index]
                
                // ページ遷移方向
                var direction: UIPageViewController.NavigationDirection = .forward
                if currentIndex > index {
                    // 前のページに戻す
                    direction = .reverse
                }
                // ページ切り替え
                setViewControllers([viewController], direction: direction, animated: true, completion: nil)
            }
        }
    }
}
