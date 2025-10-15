import java.util.HashMap;
import java.util.Map;
import java.mail.Message;
import net.sf.json.JSONArray; 
import net.sf.json.JSONObject; 
import net.sf.json.JsonConfig; 
import net.sf.json.util.PropertyFilter; 
import java.net.*;

public class p2pService {
	//map存储每个注册用户的名字和公网地址
	Map<String,String>map =new HashMap<String,String>();
	byte[] inBuff=new byte[2048];
	DatagramPacket inpacket=new DatagramPacket(inBuff,2048);
	public void init() {
		try {
			System.out.println("服务器启动");
	DatagramSocket socket=new DatagramSocket(30000);
	while(true) {
		socket.receive(inpacket);
		System.out.println("地址："+inpacket.getSocketAddress()+" 端口："+inpacket.getPort()+" 消息："+new String(inBuff,0,inpacket.getLength()));
		JSONObject json=JSONObject.fromObject(new String(inBuff,0,inpacket.getLength()));
		//message封装通信消息格式
	    message mess=(message)JSONObject.toBean(json, message.class);
	    //link类消息为注册消息
		if("link".equals(mess.type))
		{
			map.put(mess.name, inpacket.getSocketAddress()+"");
			List<Map<String,String>>list=new ArrayList<>();
			for(String key:map.keySet())
			{
				Map<String,String> map1=new HashMap<>();
				map1.put("name", key);
				map1.put("IP", map.get(key));
				list.add(map1);
			}
			String messs=JSONArray.fromObject(list)+"";
			message mes=new message();
			mes.type="list";
			mes.value=messs;
			messs=JSONObject.fromObject(mes)+"";
			System.out.println(messs);
			byte[] outmess=messs.getBytes("utf-8");
			DatagramPacket out=new DatagramPacket(outmess,outmess.length,inpacket.getSocketAddress());
			//返回所有以注册用户的姓名
		socket.send(out);
		}
		//comm是需要获取通信对方的ip
		else if("comm".equals(mess.type)) {
		//B的IP
			String IPB=map.get(mess.name);
			//A的IP
			String IPA=inpacket.getSocketAddress()+"";
			message result=new message();
			result.type="link";
			result.IP=IPB.substring(1,IPB.lastIndexOf(":"));
			System.out.println("comm:"+result.IP);
			result.port=IPB.split(":")[8];
			System.out.println("link:"+result.IP+" port"+result.port);
			byte[] buff=JSONObject.fromObject(result).toString().getBytes();
		DatagramPacket out=new DatagramPacket(buff,buff.length,inpacket.getSocketAddress());
		
		//返回B的IP给A
			socket.send(out);
			result.IP=IPA.substring(1,IPB.lastIndexOf(":"));
			result.port=IPA.split(":")[8];
			
			 buff=JSONObject.fromObject(result).toString().getBytes();
			 out=new DatagramPacket(buff,buff.length,InetAddress.getByName(IPB.substring(2,IPB.lastIndexOf(":")-1)),Integer.parseInt(IPB.split(":")[8]));
			 //返回A的IP给B
			 socket.send(out);
			 
		}
		
		
	}
	
		}
		catch(Exception ex) {
			
			ex.printStackTrace();
		}
		
	}

	
	public static void main(String[] args) {
		p2pService service=new p2pService();
		
		service.init();
		
	}
}
