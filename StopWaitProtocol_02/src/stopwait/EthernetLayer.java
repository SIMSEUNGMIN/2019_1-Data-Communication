package stopwait;

import java.util.ArrayList;

public class EthernetLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

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
	
	public byte[] ObjToByte(_ETHERNET_Frame Header, byte[] input, int length) {
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
		buf[13] = m_sHeader.enet_type[1];
		
		for (int i = 0; i < length; i++) //데이터 저장
			buf[14 + i] = input[i];

		return buf;
	}

	public boolean Send(byte[] input, int length) { //보내는 데이터
		byte[] data = ObjToByte(m_sHeader, input, length);
		this.GetUnderLayer().Send(data, data.length);
		return true;
	}
	
	public byte[] RemoveCappHeader(byte[] input, int length) {
		//앞의 14칸 삭제
		byte[] input2 = new byte[length-14];
		System.arraycopy(input, 14, input2, 0, length-14);
		
		return input2;
	}
	
	public synchronized boolean Receive(byte[] input) {
		byte[] data;
		byte[] temp_src = m_sHeader.enet_srcaddr.addr;
		byte[] temp_dst = m_sHeader.enet_dstaddr.addr;
		
		boolean srcResult = false;
		boolean brdResult = false;
		
		for (int i = 0; i < 6; i++) {
			if ((input[i] != temp_src[i]) || (input[i+6] != temp_dst[i])) {
				srcResult = false;
				break;
			}
			else {
				if((input[12] == 0x08) && (input[13] == 0x06)) {
					srcResult = true;
				}
				else srcResult = false;
			}
		}
		
		for (int i = 0; i < 6; i++) {
			if (input[i] != (byte)0xff) {
				brdResult = false;
				break;
			}
			else {
				if((input[12] == 0x08) && (input[13] == 0x06)) {
					brdResult = true;
				}
				else brdResult = false;
			}
		}
		
		if(srcResult == true) {
			data = RemoveCappHeader(input, input.length); 
			this.GetUpperLayer(0).Receive(data);
			return true;
			
		}
		else if(brdResult == true) {
			data = RemoveCappHeader(input, input.length);
			this.GetUpperLayer(0).Receive(data);
			return true;
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
