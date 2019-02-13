#!/bin/sh

#######################################################
# T-Linkアプリのローカライズファイルをプロジェクトにコピーします
#######################################################

# ローカライズファイル格納フォルダパス
basePath="../../VncServer/Localize"

echo "BasePath = "${basePath}/ar.lproj/AALocalizable.strings
echo "Copying..."

cp [ar]Localizable.strings ${basePath}/ar.lproj/Localizable.strings
cp [en]Localizable.strings ${basePath}/en.lproj/Localizable.strings
cp [es]Localizable.strings ${basePath}/es.lproj/Localizable.strings
cp [fr]Localizable.strings ${basePath}/fr.lproj/Localizable.strings
cp [id]Localizable.strings ${basePath}/id.lproj/Localizable.strings
cp [ja]Localizable.strings ${basePath}/ja.lproj/Localizable.strings
cp [ms]Localizable.strings ${basePath}/ms.lproj/Localizable.strings
cp [pt]Localizable.strings ${basePath}/pt-PT.lproj/Localizable.strings
cp [th]Localizable.strings ${basePath}/th.lproj/Localizable.strings
cp [yue]Localizable.strings ${basePath}/yue.lproj/Localizable.strings
cp [zh-Hans]Localizable.strings ${basePath}/zh-Hans.lproj/Localizable.strings

echo "copy ended..."