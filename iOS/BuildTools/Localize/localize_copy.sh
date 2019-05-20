#!/bin/bash

#######################################################
# T-Linkアプリのローカライズファイルをプロジェクトにコピーします
#######################################################

# ローカライズファイル格納フォルダパス
basePath="../../VncServer/Resources"

echo "BasePath = "${basePath}
echo "Copying..."


# ----------------------------------------------
# 関数定義
# ----------------------------------------------
# 各言語のファイルを言語フォルダにコピーする
function file_copy () {
    echo "Arg = "${1}

    cp "(Arabic_WorldArabic)"${1} ${basePath}/ar.lproj/${1}
    cp "(English_US)"${1} ${basePath}/en.lproj/${1}
    cp "(Spanish)"${1} ${basePath}/es.lproj/${1}
    cp "(French)"${1} ${basePath}/fr.lproj/${1}
    cp "(BahasaIndonesia)"${1} ${basePath}/id.lproj/${1}
    cp "(BahasaMalaysia)"${1} ${basePath}/ms.lproj/${1}
    cp "(Portuguese)"${1} ${basePath}/pt-PT.lproj/${1}
    cp "(Thai)"${1} ${basePath}/th.lproj/${1}
    cp "(Chinese_Mandarin)"${1} ${basePath}/zh-Hant.lproj/${1}
    cp "(Chinese_Cantonese)"${1} ${basePath}/zh-Hans.lproj/${1}
}

# アプリ概要ファイルを言語フォルダにコピーする
function app_overview_file_copy () {
    echo "Arg = "${1}

    cp "(iOS)_[ar]"${1} ${basePath}/ar.lproj/${1}
    cp "(iOS)_[en]"${1} ${basePath}/en.lproj/${1}
    cp "(iOS)_[es]"${1} ${basePath}/es.lproj/${1}
    cp "(iOS)_[fr]"${1} ${basePath}/fr.lproj/${1}
    cp "(iOS)_[id]"${1} ${basePath}/id.lproj/${1}
    cp "(iOS)_[ms]"${1} ${basePath}/ms.lproj/${1}
    cp "(iOS)_[pt]"${1} ${basePath}/pt-PT.lproj/${1}
    cp "(iOS)_[th]"${1} ${basePath}/th.lproj/${1}
    cp "(iOS)_[zh-hk]"${1} ${basePath}/zh-Hant.lproj/${1}
    cp "(iOS)_[zh-cn]"${1} ${basePath}/zh-Hans.lproj/${1}
}

# 利用規約ファイルを言語フォルダにコピーする
function eula_file_copy () {
    echo "Arg = "${1}

    cp "[ar]"${1} ${basePath}/ar.lproj/${1}
    cp "[en]"${1} ${basePath}/en.lproj/${1}
    cp "[es]"${1} ${basePath}/es.lproj/${1}
    cp "[fr]"${1} ${basePath}/fr.lproj/${1}
    cp "[id]"${1} ${basePath}/id.lproj/${1}
    cp "[ms]"${1} ${basePath}/ms.lproj/${1}
    cp "[pt]"${1} ${basePath}/pt-PT.lproj/${1}
    cp "[th]"${1} ${basePath}/th.lproj/${1}
    cp "[zh-hk]"${1} ${basePath}/zh-Hant.lproj/${1}
    cp "[zh-cn]"${1} ${basePath}/zh-Hans.lproj/${1}
}



# ----------------------------------------------
# UI表示文言
# ----------------------------------------------
uiFile="Localizable.strings"
file_copy ${uiFile}

#cp [ar]Localizable.strings ${basePath}/ar.lproj/Localizable.strings
#cp [en]Localizable.strings ${basePath}/en.lproj/Localizable.strings
#cp [es]Localizable.strings ${basePath}/es.lproj/Localizable.strings
#cp [fr]Localizable.strings ${basePath}/fr.lproj/Localizable.strings
#cp [id]Localizable.strings ${basePath}/id.lproj/Localizable.strings
#cp [ja]Localizable.strings ${basePath}/ja.lproj/Localizable.strings
#cp [ms]Localizable.strings ${basePath}/ms.lproj/Localizable.strings
#cp [pt]Localizable.strings ${basePath}/pt-PT.lproj/Localizable.strings
#cp [th]Localizable.strings ${basePath}/th.lproj/Localizable.strings
#cp [zh-Hant]Localizable.strings ${basePath}/zh-Hant.lproj/Localizable.strings
#cp [zh-Hans]Localizable.strings ${basePath}/zh-Hans.lproj/Localizable.strings

# ----------------------------------------------
# 利用規約
# ----------------------------------------------
tosFile="terms_of_service.html"

eula_file_copy ${tosFile}


#cp [ar]${tosFile} ${basePath}/ar.lproj/${tosFile}
#cp [en]${tosFile} ${basePath}/en.lproj/${tosFile}
#cp [es]${tosFile} ${basePath}/es.lproj/${tosFile}
#cp [fr]${tosFile} ${basePath}/fr.lproj/${tosFile}
#cp [id]${tosFile} ${basePath}/id.lproj/${tosFile}
#cp [ja]${tosFile} ${basePath}/ja.lproj/${tosFile}
#cp [ms]${tosFile} ${basePath}/ms.lproj/${tosFile}
#cp [pt]${tosFile} ${basePath}/pt-PT.lproj/${tosFile}
#cp [th]${tosFile} ${basePath}/th.lproj/${tosFile}
#cp [zh-Hant]${tosFile} ${basePath}/zh-Hant.lproj/${tosFile}
#cp [zh-Hans]${tosFile} ${basePath}/zh-Hans.lproj/${tosFile}

# ----------------------------------------------
# アプリ概要
# ----------------------------------------------
appoverviewFile="application_overview.html"

app_overview_file_copy ${appoverviewFile}

#cp [ar]${tosFile} ${basePath}/ar.lproj/${tosFile}
#cp [en]${tosFile} ${basePath}/en.lproj/${tosFile}
#cp [es]${tosFile} ${basePath}/es.lproj/${tosFile}
#cp [fr]${tosFile} ${basePath}/fr.lproj/${tosFile}
#cp [id]${tosFile} ${basePath}/id.lproj/${tosFile}
#cp [ja]${tosFile} ${basePath}/ja.lproj/${tosFile}
#cp [ms]${tosFile} ${basePath}/ms.lproj/${tosFile}
#cp [pt]${tosFile} ${basePath}/pt-PT.lproj/${tosFile}
#cp [th]${tosFile} ${basePath}/th.lproj/${tosFile}
#cp [zh-Hant]${tosFile} ${basePath}/zh-Hant.lproj/${tosFile}
#cp [zh-Hans]${tosFile} ${basePath}/zh-Hans.lproj/${tosFile}

echo "copy ended..."




