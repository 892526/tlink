package com.jvckenwood.aessampleapp

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AESによる暗号化,復号化を行うユーティリティクラス
 */
class AesUtility
{
    /**
     * クラスメソッド
     */
    companion object {


        /**
         * 暗号化設定
         * ---
         * ・アルゴリズム: AES
         * ・暗号化モード: CBC
         * ・パディング: PKCS7
         * ---
         */
        private val aesSetting: String = "AES/CBC/PKCS7Padding"

        /**
         * 暗号化アルゴリズム名
         */
        private val algorithmName: String = "AES"


        /**
         * 暗号化する。
         *
         * @args data 暗号化するデータ
         * @args passKey 鍵
         * @args initialVector 初期化ベクトル
         * @return 暗号化したデータ。暗号化できない場合は、nullを返すます。
         */
        fun encrypt(data: ByteArray, passKey: ByteArray, initialVector: ByteArray): ByteArray?  {
            try {
                val cipher = Cipher.getInstance(aesSetting)
                val secretKeySpec = SecretKeySpec(passKey, algorithmName)
                val finalIvs = ByteArray(16)
                val len = if (initialVector.size > 16) 16 else initialVector.size
                System.arraycopy(initialVector, 0, finalIvs, 0, len)
                val ivps = IvParameterSpec(finalIvs)
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivps)
                return cipher.doFinal(data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * 復号化する。
         *
         * @args data 復号化するデータ
         * @args passKey 鍵
         * @args initialVector 初期化ベクトル
         * @return 暗号化したデータ。複合化できない場合は、nullを返すます。
         */
        fun decrypt(data: ByteArray, passKey: ByteArray, initialVector: ByteArray): ByteArray? {
            try {
                val cipher = Cipher.getInstance(aesSetting)
                val secretKeySpec = SecretKeySpec(passKey, algorithmName)
                val finalIvs = ByteArray(16)
                val len = if (initialVector.size > 16) 16 else initialVector.size
                System.arraycopy(initialVector, 0, finalIvs, 0, len)
                val ivps = IvParameterSpec(finalIvs)
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivps)
                return cipher.doFinal(data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }

}