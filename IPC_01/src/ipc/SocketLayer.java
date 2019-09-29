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
		this.dst_port = dstAddress; //  ������� IPCDlg setting ��ư�� ������ ��Ʈ ����

	}

	public void setServerPort(int srcAddress) {
		this.src_port = srcAddress; // ������� IPCDlg setting ��ư�� ������ ��Ʈ ����
	}

	public boolean Send(byte[] input, int length) {
		//Send�Լ��� ���ؼ� Ŭ���̾�Ʈ ����, Ŭ���̾�Ʈ�� �����͸� sender�� ����.
		//input�� ����Ʈ������ �����͸� �ɰ���, length�� input�� ����

		try (Socket client = new Socket()) { //Ŭ���̾�Ʈ ����
			// Ŭ���̾�Ʈ �ʱ�ȭ
			InetSocketAddress ipep = new InetSocketAddress("127.0.0.1", dst_port);// ������Ʈ "127.0.0.1"�� ������ IP = ���� IP�� ����. 
			// ����
			client.connect(ipep); //�ش� ip, port�� ���� ��û.
			//����� ������ ���ŷ.

			try (OutputStream sender = client.getOutputStream();) { //�ܺη� ������ ����
				sender.write(input, 0, length); //������ �����͸� sender�� �����͸� ���
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

class Receive_Thread implements Runnable { //������ �����ϰ� ������ �����ϴ� Thread
	//���� �����͸� ���� ?
	byte[] data;

	BaseLayer UpperLayer;
	int server_port;

	public Receive_Thread(BaseLayer m_UpperLayer, int src_port) {
		// ���⿡ ���߿� charAppLayor�����ؾ���. ?

		UpperLayer = m_UpperLayer;
		server_port = src_port;
	}

	@Override
	public void run() {
		while (true)
			try (ServerSocket server = new ServerSocket()) { //ServerSocket ��ü ����
				// ���� �ʱ�ȭ

				//Ư�� ip�θ� ������ �� ������ ����. ip = 127..., port = server_port
				InetSocketAddress ipep = new InetSocketAddress("127.0.0.1", server_port); //ip�ּҿ� port��ȣ
				server.bind(ipep); //������ ip�� ��Ʈ ���ε�
				System.out.println("Initialize complate");

				// LISTEN ���
				Socket client = server.accept();
				//������ ������ �޾Ƶ��� ������ ���, ������ connection���.
				System.out.println("Connection");

				// reciever ��Ʈ�� �޾ƿ���
				// �ڵ� close
				try (InputStream reciever = client.getInputStream();) {
					//����Ʈ ������ �����͸� �д´�.
					//Ŭ���̾�Ʈ�� �����͸� ������ ����, Ŭ���̾�Ʈ�� �����͸� �о��
					
					// Ŭ���̾�Ʈ�κ��� �޽��� �ޱ�
					// byte ������
					data = new byte[1528]; // Ethernet Maxsize + Ethernet Headersize;
					reciever.read(data, 0, data.length); //�޾ƿ� �����͸� ����.
					UpperLayer.Receive(data); //?

				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
	}

}
