//
//  PageContentViewController.swift
//  VncServer
//
//  Created by 板垣勇次 on 2018/09/05.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import JKEGCommonLib
import UIKit

class PageContentViewController: UIViewController {
    @IBOutlet weak var labelTitle: UILabel!
    @IBOutlet weak var imageView: UIImageView!
    @IBOutlet weak var labelSescription: UILabel!
    @IBOutlet weak var viewCentering: UIView!
    
    // @IBOutlet weak var contentView: PageContentView!
    
    private var pageIndex: Int = 0
    private var pageTitle: String = ""
    private var pageDescription: String = ""
    private var pageImageName: String = ""
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
        
        labelTitle.text = pageTitle
        labelSescription.text = pageDescription
        imageView.image = getImage(pageImageName)
        view.layer.backgroundColor = UIColor.clear.cgColor
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    /*
     // MARK: - Navigation
     
     // In a storyboard-based application, you will often want to do a little preparation before navigation
     override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
     // Get the new view controller using segue.destinationViewController.
     // Pass the selected object to the new view controller.
     }
     */
    
    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        AppLogger.debug()
        
        if let collection = previousTraitCollection {
            AutoLayoutUtility.logTraitCollection(traitCollection: collection)
        }
    }
    
    override func willTransition(to newCollection: UITraitCollection,
                                 with coordinator: UIViewControllerTransitionCoordinator) {
        AppLogger.debug()
    }
    
    override func viewWillTransition(to size: CGSize,
                                     with coordinator: UIViewControllerTransitionCoordinator) {
        AppLogger.debug()
    }
    
    // MARK: - private methods
    
    private func getImage(_ named: String) -> UIImage {
        if let image = UIImage(named: named) {
            return image
        }
        return UIImage()
    }
    
    // MARK: - property
    
    public var index: Int {
        return pageIndex
    }
    
    // MARK: - public methods
    
    public func setup(index: Int, title: String, description: String, imageName: String) {
        pageIndex = index
        pageTitle = title
        pageDescription = description
        pageImageName = imageName
    }
}
