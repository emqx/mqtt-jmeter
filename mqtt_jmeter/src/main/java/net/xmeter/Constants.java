package net.xmeter;

public interface Constants {
	public static final String SERVER = "mqtt.server";
	public static final String MQTT_VERSION = "mqtt.version";
	public static final String PORT = "mqtt.port";
	public static final String CONN_TIMEOUT = "mqtt.conn_timeout";
	public static final String MQTT_CLIENT_NAME = "mqtt.client_name";

	public static final String PROTOCOL = "mqtt.protocol";
	public static final String WS_PATH = "mqtt.ws_path";
	public static final String DUAL_AUTH = "mqtt.dual_ssl_authentication";
	public static final String CERT_FILE_PATH1 = "mqtt.keystore_file_path";
	public static final String CERT_FILE_PATH2 = "mqtt.clientcert_file_path";
	public static final String KEY_FILE_PWD1 = "mqtt.keystore_password";
	public static final String KEY_FILE_PWD2 = "mqtt.clientcert_password";
	
	public static final String USER_NAME_AUTH = "mqtt.user_name";
	public static final String PASSWORD_AUTH = "mqtt.password";
	
	public static final String CONN_CLIENT_ID_PREFIX = "mqtt.client_id_prefix";
	public static final String CONN_CLIENT_ID_SUFFIX = "mqtt.client_id_suffix";
	
	public static final String CONN_KEEP_ALIVE = "mqtt.conn_keep_alive";
	public static final String CONN_ATTEMPT_MAX = "mqtt.conn_attempt_max";
	public static final String CONN_RECONN_ATTEMPT_MAX = "mqtt.reconn_attempt_max";
	
	public static final String CONN_CLEAN_SESSION = "mqtt.conn_clean_session";
	
	public static final String MESSAGE_TYPE = "mqtt.message_type";
	public static final String MESSAGE_FIX_LENGTH = "mqtt.message_type_fixed_length";
	public static final String MESSAGE_TO_BE_SENT = "mqtt.message_to_sent";
	
	public static final String TOPIC_NAME = "mqtt.topic_name";
	public static final String QOS_LEVEL = "mqtt.qos_level";
	public static final String ADD_TIMESTAMP = "mqtt.add_timestamp";
	public static final String RETAINED_MESSAGE = "mqtt.retained_message";
	
	public static final String SAMPLE_CONDITION_VALUE = "mqtt.sample_condition_value";
	public static final String SAMPLE_CONDITION = "mqtt.sample_condition";
	
	public static final String TIME_STAMP_SEP_FLAG = "ts_sep_flag";
	
	public static final String DEBUG_RESPONSE = "mqtt.debug_response";
	
	public static final int QOS_0 = 0;
	public static final int QOS_1 = 1;
	public static final int QOS_2 = 2;
	
	public static final String MESSAGE_TYPE_RANDOM_STR_WITH_FIX_LEN = "Random string with fixed length";
	public static final String MESSAGE_TYPE_HEX_STRING = "Hex string";
	public static final String MESSAGE_TYPE_STRING = "String";
	
	public static final String MQTT_VERSION_3_1_1 = "3.1.1";
	public static final String MQTT_VERSION_3_1 = "3.1";
	
	public static final String SAMPLE_ON_CONDITION_OPTION1 = "specified elapsed time (ms)";
	public static final String SAMPLE_ON_CONDITION_OPTION2 = "number of received messages";
	
	public static final int MAX_CLIENT_ID_LENGTH = 23;
	
	public static final String DEFAULT_SERVER = "127.0.0.1";
	public static final String DEFAULT_MQTT_VERSION = "3.1";
	public static final String DEFAULT_PORT = "1883";
	public static final String DEFAULT_CONN_TIME_OUT = "10";
	public static final String TCP_PROTOCOL = "TCP";
	public static final String SSL_PROTOCOL = "SSL";
	public static final String WS_PROTOCOL = "WS";
	public static final String WSS_PROTOCOL = "WSS";
	public static final String DEFAULT_PROTOCOL = TCP_PROTOCOL;
	public static final String FUSESOURCE_MQTT_CLIENT_NAME = "fusesource";
	public static final String HIVEMQ_MQTT_CLIENT_NAME = "hivemq";
//	public static final String DEFAULT_MQTT_CLIENT_NAME = FUSESOURCE_MQTT_CLIENT_NAME;
	public static final String DEFAULT_MQTT_CLIENT_NAME = HIVEMQ_MQTT_CLIENT_NAME;
	
	public static final String JMETER_VARIABLE_PREFIX = "${";
	
	public static final String DEFAULT_TOPIC_NAME = "test_topic";
	
	public static final String DEFAULT_CONN_PREFIX_FOR_CONN = "conn_";
	
	public static final String DEFAULT_CONN_KEEP_ALIVE = "300";
	public static final String DEFAULT_CONN_ATTEMPT_MAX = "0";
	public static final String DEFAULT_CONN_RECONN_ATTEMPT_MAX = "0";
	
	public static final String DEFAULT_SAMPLE_VALUE_COUNT = "1";
	public static final String DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_MILLI_SEC = "1000";
	
	public static final String DEFAULT_MESSAGE_FIX_LENGTH = "1024";
	
	public static final boolean DEFAULT_ADD_CLIENT_ID_SUFFIX = true;
	
	public static final int SUB_FAIL_PENALTY = 1000; // force to delay 1s if sub fails for whatever reason
	
	public static final boolean DEFAULT_SUBSCRIBE_WHEN_CONNECTED = false;
}
