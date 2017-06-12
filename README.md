# 🏺 Install Certs [![version][version-svg]][download] [![api-doc][doc-svg]][apidoc-url]

`InstallCerts` is a simple cli tool to create [PKCS12][pkcs-wiki] trustStore by retrieving server's TLS certificates.
You can achieve the same using [OpenSSL][openssl-wiki] and java [Keytool][keytool-doc] commands, but `InstallCerts` makes it fully automated using a single command.

### Download

* Binary

   [Download (v1.0.4)][download]

   > After download, make sure to set the execute permission (`chmod +x installcerts`). Windows users can run the executable jar.

* Source

    ```ruby
     $ git clone https://github.com/sureshg/InstallCerts
     $ cd InstallCerts
     $ ./gradlew -q
    ```
    > The binary would be located at `build/libs/installcerts`
    
    Inorder to build a new version, change `appVersion` in the [gradle.properties](gradle.properties) or pass it to `./gradlew -PappVersion=1.0.4`

* Github Release

    > In order to publish the binary to Github, generate [Github Access token][github-token] 
    
    ```ruby
     $ export GITHUB_TOKEN=<token>
     $ git clone https://github.com/sureshg/InstallCerts
     $ cd InstallCerts
     $ ./gradlew githubRelease -q
    ```
    
### Usage 

```ruby
$ installcerts -h
NAME
        installcerts - Creates PKCS12 TrustStore by retrieving server
        certificates

SYNOPSIS
        installcerts [(-a | --all)] [(-d | --debug)] [(-h | --help)]
                [(-p <storePasswd> | --passwd <storePasswd>)]
                [(-t <timeout> | --timeout <timeout>)] [(-v | --verbose)]
                [(-V | --version)] [--] <host>[:port]

OPTIONS
        -a, --all
            Show all certs and exits

        -d, --debug
            Enable TLS debug tracing

        -h, --help
            Display help information

        -p <storePasswd>, --passwd <storePasswd>
            Trust store password. Default is 'changeit'

        -t <timeout>, --timeout <timeout>
            TLS connect and read timeout (ms). Default is 5000 millis

        -v, --verbose
            Verbose mode

        -V, --version
            Show version

        --
            This option can be used to separate command-line options from the
            list of argument, (useful when arguments might be mistaken for
            command-line options

        <host>[:port]
            Server URL. Default port is 443
```

