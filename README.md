# 🏺 Install Certs [ ![version](https://img.shields.io/badge/installcerts-1.0.0-green.svg) ](https://github.com/sureshg/InstallCerts/releases/download/1.0.0/installcerts)

`InstallCerts` is a simple cli tool to create [PKCS12](https://en.wikipedia.org/wiki/PKCS_12) trustStore by retrieving server's TLS certificates.
You can achieve the same using [OpenSSL](https://en.wikipedia.org/wiki/OpenSSL) and java [Keytool](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html) commands, but `InstallCerts` makes it fully automated using a single command.

### Download

* Binary

   [Download (v1.0.0)](https://github.com/sureshg/InstallCerts/releases/download/1.0.0/installcerts)

   > After download, make sure to set the execute permission (`chmod +x installcerts`)
   > Windows users can run the executable jar.

* Source

    ```ruby
     $ git clone https://github.com/sureshg/InstallCerts
     $ cd InstallCerts
     $ ./gradlew
    ```
    > The binary would be located at `build/libs/installcerts`

### Usage

```ruby
$ installcerts -h
NAME
        installcerts - Creates PKCS12 TrustStore by retrieving server certificates

SYNOPSIS
        installcerts [(-a | --all)] [(-h | --help)]
                [(-p <storePasswd> | --passwd <storePasswd>)] [(-v | --verbose)]
                [(-V | --version)] [--] <host>[:port]

OPTIONS
        -a, --all
            Show all certs and exits.

        -h, --help
            Display help information

        -p <storePasswd>, --passwd <storePasswd>
            Trust store password. Default is 'changeit'

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

  *  To list all TLS certificates
  
     ```ruby
     $ installcerts walmart.com  -a
     Loading default ca truststore...
     Opening connection to walmart.com:443...
     
     Starting SSL handshake...
     
     1) Subject - CN=www.walmart.com, O="Wal-Mart Stores, Inc.", L=Bentonville, ST=Arkansas, C=US
       Issuer : CN=GlobalSign Organization Validation CA - SHA256 - G2, O=GlobalSign nv-sa, C=BE
       SHA1   : DF 3C BB 19 68 95 F7 9A BE 99 44 D1 0D 3A CA A5 C7 21 1A 90
       MD5    : CE 58 55 38 BE A5 A8 E4 FA 45 4C 5D 88 7B 98 04
       SAN    : [2, www.walmart.com, 2, walmart.com]
       Expiry : Fri Sep 07 23:10:43 PDT 2018
     
     2) Subject - CN=GlobalSign Organization Validation CA - SHA256 - G2, O=GlobalSign nv-sa, C=BE
       Issuer : CN=GlobalSign Root CA, OU=Root CA, O=GlobalSign nv-sa, C=BE
       SHA1   : 90 2E F2 DE EB 3C 5B 13 EA 4C 3D 51 93 62 93 09 E2 31 AE 55
       MD5    : D3 E8 70 6D 82 92 AC E4 DD EB F7 A8 BB BD 56 6B
       SAN    :
       Expiry : Tue Feb 20 02:00:00 PST 2024
     
     3) Subject - CN=GlobalSign Root CA, OU=Root CA, O=GlobalSign nv-sa, C=BE
       Issuer : CN=GlobalSign Root CA, OU=Root CA, O=GlobalSign nv-sa, C=BE
       SHA1   : B1 BC 96 8B D4 F4 9D 62 2A A8 9A 81 F2 15 01 52 A4 1D 82 9C
       MD5    : 3E 45 52 15 09 51 92 E1 B7 5D 37 9F B1 87 29 8A
       SAN    :
       Expiry : Fri Jan 28 04:00:00 PST 2028
  
     ```
    
  * To create PKCS12 file
  
    ```ruby
        $ installcerts https://self-signed.badssl.com/
        Loading default ca truststore...
        Opening connection to self-signed.badssl.com:443...
        
        Starting SSL handshake...
        Server sent 1 certificate(s)...
        
        1) Adding certificate to keystore using alias self-signed.badssl.com-1...
        Subject - CN=*.badssl.com, O=BadSSL, L=San Francisco, ST=California, C=US
          Issuer : CN=*.badssl.com, O=BadSSL, L=San Francisco, ST=California, C=US
          SHA1   : 64 14 50 D9 4A 65 FA EB 3B 63 10 28 D8 E8 6C 95 43 1D B8 11
          MD5    : 46 10 F4 1F 93 A3 EE 58 E0 CC 69 BE 1C 71 E0 C0
          SAN    : [2, *.badssl.com, 2, badssl.com]
          Expiry : Wed Aug 08 14:17:05 PDT 2018
        
        Starting SSL handshake...
        Certificate is trusted. Saving the trustore...
        
        🍺  PKCS12 truststore saved to installcerts/self-signed_badssl_com.p12    
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

 - Got the original idea from this [oracle blog](https://blogs.oracle.com/gc/entry/unable_to_find_valid_certification) post.
 
----------
<sup>**</sup>Require [Java 8 or later](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

