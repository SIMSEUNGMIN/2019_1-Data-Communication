package ipc;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class LayerManager {
	//���̾� ������ �����ִ� class
	//IPCDlg�� ���� Main���� ���̾ �߰��ϰ� �������� �� ���
	
	//�Է� ���� ���� �ڷ���
	//������ String�� �Է��� ���� �������� ��� ����� ������� �װ��� connectLayer�� ���
	private class _NODE{
		//_NODE ���� �ʵ�
		//�Է�����
		private String token;
		//����� ���� ���
		private _NODE next;
		//������
		public _NODE(String input){
			this.token = input;
			this.next = null;
		}
	}
	
	//�Է� ���� ���� ������ �� head�� tail
	_NODE mp_sListHead;
	_NODE mp_sListTail;
	
	//LayerManager ���� �ʵ�
	private int m_nTop;
	private int m_nLayerCount;
	
	//����ȭ�� ���̾ ���� ������ �� �� (mp_aLayers�� �ִ� ���̾ ������ ��� �׾ƿø��� ��)
	private ArrayList<BaseLayer> mp_Stack = new ArrayList<BaseLayer>();
	//�Է����� ������ ���̾�list
	private ArrayList<BaseLayer> mp_aLayers = new ArrayList<BaseLayer>() ;
	
	//LayerManager ������ (�ʱ�ȭ)
	public LayerManager(){
		m_nLayerCount = 0;
		mp_sListHead = null;
		mp_sListTail = null;
		//���� ������ �ʱ�ȭ -1
		m_nTop = -1;
	}
	
	//�Ű������� ���� ���̾ mp_aLayer�� �������
	public void AddLayer(BaseLayer pLayer){
		mp_aLayers.add(m_nLayerCount++, pLayer);
		//m_nLayerCount++;
	}
	
	//mp_aLayer�� �ִ� ���̾� �߿� index��ġ�� �ִ� ���̾ ������
	public BaseLayer GetLayer(int nindex){
		return mp_aLayers.get(nindex);
	}
	
	//mp_aLayer�� �ִ� ���̾� �߿� �Ű������� �̸��� ���� ���̾ ������
	public BaseLayer GetLayer(String pName){
		for( int i=0; i < m_nLayerCount; i++){
			if(pName.compareTo(mp_aLayers.get(i).GetLayerName()) == 0)
				return mp_aLayers.get(i);
		}
		return null;
	}
	
	//�Ű������� " "������ ����
	//���� ��ū�� List�� ���鼭 ��ɾ �°� ���̾ ����
	public void ConnectLayers(String pcList){
		MakeList(pcList); //��ū ������ ������
		LinkLayer(mp_sListHead); //���̾� ����
	}
	
	//��ū ������ ������
	private void MakeList(String pcList){
		//�Ű� ������ " "�� �������� token������ �ϳ� �� ����
		StringTokenizer tokens = new StringTokenizer(pcList, " ");
		
		//token���� Node�� ���� ����, Node���ῡ ������
		for(; tokens.hasMoreElements();){
			_NODE pNode = AllocNode(tokens.nextToken());
			AddNode(pNode);
			
		}	
	}
	
	//�Ű������� ���� �������
	private _NODE AllocNode(String pcName){
		_NODE node = new _NODE(pcName);
				
		return node;				
	}
	
	//���� ��带 ���� �߰�
	private void AddNode(_NODE pNode){
		//mp_sListHead�� ������ ù ����̱� ������ ù ������� ������ ����
		if(mp_sListHead == null){
			mp_sListHead = mp_sListTail = pNode;
		}else{
			//ù ��尡 �ƴ� ��� ���� ������� ������ pNode�� �־�� ����
			//�� ��带 pNode�� ����
			mp_sListTail.next = pNode;
			mp_sListTail = pNode;
		}
	}
	
	//mp_Stack�� �Ű����� ���̾ �߰�(�׾ƿø�)
	private void Push (BaseLayer pLayer){
		mp_Stack.add(++m_nTop, pLayer);
		//mp_Stack.add(pLayer);
		//m_nTop++;
	}
	
	//���� ���� �ִ� ���̾�(���� �ֱٿ� �� ���̾�)�� ���� (������ ����)
	private BaseLayer Pop(){
		BaseLayer pLayer = mp_Stack.get(m_nTop);
		mp_Stack.remove(m_nTop);
		m_nTop--;
		
		return pLayer;
	}
	
	//���� ���� �ִ� ���̾ ��ȯ (���� X)
	private BaseLayer Top(){
		return mp_Stack.get(m_nTop);
	}
	
	//���� pNode�� ���ƺ��� �ǵ��� �°� ���̾� �� ����
	private void LinkLayer(_NODE pNode){
		BaseLayer pLayer = null;
		
		//pNode�� ���𰡰� ���� ��
		while(pNode != null){
			//pNode�� ù ���� ���� pLayer�� �Ҵ�� ���̾ ���� ������
			//pNode�� token(���̾� �̸�)�� �´� ���̾ ã�Ƽ� �Ҵ�
			if( pLayer == null)
				pLayer = GetLayer (pNode.token);
			else{
				//�̹� �Ҵ�� ��� ���� ��� Ȯ��
				//'('�� ��� ���ÿ� �������
				if(pNode.token.equals("("))
					Push (pLayer);
				//')'�� ��� �̹� pLayer�� �� ���� �Ҵ��� �Ǿ��ְ� 
				//������ ������ ������ �ʿ� �������� ����
				else if(pNode.token.equals(")"))
					Pop();
				else{
					// '(' ')' �� �ƴ� ��� ���� ���� +Chat�����̱� ������
					//0��° char�� ��ȣ�� �и�
					char cMode = pNode.token.charAt(0);
					//�޺κ� �и�(���̾� �̸�)
					String pcName = pNode.token.substring(1, pNode.token.length());
					//�̸��� �´� ���̾ ã�ƿͼ� �Ҵ�
					pLayer = GetLayer (pcName);
					
					//��ȣ�� ���� ���̾� ���� ���� ����
					switch(cMode){
					case '*':
						Top().SetUpperUnderLayer( pLayer );
						break;
					case '+':
						Top().SetUpperLayer( pLayer );
						break;
					case '-':
						Top().SetUnderLayer( pLayer );
						break;
					}					
				}
			}
			
			//���� ��带 �ҷ���
			pNode = pNode.next;
				
		}
	}
	
	
}
