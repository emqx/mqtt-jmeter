# mqtt-jmeter
MQTT JMeter Plugin, it's used for testing MQTT protocol. The plugin was used for EMQ's performance benchmark test, and here is report link - https://github.com/emqtt/emq-docs-cn.
The plugin is developed and maintained by https://www.xmeter.net. XMeter is a professional performance testing service provider.

# Install instruction
The plugin is a standard JMeter plugin. You can download the latest version of mqtt-jmeter from https://github.com/emqtt/mqtt-jmeter/releases, and then copy the downloaded JAR files into $JMETER_HOME/lib/ext folder. After restart the JMeter, then you can see the 3 samplers provided by this plugin.

We recommend to use JMeter 3.0 or above. 

## Build from source code

If you'd like to build binary by yourself, please clone the project and run 'mvn install'. Maven will download some JMeter dependency binary files, so the build elapsed time will up to your network status.

# How to use
The plugin includes 3 samplers: 

1) Connection sampler, which can be used for connection mock. For example, in a large scale system, there could have lots of backend connections with no data transimission except some hearbeat signal. The sampler can be used in this case.

2) Pub sampler, which can be used for publish message to MQTT server.

3) Sub sampler, which can be used for sub message from MQTT server.

## Certification files for SSL/TLS connections
After deploying emqtt server, you get the following OOTB (out of the box) SSL/TLS certification files under <EMQTTD_HOME>/etc/certs directory:

1) cacert.pem : the self-signed CA certification 

2) cert.pem : certification for emqtt server

3) client-cert.pem : certfication for emqtt client in order to connect to server via SSL/TLS connection. In this jmeter plugin case, the client implies jmeter "virtual user" 

[Note:] The above server and client certifications are both issued by the self-signed CA. If you would like to use official certifications for your EMQTT deployment, please check out relevant document to configure it.

We will use the OOTB test certfications (as an example) to show you how to prepare the required certification files for this EMQTT JMeter plugin.

	```bash
	export PATH=$PATH:<YOUR_JDK_HOM>/bin

	keytool -import -alias cacert -keystore emqtt.jks -file cacert.pem -storepass <YOUR_PASSWORD> -trustcacerts -noprompt
	keytool -import -alias client -keystore emqtt.jks -file client-cert.pem -storepass <YOUR_PASSWORD>
	keytool -import -alias server -keystore emqtt.jks -file cert.pem -storepass <YOUR_PASSWORD>

	openssl pkcs12 -export -inkey client-key.pem -in client-cert.pem -out client.p12 -password pass:<YOUR_PASSWORD>
	```

#### Specify key store, client certfication and corresponding pass phrases in plugin sampler
![Specify key store, client certfication and corresponding pass phrases](https://github.com/emqtt/mqtt-jmeter/raw/master/screenshots/ssl_conn.png)


## Connection sampler



## Pub sampler



## Sub sampler




 
