package stopwait;

import java.util.ArrayList;

public class ChatAppLayer implements BaseLayer{
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	private class _CAPP_APP {
		byte[] capp_totlen;
		byte capp_type;
		byte capp_unused;

		public _CAPP_APP() {
			this.capp_totlen = new byte[2];
			this.capp_type = 0x00;
			this.capp_unused = 0x00;
		}
	}

	_CAPP_APP m_sHeader = new _CAPP_APP();

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

	public byte[] ObjToByte(_CAPP_APP Header, byte[] input, int length) {
		byte[] buf = new byte[length + 4];

		buf[0] = Header.capp_totlen[0];
		buf[1] = Header.capp_totlen[1];
		buf[2] = Header.capp_type;
		buf[3] = Header.capp_unused;

		for (int i = 0; i < length; i++) //데이터 저장
			buf[4 + i] = input[i];

		return buf;
	}

	public boolean Send(byte[] input, int length) { //보내는 데이터
		//헤더에 unused, type, Total length를 집어넣어야함.
		byte[] data = ObjToByte(m_sHeader, input, length);
		this.GetUnderLayer().Send(data, data.length);
		return true;
	}

	public byte[] RemoveCappHeader(byte[] input, int length) {
		//앞의 4칸 삭제
		byte[] input2 = new byte[length-4];
		System.arraycopy(input, 4, input2, 0, length-4);
		
		return input2;
	}
	
	public synchronized boolean Receive(byte[] input) { 
		byte[] data;
		
		data = RemoveCappHeader(input, input.length); 
		this.GetUpperLayer(0).Receive(data);
		return true;
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

}
