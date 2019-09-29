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
	//��Ʈ ������ ���� �ʵ�
	public int dst_port;
	public int src_port;

	public SocketLayer(String pName) {
		// super(pName);
		pLayerName = pName;

	}
	
	//Socket -> ��� ��� ������
	
	//IP�� PORT�� ����
	//IP�� ��ǻ�͸� ã�� �� ���� �ּ�
	//PORT�� ��ǻ�� ���� ���μ����� ã�� �� ���� �ּ�
	
	//Ŭ���̾�Ʈ -> ������ ��û (Socket)
	//���� -> ���� ��û ���, ���� ��û�� ���� �׶����� ������ �ΰ� ���� ���� (ServerSocket)
	
	//IPCDlg setting ��ư�� ������ ��Ʈ ����
	public void setClientPort(int dstAddress) {
		this.dst_port = dstAddress; 

	}
	
	//IPCDlg setting ��ư�� ������ ��Ʈ ����
	public void setServerPort(int srcAddress) {
		this.src_port = srcAddress; 
	}
	
	//�����͸� ������ ���� -> ��û�� ���� �����̱� ������ Ŭ���̾�Ʈ
	//�����͸� send�ÿ��� ��θ� ����� �ϱ� ������ Ŭ���̾�Ʈsocket�� ����ؼ� ��� ����
	//�ּҿ� ���� ��θ� �� ������ �����͸� ����
	public boolean Send(byte[] input, int length) {
		//input�� ����Ʈ������ �����͸� �ɰ���, length�� input�� ����
		
		try (Socket client = new Socket()) {
			//socket�� �ּҸ� ����(���� ip�ּҿ� ������ PORT����) -> �ϳ��� �������� ����� ��
			InetSocketAddress ipep = new InetSocketAddress("127.0.0.1", dst_port); // ������Ʈ "127.0.0.1"�� ������ IP = ���� IP�� ����
			//�ش� �ּҷ� ���� (�������� ����)
			client.connect(ipep); //�ش� ip, port�� ���� ��û.
			//����� ������ ���ŷ.
			
			//����Ǹ� �ܺη� �����͸� �����ϱ� ���� ��Ʈ���� ���� ������ ����
			try (OutputStream sender = client.getOutputStream();) {
				sender.write(input, 0, length); //������ �����͸� sender�� ��� (������ ����)
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return true;
	}
	
	//Thread�� ����ؼ� ������ �����͸� ���� �� �ֵ��� ��
	//RECEIVE Thread�� �����ϰ� thread ����
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


class Receive_Thread implements Runnable { //������ �����ϰ� ������ �����ϴ� Thread
	//receive�� �޴� �����͸� ���� ����
	byte[] data;
	
	//�����͸� ������ ���� ���̾�� �ø��� ���� ����
	BaseLayer UpperLayer;
	//�ڱ� ip�ּҸ� ���� ������ �� ��� ���μ������� �����ؾ����� �����ϴ� �뵵�� serverPort
	int server_port;
	
	public Receive_Thread(BaseLayer m_UpperLayer, int src_port) {
		//�Ű� ������ Ŭ���� �ʱ�ȭ
		UpperLayer = m_UpperLayer;
		server_port = src_port;
	}

	@Override
	public void run() {
		//��� ���ư��� (���� ������ ���� �𸣱� ����)
		while (true)
			try (ServerSocket server = new ServerSocket()) { //ServerSocket ��ü ���� (�����͸� �޴� �� �����ε�..?)
				//Ư�� ip�θ� ������ �� ������ ����. ip = 127..., port = server_port 
				//(�ڽ��� ip�� Port�� ���缭 �;� ������ ����� �� �ֵ��� ��� ����)
				InetSocketAddress ipep = new InetSocketAddress("127.0.0.1", server_port); //ip�ּҿ� port��ȣ
				server.bind(ipep); //��� ����
				System.out.println("Initialize complate");

				//LISTEN ���, sever�� ���� ��û�� �� ������ ��ٸ��ٰ�(��� ���)
				//���� ��û�� ���� Socket�� ����
				Socket client = server.accept();
				//������ ������ �޾Ƶ��� ������ ���, ������ connection���.
				System.out.println("Connection");

				// ������ �޾ƿ���
				// �ڵ� close
				try (InputStream reciever = client.getInputStream();) { //����Ʈ ������ �����͸� �д´�.
					//Ŭ���̾�Ʈ�� OutputStream�� �ѷ����� �����͸� InputStream���� ������
					
					// byte ������
					data = new byte[1528]; // Ethernet Maxsize + Ethernet Headersize = 1528;
					reciever.read(data, 0, data.length); //��Ʈ���� �ִ� �����͸� �о��
					UpperLayer.Receive(data); //�������� ó���� ���� ���� ���̾�� �ø�

				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
	}

}
