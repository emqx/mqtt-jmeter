# mqtt-jmeter
MQTT JMeter Plugin, it's used for testing MQTT protocol. The plugin was used for EMQ's performance benchmark test, and here is report link - https://github.com/emqtt/emq-docs-cn.
The plugin is developed and maintained by https://www.xmeter.net. XMeter is a professional performance testing service provider.

# Install instruction
The plugin is a starndard JMeter plugin. You can download the latest version of mqtt-jmeter from https://github.com/emqtt/mqtt-jmeter/releases, and then copy the downloaded JAR files into $JMETER_HOME/lib/ext folder. After restart the JMeter, then you can see the 3 samplers provided by this plugin.

We recommend to use JMeter 3.0 or above. 

## Build from source code

If you'd like to build binary by yourself, please clone the project and run 'mvn install'. Maven will download some JMeter depdency binary files, so the build elapsed time will up to your network status.

# How to use
The plugin includes 3 samplers: 
1) Connection sampler, which can be used for connection mock. For example, in a large scale system, there could have lots of backend connections with no data transimission except some hearbeat signal. The sampler can be used in this case.
2) Pub sampler, which can be used for publish message to MQTT server.
3) Sub sampler, which can be used for sub message from MQTT server.


## Connection sampler



## Pub sampler



## Sub sampler




 
