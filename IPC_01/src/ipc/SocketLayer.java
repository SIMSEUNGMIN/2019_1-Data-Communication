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
	//포트 설정용 변수 필드
	public int dst_port;
	public int src_port;

	public SocketLayer(String pName) {
		// super(pName);
		pLayerName = pName;

	}
	
	//Socket -> 어떠한 통신 접속점
	
	//IP와 PORT를 설정
	//IP는 컴퓨터를 찾을 때 쓰는 주소
	//PORT는 컴퓨터 내의 프로세스를 찾을 때 쓰는 주소
	
	//클라이언트 -> 연결을 요청 (Socket)
	//서버 -> 연결 요청 대기, 연결 요청이 오면 그때서야 연결을 맺고 소켓 생성 (ServerSocket)
	
	//IPCDlg setting 버튼을 누르면 포트 설정
	public void setClientPort(int dstAddress) {
		this.dst_port = dstAddress; 

	}
	
	//IPCDlg setting 버튼을 누르면 포트 설정
	public void setServerPort(int srcAddress) {
		this.src_port = srcAddress; 
	}
	
	//데이터를 보내는 입장 -> 요청을 위한 입장이기 때문에 클라이언트
	//데이터를 send시에는 통로를 열어야 하기 때문에 클라이언트socket을 사용해서 통로 개방
	//주소에 맞춰 통로를 연 다음에 데이터를 전송
	public boolean Send(byte[] input, int length) {
		//input은 바이트단위로 데이터를 쪼갠것, length는 input의 길이
		
		try (Socket client = new Socket()) {
			//socket의 주소를 설정(상대방 ip주소와 상대방의 PORT설정) -> 하나의 접속점을 만드는 것
			InetSocketAddress ipep = new InetSocketAddress("127.0.0.1", dst_port); // 상대방포트 "127.0.0.1"은 루프백 IP = 본인 IP를 뜻함
			//해당 주소로 접속 (접속점을 만듦)
			client.connect(ipep); //해당 ip, port로 연결 요청.
			//연결될 때까지 블로킹.
			
			//연결되면 외부로 데이터를 전송하기 위한 스트림을 열고 데이터 전송
			try (OutputStream sender = client.getOutputStream();) {
				sender.write(input, 0, length); //보내는 데이터를 sender에 기록 (데이터 전송)
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return true;
	}
	
	//Thread를 사용해서 언제든 데이터를 받을 수 있도록 함
	//RECEIVE Thread를 생성하고 thread 실행
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
	//receive시 받는 데이터를 위한 변수
	byte[] data;
	
	//데이터를 받으면 상위 레이어로 올리기 위한 변수
	BaseLayer UpperLayer;
	//자기 ip주소를 통해 들어왔을 때 어느 프로세스에게 전달해야할지 결정하는 용도의 serverPort
	int server_port;
	
	public Receive_Thread(BaseLayer m_UpperLayer, int src_port) {
		//매개 변수로 클래스 초기화
		UpperLayer = m_UpperLayer;
		server_port = src_port;
	}

	@Override
	public void run() {
		//계속 돌아간다 (언제 수신이 올지 모르기 때문)
		while (true)
			try (ServerSocket server = new ServerSocket()) { //ServerSocket 객체 생성 (데이터를 받는 건 서버인듯..?)
				//특정 ip로만 접속할 때 연결을 수락. ip = 127..., port = server_port 
				//(자신의 ip와 Port에 맞춰서 와야 수신을 허락할 수 있도록 통로 설정)
				InetSocketAddress ipep = new InetSocketAddress("127.0.0.1", server_port); //ip주소와 port번호
				server.bind(ipep); //통로 연결
				System.out.println("Initialize complate");

				//LISTEN 대기, sever에 연결 요청이 올 때까지 기다리다가(계속 대기)
				//연결 요청이 오면 Socket을 생성
				Socket client = server.accept();
				//서버가 연결을 받아들일 때까지 대기, 받으면 connection띄움.
				System.out.println("Connection");

				// 데이터 받아오기
				// 자동 close
				try (InputStream reciever = client.getInputStream();) { //바이트 단위로 데이터를 읽는다.
					//클라이언트가 OutputStream에 뿌려놓은 데이터를 InputStream으로 가져옴
					
					// byte 데이터
					data = new byte[1528]; // Ethernet Maxsize + Ethernet Headersize = 1528;
					reciever.read(data, 0, data.length); //스트림에 있는 데이터를 읽어옴
					UpperLayer.Receive(data); //데이터의 처리를 위해 상위 레이어로 올림

				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
	}

}
