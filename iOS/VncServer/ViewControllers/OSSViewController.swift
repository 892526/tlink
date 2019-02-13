//
//  OSSViewController.swift
//  JKVncServer
//
//  Created by 板垣勇次 on 2018/07/02.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import JKEGCommonLib
import UIKit
class OSSViewController: BaseWebViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
        
        // オープンソースライセンス
        // タイトルヘッダービュー
        setupTitleView(Localize.localizedString("SS_01_005"))
        
        // HTMLファイルロードする
        let filePath = Bundle.main.path(forResource: "open_source_license", ofType: "html")
        let url: URL = URL(fileURLWithPath: filePath!)
        loadHtml(url: url)
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
}
