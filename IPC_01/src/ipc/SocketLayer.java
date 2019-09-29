package ipc;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class SocketLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	public int dst_port;
	public int src_port;

	public SocketLayer(String pName) {
		// super(pName);
		pLayerName = pName;

	}

	public void setClientPort(int dstAddress) {
		this.dst_port = dstAddress; //  참고사항 IPCDlg setting 버튼을 누르면 포트 설정

	}

	public void setServerPort(int srcAddress) {
		this.src_port = srcAddress; // 참고사항 IPCDlg setting 버튼을 누르면 포트 설정
	}

	public boolean Send(byte[] input, int length) {
		//Send함수를 통해서 클라이언트 생성, 클라이언트의 데이터를 sender에 저장.
		//input은 바이트단위로 데이터를 쪼갠것, length는 input의 길이

		try (Socket client = new Socket()) { //클라이언트 생성
			// 클라이언트 초기화
			InetSocketAddress ipep = new InetSocketAddress("127.0.0.1", dst_port);// 상대방포트 "127.0.0.1"은 루프백 IP = 본인 IP를 뜻함. 
			// 접속
			client.connect(ipep); //해당 ip, port로 연결 요청.
			//연결될 때까지 블로킹.

			try (OutputStream sender = client.getOutputStream();) { //외부로 데이터 전송
				sender.write(input, 0, length); //보내는 데이터를 sender에 데이터를 기록
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return true;
	}

	public boolean Receive() {
		Receive_Thread thread = new Receive_Thread(this.GetUpperLayer(0), src_port);
		Thread obj = new Thread(thread);
		obj.start();

		return false;
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
		// nUpperLayerCount++;
	}

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		// TODO Auto-generated method stub
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);
	}

}

class Receive_Thread implements Runnable { //서버를 생성하고 연결을 수락하는 Thread
	//받을 데이터를 설정 ?
	byte[] data;

	BaseLayer UpperLayer;
	int server_port;

	public Receive_Thread(BaseLayer m_UpperLayer, int src_port) {
		// 여기에 나중에 charAppLayor연결해야함. ?

		UpperLayer = m_UpperLayer;
		server_port = src_port;
	}

	@Override
	public void run() {
		while (true)
			try (ServerSocket server = new ServerSocket()) { //ServerSocket 객체 생성
				// 서버 초기화

				//특정 ip로만 접속할 때 연결을 수락. ip = 127..., port = server_port
				InetSocketAddress ipep = new InetSocketAddress("127.0.0.1", server_port); //ip주소와 port번호
				server.bind(ipep); //서버에 ip랑 포트 바인딩
				System.out.println("Initialize complate");

				// LISTEN 대기
				Socket client = server.accept();
				//서버가 연결을 받아들일 때까지 대기, 받으면 connection띄움.
				System.out.println("Connection");

				// reciever 스트림 받아오기
				// 자동 close
				try (InputStream reciever = client.getInputStream();) {
					//바이트 단위로 데이터를 읽는다.
					//클라이언트는 데이터를 가지고 있음, 클라이언트의 데이터를 읽어옴
					
					// 클라이언트로부터 메시지 받기
					// byte 데이터
					data = new byte[1528]; // Ethernet Maxsize + Ethernet Headersize;
					reciever.read(data, 0, data.length); //받아온 데이터를 읽음.
					UpperLayer.Receive(data); //?

				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
	}

}
