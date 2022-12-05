package Test;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class MqttPublisher_API{
	static MqttClient sampleClient;
		
    public static void main(String[] args) {
    	connectBroker();
    	
    	String pm_data = get_pm_data();
    	String[] weather_data  = get_weather_data();
    	
    	publish_data("tmp", "{\"tmp\": "+weather_data[0]+"}");
    	publish_data("humi", "{\"humi\": "+weather_data[1]+"}");
    	publish_data("pm", "{\"pm\": "+pm_data+"}");
    	
    	try {
			sampleClient.disconnect();
	        System.out.println("Disconnected");
	        System.exit(0);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void connectBroker() {
        String broker       = "tcp://127.0.0.1:1883";
        String clientId     = "practice";
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
    
    public static void publish_data(String topic_input, String data) {
        String topic        = topic_input;
        int qos             = 0;
        try {
            System.out.println("Publishing message: "+data);
            sampleClient.publish(topic, data.getBytes(), qos, false);
            System.out.println("Message published");
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
    
    public static String[] get_weather_data() {
    	Date current = new Date(System.currentTimeMillis());
    	SimpleDateFormat d_format = new SimpleDateFormat("yyyyMMddHHmmss"); 
    	//System.out.println(d_format.format(current));
    	String date = d_format.format(current).substring(0,8);
    	String time = d_format.format(current).substring(8,10);
    	String url = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst" // https가 아닌 http 프로토콜을 통해 접근해야 함.
    			+ "?serviceKey=X7VbxDZo%2F8scobmS5QUIF2h6s%2F2FVu4HbJ%2BSa2x31kXEuRx8j48OX79kZ4kGJ9F6jl7ef6Haq4SD2sK8t3Entw%3D%3D"
    			+ "&pageNo=1&numOfRows=1000"
    			+ "&dataType=XML"
    			+ "&base_date="+date
    			+ "&base_time="+time+"00"
    			+ "&nx=55"
    			+ "&ny=127";
    	
		String temp = "";
		String humi = "";
				
    	Document doc = null;
		
		// 2.파싱
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println(doc);
		
		Elements elements = doc.select("item");
		for (Element e : elements) {
			if (e.select("category").text().equals("T1H")) {
				temp = e.select("obsrValue").text();
			}
			if (e.select("category").text().equals("REH")) {
				humi = e.select("obsrValue").text();
			}
		}
		String[] out = {temp, humi};
    	return out;
    }
    
    
    public static String get_pm_data() {
    	String url = "http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/" // https가 아닌 http 프로토콜을 통해 접근해야 함.
    			+ "getCtprvnRltmMesureDnsty"
    			+ "?serviceKey=X7VbxDZo%2F8scobmS5QUIF2h6s%2F2FVu4HbJ%2BSa2x31kXEuRx8j48OX79kZ4kGJ9F6jl7ef6Haq4SD2sK8t3Entw%3D%3D"
    			+ "&returnType=xml"
    			+ "&numOfRows=100"
    			+ "&pageNo=1"
    			+ "&sidoName=%EA%B0%95%EC%9B%90"
    			+ "&ver=1.0"; //크롤링할 url지정
		String value = "";
    	Document doc = null;
		
		// 2.파싱
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println(doc);
		
		Elements elements = doc.select("item");
		for (Element e : elements) {
			if (e.select("stationName").text().equals("석사동")) {
				value = e.select("pm10Value").text();
			}
		}
    	return value;
    }
}


// written by Sangwoo Lee