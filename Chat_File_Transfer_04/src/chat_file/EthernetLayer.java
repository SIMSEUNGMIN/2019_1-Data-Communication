package chat_file;

import java.util.ArrayList;

public class EthernetLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	public ChatAppLayer chat;
	
	private class _ETHERNET_ADDR {
		//enet_type는 chat의 경우 2080 file의 경우 2090
		private byte[] addr = new byte[6];
		
		public _ETHERNET_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
			this.addr[4] = (byte) 0x00;
			this.addr[5] = (byte) 0x00;
		}
	}
	
	private class _ETHERNET_Frame{
		_ETHERNET_ADDR enet_dstaddr;
		_ETHERNET_ADDR enet_srcaddr;
		byte[] enet_type;
		byte[] enet_data;
		
		public _ETHERNET_Frame() {
			this.enet_dstaddr = new _ETHERNET_ADDR();
			this.enet_srcaddr = new _ETHERNET_ADDR();
			this.enet_type = new byte[2];
			this.enet_data = null;
		}
	}

	_ETHERNET_Frame m_sHeader = new _ETHERNET_Frame();

	public EthernetLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}

	public void ResetHeader() {
		for (int i = 0; i < 6; i++) {
			m_sHeader.enet_dstaddr.addr[i] = (byte) 0x00;
			m_sHeader.enet_srcaddr.addr[i] = (byte) 0x00;
		}
		
		m_sHeader.enet_type[0] = 0x20;
		m_sHeader.enet_type[1] = 0x00;
		m_sHeader.enet_data = null;
	}
	
	//채팅 데이터를 보낼 때 사용하는 헤더
	public byte[] ObjToByteChat(_ETHERNET_Frame Header, byte[] input, int length) {
		byte[] buf = new byte[length + 14];

		buf[0] = m_sHeader.enet_dstaddr.addr[0];
		buf[1] = m_sHeader.enet_dstaddr.addr[1];
		buf[2] = m_sHeader.enet_dstaddr.addr[2];
		buf[3] = m_sHeader.enet_dstaddr.addr[3];
		buf[4] = m_sHeader.enet_dstaddr.addr[4];
		buf[5] = m_sHeader.enet_dstaddr.addr[5];
		buf[6] = m_sHeader.enet_srcaddr.addr[0];
		buf[7] = m_sHeader.enet_srcaddr.addr[1];
		buf[8] = m_sHeader.enet_srcaddr.addr[2];
		buf[9] = m_sHeader.enet_srcaddr.addr[3];
		buf[10] = m_sHeader.enet_srcaddr.addr[4];
		buf[11] = m_sHeader.enet_srcaddr.addr[5];
		buf[12] = m_sHeader.enet_type[0];
		buf[13] = (byte)0x80;
		
		for (int i = 0; i < length; i++) //데이터 저장
			buf[14 + i] = input[i];

		return buf;
	}
	
	//파일 데이터를 보낼 때 사용하는 헤더
	public byte[] ObjToByteFile(_ETHERNET_Frame Header, byte[] input, int length) { 
		byte[] buf = new byte[length + 14];

		buf[0] = m_sHeader.enet_dstaddr.addr[0];
		buf[1] = m_sHeader.enet_dstaddr.addr[1];
		buf[2] = m_sHeader.enet_dstaddr.addr[2];
		buf[3] = m_sHeader.enet_dstaddr.addr[3];
		buf[4] = m_sHeader.enet_dstaddr.addr[4];
		buf[5] = m_sHeader.enet_dstaddr.addr[5];
		buf[6] = m_sHeader.enet_srcaddr.addr[0];
		buf[7] = m_sHeader.enet_srcaddr.addr[1];
		buf[8] = m_sHeader.enet_srcaddr.addr[2];
		buf[9] = m_sHeader.enet_srcaddr.addr[3];
		buf[10] = m_sHeader.enet_srcaddr.addr[4];
		buf[11] = m_sHeader.enet_srcaddr.addr[5];
		buf[12] = m_sHeader.enet_type[0];
		buf[13] = (byte)0x90;
		
		for (int i = 0; i < length; i++) //데이터 저장
			buf[14 + i] = input[i];

		return buf;
	}
	
	//헤더를 붙여서 다른 컴퓨터로 데이터를 보냄(chat의 경우)
	public boolean chatSend(byte[] input, int length) {
		System.out.println("EthernetchatSend 들어감");
		byte[] data = ObjToByteChat(m_sHeader, input, length);
		this.GetUnderLayer().Send(data, data.length);
		return true;
	}
	
	//헤더를 붙여서 다른 컴퓨터로 데이터를 보냄(file의 경우)
	public boolean fileSend(byte[] input, int length) {
		System.out.println("Ethernet file send");
		byte[] data = ObjToByteFile(m_sHeader, input, length);
		this.GetUnderLayer().Send(data, data.length);
		return true;
	}
	
	//이더넷의 헤더를 뗀다. (앞의 14칸 삭제)
	public byte[] RemoveEHeader(byte[] input, int length) { 
		
		byte[] input2;
		input2 = new byte[length-14];
		System.arraycopy(input, 14, input2, 0, length-14);
		
		return input2;
		
	}
	
	//NILayer에서 받아온 데이터를 ChatApp 또는 FileApp로 보낸다.
	//먼저 내가 보낸 데이터인지 확인 -> 내가 보낸 데이터면 discard.
	//아닐 경우 1대 1통신인지 확인 또는 브로드캐스트인지 확인.
	//나 같은 경우에는 내가 보낸 데이터인지 확인하는 부분이랑 1대1통신, 브로드캐스트인지 확인하는 것을 묶어버림.
	
	public synchronized boolean Receive(byte[] input) {
		
		byte[] data;
		byte[] temp_src = m_sHeader.enet_srcaddr.addr;
		byte[] temp_dst = m_sHeader.enet_dstaddr.addr;
		
		boolean srcResult = false;
		boolean brdResult = false;
		
		for (int i = 0; i < 6; i++) {
			if ((input[i] != temp_src[i]) || (input[i+6] != temp_dst[i])) { 
				//받은 데이터의 목적지 주소가 내 주소와 다를 경우
				//내 목적지 주소가 받은 데이터의 주소와 다를 경
				srcResult = false;
				break;
			}
			else { //받은 데이터의 목적지 주소가 내 주소와 같은 경우
					srcResult = true;
			}
		}
		
		for (int i = 0; i < 6; i++) {
			if ((input[i] != (byte)0xff) || (input[i+6] == temp_src[i])) { 
				//input의 src와 나의 src가 같으면 내가 전송한 패킷이므로 받지 않는다.
				//브로드캐스트일 때 내가 전송한 브로드캐스트는 받지 않는다.
				brdResult = false;
				break;
			}
			else { //받은 데이터의 출발지 주소가 내 주소가 아니고 브로드캐스트인 경우
					brdResult = true;
			}
		}
		
		if(srcResult == true) { //받아오는 데이터가 맞는 경우
			//chat인지 file인지 확인하고 그에 따라 데이터를 올려준다.
			if(input[13] == (byte)0x80) {
				//chat데이터일 경우
				data = RemoveEHeader(input, input.length);
				this.GetUpperLayer(0).Receive(data); //0번 째가 ChatAppLayer
				return true;
			}
			else if(input[13] == (byte)0x90){
				//file데이터일 경우
				data = RemoveEHeader(input, input.length);
				this.GetUpperLayer(1).Receive(data); //1번 째가 FileAppLayer
				System.out.println("Ethernet File receive");
				return true;
			}
			else {
				return false;
			}
		}
		else if(brdResult == true) { //받아오는 데이터가 맞는 경우
			//chat인지 file인지 확인하고 그에 따라 데이터를 올려준다.
			if(input[13] == (byte)0x80) {
				//chat데이터일 경우
				data = RemoveEHeader(input, input.length);
				this.GetUpperLayer(0).Receive(data); //0번 째가 ChatAppLayer
				return true;
			}
			else {
				//file데이터일 경우
				data = RemoveEHeader(input, input.length);
				this.GetUpperLayer(1).Receive(data); //1번 째가 FileAppLayer
				return true;
			}
		}
		else {
			return false;
		}
		
	}

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		// TODO Auto-generated method stub
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
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
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
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);
	}

	public void SetEnetSrcAddress(byte[] srcAddress) {
		// TODO Auto-generated method stub
		m_sHeader.enet_srcaddr.addr = srcAddress;
	}

	public void SetEnetDstAddress(byte[] dstAddress) {
		// TODO Auto-generated method stub
		m_sHeader.enet_dstaddr.addr = dstAddress;
	}

}
