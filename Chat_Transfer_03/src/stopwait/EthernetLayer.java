package stopwait;

import java.util.ArrayList;

public class EthernetLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	public ChatAppLayer chat;
	
	private class _ETHERNET_ADDR {
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
		
		m_sHeader.enet_type[0] = 0x08;
		m_sHeader.enet_type[1] = 0x06;
		m_sHeader.enet_data = null;
	}
	
	public byte[] ObjToByte(_ETHERNET_Frame Header, byte[] input, int length) { //데이터만을 보낼 때 사용하는 헤더
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
		buf[13] = (byte)0x01;
		
		for (int i = 0; i < length; i++) //데이터 저장
			buf[14 + i] = input[i];

		return buf;
	}

	public boolean Send(byte[] input, int length) { // 헤더를 붙여서 다른 컴퓨터로 데이터를 보냄
		byte[] data = ObjToByte(m_sHeader, input, length);
		this.GetUnderLayer().Send(data, data.length);
		return true;
	}
	
	int eByte2ToInt(byte one0, byte two1) {
		   int number = one0 | (two1 << 8);
		   return number;
	   }
	
	public byte[] RemoveCappHeader(byte[] input, int length) {
		//데이터 크기 + 이더넷헤더 + 챗앱 헤더의 크기랑 현재 받아온 길이랑 같으면 처리 X
		//아니면 뒤에 쓰레기가 들어온 것이므로 처리 해야 한다.
		//상대방이랑 통신할 때는 이더넷으로 들어올 때 60byte로 뒤에 쓰레기 값이 들어온다.
		//뒤의 값도 같이 떼어줘야 한다.
		//이를 위해서는 byte2ToInt를 사용하여 데이터 크기를 결정해줘야 한다.
		//앞의 14칸 삭제(이더넷 헤더를 뗀다)
		
		int dataLength = eByte2ToInt(input[14], input[15]);
		System.out.println("원래 데이터 : " + length);
		System.out.println("정리 데이터 : " + dataLength);
		byte[] input2;
		
		if(length <= 28) {
			// 한 번에 보낼 수 있는 데이터의 전체 크기는 28
			// 28이랑 같거나 작은 경우는 데이터에 쓰레기가 들어오지 않음
			input2 = new byte[length-14];
			System.arraycopy(input, 14, input2, 0, length-14);
		}
		else { //넘을 경우에는 데이터에 뒷부분에 쓰레기데이터가 들어옴
			//이때 데이터에서 챗앱헤더가 01일 경우에는 데이터의 길이가 10이상 들어온다.(전체 데이터를 나타내기 때문)
			//이때 데이터에서 챗앱헤더가 03일 경우에는 데이터의 길이가 10이랑 같거나 작다.
			if(dataLength <= 10) { 
				//03헤더의 경우 데이터의 길이가 10이랑 같거나 작으므로 복사해주는 길이도 줄여야한다.
				length = dataLength + 14 + 4;
				input2 = new byte[length-14];
				System.arraycopy(input, 14, input2, 0, length-14);	
			}
			else {
				length = 10 + 14 + 4; //잘라야할 길이를 정확히 지정.
				input2 = new byte[14];
				System.arraycopy(input, 14, input2, 0, 14);
			}
		}
		return input2;
	}
	
	public synchronized boolean Receive(byte[] input) {
		byte[] data;
		byte[] temp_src = m_sHeader.enet_srcaddr.addr;
		byte[] temp_dst = m_sHeader.enet_dstaddr.addr;
		
		boolean srcResult = false;
		boolean brdResult = false;
		boolean ackResult = false;
		
		for (int i = 0; i < 6; i++) {
			if ((input[i] != temp_src[i]) || (input[i+6] != temp_dst[i])) { 
				//받은 데이터의 목적지 주소가 내 주소와 다를 경우
				//내 목적지 주소가 받은 데이터의 주소와 다를 경우
				srcResult = false;
				break;
			}
			else { //받은 데이터의 목적지 주소가 내 주소와 같은 경우
				if((input[12] == 0x08) && (input[13] == 0x01)) { 
					//이때 데이터가 받는 거면 (input[13] == 0x01인 경우)
					srcResult = true;
				}
				else if((input[12] == 0x08) && (input[13] == 0x02)) {
					ackResult = true;
				}
				else srcResult = false;
			}
		}
		
		for (int i = 0; i < 6; i++) {
			if (input[i] != (byte)0xff) { //브로드캐스트일 경우
				brdResult = false;
				break;
			}
			else {
				if((input[12] == 0x08) && (input[13] == 0x01)) {
					//이때 데이터가 받는 거면 (input[13] == 0x01인 경우)
					brdResult = true;
				}
				else if((input[12] == 0x08) && (input[13] == 0x02)) {
					ackResult = true;
				}
				else brdResult = false;
			}
		}
		
		if(srcResult == true) { //받아오는 데이터가 맞는 경우
			//ACK를 보낸다.
			this.GetUnderLayer().Send(makeAck(), makeAck().length);
			System.out.println("받기 : " + input.length);
			data = RemoveCappHeader(input, input.length); 
			this.GetUpperLayer(0).Receive(data);
			return true;	
		}
		else if(brdResult == true) { //받아오는 데이터가 맞는 경우
			//ACK를 보낸다.
			this.GetUnderLayer().Send(makeAck(), makeAck().length);
			System.out.println("받기 브로드: " + input.length);
			data = RemoveCappHeader(input, input.length);
			this.GetUpperLayer(0).Receive(data);
			return true;
		}
		else if(ackResult == true) {
			chat.ACK = true;
			return true;
		}
		else {
			return false;
		}
	}
	
	public byte[] makeAck() {
		byte[] buf = new byte[14];

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
		buf[13] = (byte)0x02;
		
		return buf;
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
