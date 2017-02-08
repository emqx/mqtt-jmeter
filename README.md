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

If MQTT JMeter plugin is installed successfully, then open JMeter and below 3 MQTT samplers can be found under 'Sampler'.

![mqtt_jmeter_plugin](screenshots/mqtt_jmeter_plugin.png)


## Connection sampler

![conn_sampler](screenshots/conn_sampler.png)

### MQTT connection

This section includes basic connection settings.

**Server name or IP**: The server install with MQTT server, it can be either IP address or server name. The default value is 127.0.0.1.

**Port number**: The port that opens by MQTT server, the default value is 1883 for TCP protocol, and normally 8883 for SSL protocol.

**Timeout(s)**: The connection timeout seconds while connecting to MQTT server. The default is 10 seconds.

### MQTT Protocol

The sampler supports for 2 protocols, TCP and SSL. For the SSL protocol, it includes normal SSL and dual SSL authentication. 

If **'Dual SSL authentication'** is checked, please follow 'Certification files for SSL/TLS connections' in below to configure client SSL configuration.

![protocol_setting](screenshots/protocol_setting.png)

### User authentication

User can configure MQTT server with user name & password authentication, refer to [EMQ user name and password authentication guide](http://emqtt.com/docs/v2/guide.html#id3).

**User name**: If MQTT server configured with user name, then specify user name here.

**Password**: If MQTT server configured with password, then specify password here.

### Connection options

**ClientId prefix**: The client id prefix, the plugin will add generated uuid after the prefix to identify the client. Default value is 'conn_'.

**Keep alive(s)**: Ping packet send interval in seconds. Default value is 300, which means each connection sends a ping packet to MQTT server every 5 minutes.

**Connection keep time(s)**: The value is for setting the connection elapsed time after successfully established MQTT connection. The default value is 1800 seconds, which means that the connection will be alive within 30 minutes.

**Connect attampt max**: The maximum number of reconnect attempts before an error is reported back to the client on the first attempt by the client to connect to a server. Set to -1 to use unlimited attempts. Defaults to 0.

**Reconnect attampt max**:  The maximum number of reconnect attempts before an error is reported back to the client after a server connection had previously been established. Set to -1 to use unlimited attempts. Defaults to 0.

## Pub sampler
![pub_sampler](screenshots/pub_sampler.png)

For **MQTT connection**, **User authentication** and **Connection options** section settings, please refer to *Connection sampler* for more detailed information.

### Pub options

**QoS level**: The available QoS value, 0 is AT_MOST_ONCE, 1 is AT_LEAST_ONCE and 2 is EXACTLY_ONCE.

**Topic name**: The topic name that the message will send to.

**Add timestamp in payload**: Add timestamp in the payload or not. If the checkbox is enabled, then timestamp of running pub sampler will be added ahead of payload. Mostly it can be used together with **Sub sampler** to calculate message latency time.

### Payloads

**Message type**: 3 types of message can be used. 

1) String: The normal string that sent to MQTT server. It can also be a JMeter variable.

2) Hex string: The hex string that sent to MQTT server. The chars input must be [0-9] or [A-F] or [a-f]. The hex string will be converted to binary and send to server. It can also be a JMeter variable.

3) Random string with fixed length: Refer to below screenshot. If the option is selected, then it requires user to input 'Length'. The length means the auto generated string length. Default is 1024, which means generated a 1kb size of random string.

![payload_setting](screenshots/payload_setting.png)

## Sub sampler
![sub_sampler](screenshots/sub_sampler.png)

For **MQTT connection**, **User authentication** and **Connection options** section settings, please refer to *Connection sampler* for more detailed information.

### Sub options

**QoS level**: The available QoS value, 0 is AT_MOST_ONCE, 1 is AT_LEAST_ONCE and 2 is EXACTLY_ONCE.

**Topic name**: The topic name that the message will subscribe to.

**Payload includes timestamp**: If the checkbox is enabled, then it means the payload includes timestamp. It can be used to calcuate the message latency time. 

message_latency = timestamp_in_sub_when_receive_msg - timestamp_in_payload (timestamp in pub machine when sending out message)

*Please notice, if the machine publish message is not the same as subscriber, then the calculated message latency time is not accurate. It's because the time is almost not the same in different machines. So the latency time calculated by sub sampler could be only be a reference.*

**Debug response**: If it's checked, then the received message will be print in response. It's recommend to enable it when you're debugging script. 



## Certification files for SSL/TLS connections
After deploying emqtt server, you get the following OOTB (out of the box) SSL/TLS certification files under ${EMQTTD_HOME}/etc/certs directory:

1) cacert.pem : the self-signed CA certification 

2) cert.pem : certification for emqtt server

3) client-cert.pem : certfication for emqtt client in order to connect to server via SSL/TLS connection. In this jmeter plugin case, the client implies jmeter "virtual user" 

[Note:] The above server and client certifications are both issued by the self-signed CA. If you would like to use official certifications for your EMQTT deployment, please check out relevant document to configure it.

We will use the OOTB test certfications (as an example) to show you how to prepare the required certification files for this EMQTT JMeter plugin.


	```

	export PATH=$PATH:<YOUR_JDK_HOM>/bin

	keytool -import -alias cacert -keystore emqtt.jks -file cacert.pem -storepass <YOUR_PASSWORD> -trustcacerts -noprompt
	keytool -import -alias client -keystore emqtt.jks -file client-cert.pem -storepass <YOUR_PASSWORD>
	keytool -import -alias server -keystore emqtt.jks -file cert.pem -storepass <YOUR_PASSWORD>

	openssl pkcs12 -export -inkey client-key.pem -in client-cert.pem -out client.p12 -password pass:<YOUR_PASSWORD>

	```


#### Specify key store, client certfication and corresponding pass phrases in plugin sampler:

![ssl_conn](screenshots/ssl_conn.png)
