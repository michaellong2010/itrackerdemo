1.  �ͦ�my-release-key.keystore
c:\keytool\keytool -genkey -v -alias knight2001 -keystore my-release-key.keystore -keyalg RSA -keysize 2048 -validity 10000

2.  �qeclipse�ץX�|��ñ��ñ����*.apk�A"Android Tools"-->"Export unsigned Application package"
c:\keytool\jarsigner -verbose -sigalg MD5withRSA -digestalg SHA1 -keystore my-release-key.keystore -signedjar <output_signed.apk> <input_unsigned.apk> knight2001

3.  �Nsigned apk�i��word alignment
c:\keytool\zipalign -v 4 <signed_unalign.apk> <signed_aligned.apk>