#!/bin/sh

git rm -r Acknowledgements.txt
git rm -r Bearers
git rm -r doc
git rm -r include
git rm -r libs

# ライセンスファイルは削除しない
# git rm -r LicenseFile

git rm -r ReleaseNotes.txt
