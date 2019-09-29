package ipc;
import java.util.ArrayList;

interface BaseLayer {
	//���̾� �������̽�
	//�������� ������ �⺻�� �Լ� ����
	//���� ���̾� ����Ʈ, ���� ���̾� ����, ���� ���̾�, ���� ���̾� �̸�, ���̾� ����
	//Default �޼ҵ�� ���� �������̽����� �Լ��� �����ϴ� �Ϳ� ��ġ�� �ʰ� ������ �����ϰ� ��
	//���� �ʿ�ÿ��� override�Ͽ� ����� �� �ְ� ��
	
	//���� ���̾� ����
	public final int m_nUpperLayerCount = 0;
	//���� ���̾� �̸�
	public final String m_pLayerName = null;
	//���� ���̾�
	public final BaseLayer mp_UnderLayer = null;
	//���� ���̾� list
	public final ArrayList<BaseLayer> mp_aUpperLayer = new ArrayList<BaseLayer>();
	
	
	public String GetLayerName();

	public BaseLayer GetUnderLayer();

	public BaseLayer GetUpperLayer(int nindex);

	//���� ���̾�� �Ű������� ���� ���̾�� ����
	public void SetUnderLayer(BaseLayer pUnderLayer);

	//���� ���̾�� �Ű������� ���� ���̾�� ����
	public void SetUpperLayer(BaseLayer pUpperLayer);
	
	//�Ű������� ���� ���̾ �ǰ� �� �Լ��� ȣ���ϴ� ���̾�� ���� ���̾ ��
	public default void SetUnderUpperLayer(BaseLayer pUULayer) {
	}
	
	public void SetUpperUnderLayer(BaseLayer pUULayer);
	
	//���̾ ������ ����(������, byte��)
	public default boolean Send(byte[] input, int length) {
		return false;
	}
	
	//���̾ ������ ����(String��)
	public default boolean Send(String filename) {
		return false;
	}
	
	//���̾ ������ ����
	public default boolean Receive(byte[] input) {
		return false;
	}
	
	//���̾ ������ ����
	public default boolean Receive() {
		return false;
	}

}
