package net.xmeter;

public interface Constants {
	String SERVER = "mqtt.server";
	String MQTT_VERSION = "mqtt.version";
	String PORT = "mqtt.port";
	String CONN_TIMEOUT = "mqtt.conn_timeout";
	String MQTT_CLIENT_NAME = "mqtt.client_name";
	String MQTT_CONN_NAME = "mqtt.connect_name";

	String PROTOCOL = "mqtt.protocol";
	String WS_PATH = "mqtt.ws_path";
	String DUAL_AUTH = "mqtt.dual_ssl_authentication";
	String CERT_FILE_PATH1 = "mqtt.keystore_file_path";
	String CERT_FILE_PATH2 = "mqtt.clientcert_file_path";
	String KEY_FILE_PWD1 = "mqtt.keystore_password";
	String KEY_FILE_PWD2 = "mqtt.clientcert_password";
	
	String USER_NAME_AUTH = "mqtt.user_name";
	String PASSWORD_AUTH = "mqtt.password";
	
	String CONN_CLIENT_ID_PREFIX = "mqtt.client_id_prefix";
	String CONN_CLIENT_ID_SUFFIX = "mqtt.client_id_suffix";
	
	String CONN_KEEP_ALIVE = "mqtt.conn_keep_alive";
	String CONN_ATTEMPT_MAX = "mqtt.conn_attempt_max";
	String CONN_RECONN_ATTEMPT_MAX = "mqtt.reconn_attempt_max";
	
	String CONN_CLEAN_SESSION = "mqtt.conn_clean_session";
	
	String MESSAGE_TYPE = "mqtt.message_type";
	String MESSAGE_FIX_LENGTH = "mqtt.message_type_fixed_length";
	String MESSAGE_TO_BE_SENT = "mqtt.message_to_sent";
	
	String TOPIC_NAME = "mqtt.topic_name";
	String QOS_LEVEL = "mqtt.qos_level";
	String ADD_TIMESTAMP = "mqtt.add_timestamp";
	String RETAINED_MESSAGE = "mqtt.retained_message";
	
	String SAMPLE_CONDITION_VALUE = "mqtt.sample_condition_value";
	String SAMPLE_CONDITION_VALUE_OPT = "mqtt.sample_condition_value_opt";
	String SAMPLE_CONDITION = "mqtt.sample_condition";
	
	String TIME_STAMP_SEP_FLAG = "ts_sep_flag";
	
	String DEBUG_RESPONSE = "mqtt.debug_response";
	
	int QOS_0 = 0;
	int QOS_1 = 1;
	int QOS_2 = 2;
	
	String MESSAGE_TYPE_RANDOM_STR_WITH_FIX_LEN = "Random string with fixed length";
	String MESSAGE_TYPE_HEX_STRING = "Hex string";
	String MESSAGE_TYPE_STRING = "String";
	
	String MQTT_VERSION_3_1_1 = "3.1.1";
	String MQTT_VERSION_3_1 = "3.1";
	
	String SAMPLE_ON_CONDITION_OPTION1 = "specified elapsed time (ms)";
	String SAMPLE_ON_CONDITION_OPTION2 = "number of received messages";
	
	int MAX_CLIENT_ID_LENGTH = 23;
	
	String DEFAULT_SERVER = "127.0.0.1";
	String DEFAULT_MQTT_VERSION = "3.1";
	String DEFAULT_PORT = "1883";
	String DEFAULT_CONN_TIME_OUT = "10";
	String DEFAULT_MQTT_CONN_NAME = "mqttconn";
	String TCP_PROTOCOL = "TCP";
	String SSL_PROTOCOL = "SSL";
	String WS_PROTOCOL = "WS";
	String WSS_PROTOCOL = "WSS";
	String DEFAULT_PROTOCOL = TCP_PROTOCOL;
	String FUSESOURCE_MQTT_CLIENT_NAME = "fusesource";
	String HIVEMQ_MQTT_CLIENT_NAME = "hivemq";
//	String DEFAULT_MQTT_CLIENT_NAME = FUSESOURCE_MQTT_CLIENT_NAME;
	String DEFAULT_MQTT_CLIENT_NAME = HIVEMQ_MQTT_CLIENT_NAME;
	
	String JMETER_VARIABLE_PREFIX = "${";
	
	String DEFAULT_TOPIC_NAME = "test_topic";
	
	String DEFAULT_CONN_PREFIX_FOR_CONN = "conn_";
	
	String DEFAULT_CONN_KEEP_ALIVE = "300";
	String DEFAULT_CONN_ATTEMPT_MAX = "0";
	String DEFAULT_CONN_RECONN_ATTEMPT_MAX = "0";
	
	String DEFAULT_SAMPLE_VALUE_COUNT = "1";
	String DEFAULT_SAMPLE_VALUE_COUNT_TIMEOUT = "5000";
	String DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_MILLI_SEC = "1000";
	
	String DEFAULT_MESSAGE_FIX_LENGTH = "1024";
	
	boolean DEFAULT_ADD_CLIENT_ID_SUFFIX = true;
	
	int SUB_FAIL_PENALTY = 1000; // force to delay 1s if sub fails for whatever reason
	
	boolean DEFAULT_SUBSCRIBE_WHEN_CONNECTED = false;
}