### Examples

  *  To list all TLS certificates (`-a`)
  
     ```ruby
     $ installcerts google.com -a

       Loading default ca truststore...
       Opening connection to google.com:443...
       
       Starting SSL handshake...
       
       1) Subject - CN=*.google.com, O=Google Inc, L=Mountain View, ST=California, C=US
         Issuer : CN=Google Internet Authority G2, O=Google Inc, C=US
         SHA1   : 5A B6 93 22 33 B7 58 4F D2 BA 42 FE 94 53 65 79 19 E9 7B BC
         MD5    : 16 1F 54 D8 3A E9 33 78 DE 68 72 4C 80 5C 98 C4
         SAN    : *.google.com
                  *.android.com
                  *.appengine.google.com
                  *.cloud.google.com
                  *.gcp.gvt2.com
                  *.google-analytics.com
                  *.googleadapis.com
                  *.googleapis.cn
                  *.url.google.com
                  *.youtube-nocookie.com
                  *.youtube.com
                  *.youtubeeducation.com
                  *.ytimg.com
                  android.clients.google.com
                  android.com
                  developer.android.google.cn
                  developers.android.google.cn
                  g.co
                  goo.gl
                  google-analytics.com
                  google.com
                  googlecommerce.com
                  source.android.google.cn
                  urchin.com
                  www.goo.gl
                  youtu.be
                  youtube.com
                  youtubeeducation.com
         Expiry : Fri Jul 14 01:25:00 PDT 2017
       
       2) Subject - CN=Google Internet Authority G2, O=Google Inc, C=US
         Issuer : CN=GeoTrust Global CA, O=GeoTrust Inc., C=US
         SHA1   : D6 AD 07 C6 67 56 30 F5 7B 92 7F 66 BE 8C E1 F7 68 F8 79 48
         MD5    : C5 6F 1A 63 B8 17 B7 31 89 34 C0 6E C5 AB B5 B3
         SAN    :
         Expiry : Sun Dec 31 15:59:59 PST 2017
       
       3) Subject - CN=GeoTrust Global CA, O=GeoTrust Inc., C=US
         Issuer : OU=Equifax Secure Certificate Authority, O=Equifax, C=US
         SHA1   : 73 59 75 5C 6D F9 A0 AB C3 06 0B CE 36 95 64 C8 EC 45 42 A3
         MD5    : 2E 7D B2 A3 1D 0E 3D A4 B2 5F 49 B9 54 2A 2E 1A
         SAN    :
         Expiry : Mon Aug 20 21:00:00 PDT 2018
       
       SSL-Session:
         Protocol    : TLSv1.2
         CipherSuite : TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
         Session-ID  : 68 3E AD 92 27 59 F6 C2 C5 BF 10 58 04 BF AC 6C 06 DF E9 74 05 A5 39 D2 0E 1F 97 4B 4F 03 81 64
         Timeout     : 86400
         Create Time : Mon Apr 24 11:10:04 PDT 2017
         Access Time : Mon Apr 24 11:10:04 PDT 2017
         Values      :

     ```
    
  * To create PKCS12 file
  
    ```ruby
        $ installcerts https://self-signed.badssl.com
    
          Loading default ca truststore...
          Opening connection to self-signed.badssl.com:443...
          
          Starting SSL handshake...
          Server sent 1 certificate(s)...
          
          1) Adding certificate to keystore using alias self-signed.badssl.com-1...
          Subject - CN=*.badssl.com, O=BadSSL, L=San Francisco, ST=California, C=US
            Issuer : CN=*.badssl.com, O=BadSSL, L=San Francisco, ST=California, C=US
            SHA1   : 64 14 50 D9 4A 65 FA EB 3B 63 10 28 D8 E8 6C 95 43 1D B8 11
            MD5    : 46 10 F4 1F 93 A3 EE 58 E0 CC 69 BE 1C 71 E0 C0
            SAN    : *.badssl.com
                     badssl.com
            Expiry : Wed Aug 08 14:17:05 PDT 2018
          
          Starting SSL handshake...
          Certificate is trusted. Saving the trustore...
          
          🍺  PKCS12 truststore saved to /Users/suresh/installcerts/self-signed_badssl_com.p12  
      
          To lists entries in the keystore, run
          keytool -list -keystore self-signed_badssl_com.p12 --storetype pkcs12
       ```
  
  * Debug TLS Session (`-d`)   

    ```ruby
        $ installcerts https://rsa2048.badssl.com/ -a -d
    
          ➤ Enabling TLS debug tracing...
          Loading default ca truststore...
          Opening connection to rsa2048.badssl.com:443...
          adding as trusted cert:
            Subject: CN=Equifax Secure Global eBusiness CA-1, O=Equifax Secure Inc., C=US
            Issuer:  CN=Equifax Secure Global eBusiness CA-1, O=Equifax Secure Inc., C=US
            Algorithm: RSA; Serial number: 0xc3517
            Valid from Sun Jun 20 21:00:00 PDT 1999 until Sun Jun 21 21:00:00 PDT 2020
          ...
          Extension signature_algorithms, signature_algorithms: SHA512withECDSA, SHA512withRSA, SHA384withECDSA, SHA384withRSA, SHA256withECDSA,...
          Extension server_name, server_name: [type=host_name (0), value=rsa2048.badssl.com]
          ***
          [write] MD5 and SHA1 hashes:  len = 194
          0000: 01 00 00 BE 03 03 58 FE   41 39 72 B5 AA 3D F4 04  ......X.A9r..=..
          0010: 9E 4B E2 C4 C3 D0 44 2E   6C A7 19 67 58 01 AC D0  .K....D.l..gX...
          0020: 40 C3 D8 6A B7 AD 00 00   3A C0 23 C0 27 00 3C C0  @..j....:.#.'.<.
          0030: 25 C0 29 00 67 00 40 C0   09 C0 13 00 2F C0 04 C0  %.).g.@...../...
          0040: 0E 00 33 00 32 C0 2B C0   2F 00 9C C0 2D C0 31 00  ..3.2.+./...-.1.
          ...
          
          main, SEND TLSv1.2 ALERT:  warning, description = close_notify
          Padded plaintext before ENCRYPTION:  len = 2
          0000: 01 00                                              ..
          main, WRITE: TLSv1.2 Alert, length = 26
          [Raw write]: length = 31
          0000: 15 03 03 00 1A 00 00 00   00 00 00 00 01 18 B9 59  ...............Y
          0010: 96 9B 04 93 CB 8A 4C EC   D8 B1 9B 0C 43 76 E3     ......L.....Cv.
          main, called closeSocket(true)
          ...
       ```    
         
  * Some useful Keytool commands
    
    ```ruby
    # List all certificates from the pkcs12 truststore.
    $ keytool -list -keystore self-signed_badssl_com.p12 --storetype pkcs12
      Enter keystore password: changeit
  
    # Extract certificate from pkcs12 truststore.
    $ keytool -exportcert -alias [host]-1 -keystore self-signed_badssl_com.p12 -storepass changeit -file [host].cer

    # Import certificate into system keystore
    $ keytool -importcert -alias [host] -keystore [path to system keystore] -storepass changeit -file [host].cer
    ```
    
    
## Credits

 - Got the original idea from this [oracle blog][installcert-blog] post.
 
----------
<sup>**</sup>Require [Java 8 or later][java-download]

[version-svg]: https://img.shields.io/badge/installcerts-1.0.4-green.svg?style=flat-square
[doc-svg]: https://img.shields.io/badge/apidoc-1.0.4-ff69b4.svg?style=flat-square
[apidoc-url]: https://sureshg.github.io/InstallCerts/
[download]: https://github.com/sureshg/InstallCerts/releases/download/1.0.4/installcerts
[java-download]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[github-token]: https://github.com/settings/tokens
[installcert-blog]: https://blogs.oracle.com/gc/entry/unable_to_find_valid_certification
[pkcs-wiki]: https://en.wikipedia.org/wiki/PKCS_12
[keytool-doc]: https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html
[openssl-wiki]: https://en.wikipedia.org/wiki/OpenSSL

