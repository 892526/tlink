//
//  AppLoggerSettingTableViewController.swift
//  TestArgment
//
//  Created by 板垣勇次 on 2018/06/26.
//  Copyright © 2018年 JVCKENWOOD Engineering Corporation. All rights reserved.
//

import UIKit

public class AppLoggerSettingTableViewController: UITableViewController {
    enum JKLoggerSettingType: Int {
        case general = 0
        case destinationAccount = 1
        case resetSettings = 2
    }
    
    @IBOutlet weak var acountSettingCell: UITableViewCell!
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false
        
        // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
        // self.navigationItem.rightBarButtonItem = self.editButtonItem
        
        title = "ログ設定"
        
        // 設定セル更新
        if let setting = DebugSettings.loadSetting() {
            updateAccountCell(setting)
        }
    }
    
    public override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: - Table view data source
    
    public override func numberOfSections(in _: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 3
    }
    
    public override func tableView(_: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        
        switch section {
        case JKLoggerSettingType.general.rawValue:
            return 1
            
        case JKLoggerSettingType.destinationAccount.rawValue:
            return 1
            
        case JKLoggerSettingType.resetSettings.rawValue:
            return 1
        default:
            return 0
        }
    }
    
    public override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        AppLogger.debug()
        
        tableView.deselectRow(at: indexPath, animated: true)
        
        if indexPath.section == JKLoggerSettingType.destinationAccount.rawValue {
            AlertMessageUtility.showInputView(owner: self, title: "アカウント設定", message: "メール送信先のアカウント(名前、メールアドレス)を設定してください。",
                                              placeholders: ["名前", "メールアドレス"]) { inputValues in
                print("\(inputValues[0]), \(inputValues[1])")
                
                if inputValues[0].isEmpty || inputValues[1].isEmpty {
                    DispatchQueue.main.async {
                        AlertMessageUtility.show(owner: self, title: "エラー", message: "名前、またはメールアドレスが入力されていません。",
                                                 type: AlertMessageUtility.AlertMessageUtilityType.typeOk, completion: nil)
                    }
                    
                } else {
                    let settings = DebugSettingInfo(name: inputValues[0], address: inputValues[1])
                    if DebugSettings.saveSetting(info: settings) {
                        self.updateAccountCell(settings)
                    }
                }
            }
        } else if indexPath.section == JKLoggerSettingType.resetSettings.rawValue {
            AlertMessageUtility.show(owner: self, title: "確認", message: "デバッグ設定を初期化しますか？", type: .typeCancelOk) { selectedButtonType in
                if selectedButtonType == AlertMessageUtility.AlertMessageUtilityButtonType.typeOk {
                    let settings = DebugSettingInfo()
                    if DebugSettings.saveSetting(info: settings) {
                        self.updateAccountCell(settings)
                    }
                }
            }
        }
    }
    
    /*
     public override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
     let cell = tableView.dequeueReusableCell(withIdentifier: "reuseIdentifier", for: indexPath)
     
     // Configure the cell...
     
     return cell
     }
     */
    
    /*
     // Override to support conditional editing of the table view.
     override func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
     // Return false if you do not want the specified item to be editable.
     return true
     }
     */
    
    /*
     // Override to support editing the table view.
     override func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
     if editingStyle == .delete {
     // Delete the row from the data source
     tableView.deleteRows(at: [indexPath], with: .fade)
     } else if editingStyle == .insert {
     // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
     }
     }
     */
    
    /*
     // Override to support rearranging the table view.
     override func tableView(_ tableView: UITableView, moveRowAt fromIndexPath: IndexPath, to: IndexPath) {
     
     }
     */
    
    /*
     // Override to support conditional rearranging of the table view.
     override func tableView(_ tableView: UITableView, canMoveRowAt indexPath: IndexPath) -> Bool {
     // Return false if you do not want the item to be re-orderable.
     return true
     }
     */
    
    /*
     // MARK: - Navigation
     
     // In a storyboard-based application, you will often want to do a little preparation before navigation
     override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
     // Get the new view controller using segue.destinationViewController.
     // Pass the selected object to the new view controller.
     }
     */
    
    /// アカウントセル更新
    ///
    /// - Parameter info: アカウント設定情報
    private func updateAccountCell(_ info: DebugSettingInfo) {
        if let cell = self.acountSettingCell {
            cell.textLabel?.text = info.accountName
            cell.detailTextLabel?.text = info.accountAddress
        }
    }
}
