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
	
	public byte[] ObjToByte(_ETHERNET_Frame Header, byte[] input, int length) { //�����͸��� ���� �� ����ϴ� ���
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
		
		for (int i = 0; i < length; i++) //������ ����
			buf[14 + i] = input[i];

		return buf;
	}

	public boolean Send(byte[] input, int length) { // ����� �ٿ��� �ٸ� ��ǻ�ͷ� �����͸� ����
		byte[] data = ObjToByte(m_sHeader, input, length);
		this.GetUnderLayer().Send(data, data.length);
		return true;
	}
	
	int eByte2ToInt(byte one0, byte two1) {
		   int number = one0 | (two1 << 8);
		   return number;
	   }
	
	public byte[] RemoveCappHeader(byte[] input, int length) {
		//������ ũ�� + �̴������ + ê�� ����� ũ��� ���� �޾ƿ� ���̶� ������ ó�� X
		//�ƴϸ� �ڿ� �����Ⱑ ���� ���̹Ƿ� ó�� �ؾ� �Ѵ�.
		//�����̶� ����� ���� �̴������� ���� �� 60byte�� �ڿ� ������ ���� ���´�.
		//���� ���� ���� ������� �Ѵ�.
		//�̸� ���ؼ��� byte2ToInt�� ����Ͽ� ������ ũ�⸦ ��������� �Ѵ�.
		//���� 14ĭ ����(�̴��� ����� ����)
		
		int dataLength = eByte2ToInt(input[14], input[15]);
		System.out.println("���� ������ : " + length);
		System.out.println("���� ������ : " + dataLength);
		byte[] input2;
		
		if(length <= 28) {
			// �� ���� ���� �� �ִ� �������� ��ü ũ��� 28
			// 28�̶� ���ų� ���� ���� �����Ϳ� �����Ⱑ ������ ����
			input2 = new byte[length-14];
			System.arraycopy(input, 14, input2, 0, length-14);
		}
		else { //���� ��쿡�� �����Ϳ� �޺κп� �����ⵥ���Ͱ� ����
			//�̶� �����Ϳ��� ê������� 01�� ��쿡�� �������� ���̰� 10�̻� ���´�.(��ü �����͸� ��Ÿ���� ����)
			//�̶� �����Ϳ��� ê������� 03�� ��쿡�� �������� ���̰� 10�̶� ���ų� �۴�.
			if(dataLength <= 10) { 
				//03����� ��� �������� ���̰� 10�̶� ���ų� �����Ƿ� �������ִ� ���̵� �ٿ����Ѵ�.
				length = dataLength + 14 + 4;
				input2 = new byte[length-14];
				System.arraycopy(input, 14, input2, 0, length-14);	
			}
			else {
				length = 10 + 14 + 4; //�߶���� ���̸� ��Ȯ�� ����.
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
				//���� �������� ������ �ּҰ� �� �ּҿ� �ٸ� ���
				//�� ������ �ּҰ� ���� �������� �ּҿ� �ٸ� ���
				srcResult = false;
				break;
			}
			else { //���� �������� ������ �ּҰ� �� �ּҿ� ���� ���
				if((input[12] == 0x08) && (input[13] == 0x01)) { 
					//�̶� �����Ͱ� �޴� �Ÿ� (input[13] == 0x01�� ���)
					srcResult = true;
				}
				else if((input[12] == 0x08) && (input[13] == 0x02)) {
					ackResult = true;
				}
				else srcResult = false;
			}
		}
		
		for (int i = 0; i < 6; i++) {
			if (input[i] != (byte)0xff) { //��ε�ĳ��Ʈ�� ���
				brdResult = false;
				break;
			}
			else {
				if((input[12] == 0x08) && (input[13] == 0x01)) {
					//�̶� �����Ͱ� �޴� �Ÿ� (input[13] == 0x01�� ���)
					brdResult = true;
				}
				else if((input[12] == 0x08) && (input[13] == 0x02)) {
					ackResult = true;
				}
				else brdResult = false;
			}
		}
		
		if(srcResult == true) { //�޾ƿ��� �����Ͱ� �´� ���
			//ACK�� ������.
			this.GetUnderLayer().Send(makeAck(), makeAck().length);
			System.out.println("�ޱ� : " + input.length);
			data = RemoveCappHeader(input, input.length); 
			this.GetUpperLayer(0).Receive(data);
			return true;	
		}
		else if(brdResult == true) { //�޾ƿ��� �����Ͱ� �´� ���
			//ACK�� ������.
			this.GetUnderLayer().Send(makeAck(), makeAck().length);
			System.out.println("�ޱ� ��ε�: " + input.length);
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
