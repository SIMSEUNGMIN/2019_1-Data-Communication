package ipc;
import java.util.ArrayList;

interface BaseLayer {
	//레이어 인터페이스
	//계층들이 가지는 기본적 함수 정의
	//상위 레이어 리스트, 상위 레이어 개수, 하위 레이어, 현재 레이어 이름, 레이어 연결
	//Default 메소드는 기존 인터페이스에서 함수만 정의하던 것에 그치지 않고 구현을 가능하게 함
	//또한 필요시에만 override하여 사용할 수 있게 함
	
	//상위 레이어 개수
	public final int m_nUpperLayerCount = 0;
	//현재 레이어 이름
	public final String m_pLayerName = null;
	//하위 레이어
	public final BaseLayer mp_UnderLayer = null;
	//상위 레이어 list
	public final ArrayList<BaseLayer> mp_aUpperLayer = new ArrayList<BaseLayer>();
	
	
	public String GetLayerName();

	public BaseLayer GetUnderLayer();

	public BaseLayer GetUpperLayer(int nindex);

	//현재 레이어에서 매개변수를 하위 레이어로 연결
	public void SetUnderLayer(BaseLayer pUnderLayer);

	//현재 레이어에서 매개변수를 상위 레이어로 연결
	public void SetUpperLayer(BaseLayer pUpperLayer);
	
	//매개변수가 상위 레이어가 되고 이 함수를 호출하는 레이어는 하위 레이어가 됨
	public default void SetUnderUpperLayer(BaseLayer pUULayer) {
	}
	
	public void SetUpperUnderLayer(BaseLayer pUULayer);
	
	//레이어간 데이터 전송(데이터, byte형)
	public default boolean Send(byte[] input, int length) {
		return false;
	}
	
	//레이어간 데이터 전송(String형)
	public default boolean Send(String filename) {
		return false;
	}
	
	//레이어간 데이터 수신
	public default boolean Receive(byte[] input) {
		return false;
	}
	
	//레이어간 데이터 수신
	public default boolean Receive() {
		return false;
	}

}
