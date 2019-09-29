package ipc;

import java.util.ArrayList;

public class ChatAppLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	private class _CAPP_HEADER {
		int capp_src;
		int capp_dst;
		byte[] capp_totlen;

		public _CAPP_HEADER() {
			this.capp_src = 0x00000000;
			this.capp_dst = 0x00000000;
			this.capp_totlen = new byte[2];
		}
	}

	_CAPP_HEADER m_sHeader = new _CAPP_HEADER();

	public ChatAppLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}

	public void ResetHeader() {
		for (int i = 0; i < 2; i++) {
			m_sHeader.capp_totlen[i] = (byte) 0x00;
		}
	}

//	public byte[] ObjToByte(_CAPP_HEADER Header, byte[] input, int length) {
//		byte[] buf = new byte[length + 6];
//		byte[] srctemp = intToByte2(Header.capp_src);
//		byte[] dsttemp = intToByte2(Header.capp_dst);
//
//		buf[0] = dsttemp[0];
//		buf[1] = dsttemp[1];
//		buf[2] = srctemp[0];
//		buf[3] = srctemp[1];
//		buf[4] = (byte) (length % 256); // 바이트로 표현하기 위한 것
//		buf[5] = (byte) (length / 256);
//
//		for (int i = 0; i < length; i++) //데이터 저장
//			buf[6 + i] = input[i];
//
//		return buf;
//	}
	
	public byte[] ObjToByte(_CAPP_HEADER Header, byte[] input, int length) {
		byte[] buf = new byte[length + 10];
		byte[] srctemp = intToByte4(Header.capp_src);
		byte[] dsttemp = intToByte4(Header.capp_dst);

		buf[0] = dsttemp[0];
		buf[1] = dsttemp[1];
		buf[2] = dsttemp[2];
		buf[3] = dsttemp[3];
		buf[4] = srctemp[0];
		buf[5] = srctemp[1];
		buf[6] = srctemp[2];
		buf[7] = srctemp[3];
		buf[8] = (byte) (length % 256); // 바이트로 표현하기 위한 것
		buf[9] = (byte) (length / 256);

		for (int i = 0; i < length; i++) //데이터 저장
			buf[10 + i] = input[i];

		return buf;
	}

	public boolean Send(byte[] input, int length) { //보내는 데이터, 과제
		//헤더에 src주소랑 dst주소를 집어넣어야함.
		byte[] data = ObjToByte(m_sHeader, input, length);
		this.GetUnderLayer().Send(data, data.length);
		return true;
	}

//	public byte[] RemoveCappHeader(byte[] input, int length) { //과제
//		//앞의 6칸 삭제
//		byte[] input2 = new byte[length-6];
//		System.arraycopy(input, 6, input2, 0, length-6);
//		
//		return input2;// 변경하세요 필요하시면
//	}
	
	public byte[] RemoveCappHeader(byte[] input, int length) { //과제
		//앞의 6칸 삭제
		byte[] input2 = new byte[length-10];
		System.arraycopy(input, 10, input2, 0, length-10);
		
		return input2;// 변경하세요 필요하시면
	}

//	public synchronized boolean Receive(byte[] input) { //소켓레이어를 통해서 받은 데이터
//		byte[] data;
//		byte[] temp_src = intToByte2(m_sHeader.capp_src);
//		for (int i = 0; i < 2; i++) {
//			if (input[i] != temp_src[i]) {
//				return false;
//			}
//		}
//		data = RemoveCappHeader(input, input.length); //소켓 헤더를 분리
//		this.GetUpperLayer(0).Receive(data);
//		// 주소설정
//		return true;
//	}
	
	public synchronized boolean Receive(byte[] input) { //소켓레이어를 통해서 받은 데이터
		byte[] data;
		byte[] temp_src = intToByte4(m_sHeader.capp_src);
		for (int i = 0; i < 4; i++) {
			if (input[i] != temp_src[i]) {
				return false;
			}
		}
		data = RemoveCappHeader(input, input.length); //소켓 헤더를 분리
		this.GetUpperLayer(0).Receive(data);
		// 주소설정
		return true;
	}

//	byte[] intToByte2(int value) { //바이트로 변경.
//		byte[] temp = new byte[2];
//		temp[1] = (byte) (value >> 8);
//		temp[0] = (byte) value;
//
//		return temp;
//	}
	
	byte[] intToByte4(int value) { //바이트로 변경.
		byte[] temp = new byte[4];
		
		temp[0] |= (byte) ((value & 0xFF000000) >> 24);
		temp[1] |= (byte) ((value & 0xFF0000) >> 16);
		temp[2] |= (byte) ((value & 0xFF00) >> 8);
		temp[3] |= (byte) (value & 0xFF);
		
		return temp;
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

	public void SetEnetSrcAddress(int srcAddress) {
		// TODO Auto-generated method stub
		m_sHeader.capp_src = srcAddress;
	}

	public void SetEnetDstAddress(int dstAddress) {
		// TODO Auto-generated method stub
		m_sHeader.capp_dst = dstAddress;
	}

}
