package ipc;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class LayerManager {
	//레이어 연결을 도와주는 class
	//IPCDlg에 보면 Main에서 레이어를 추가하고 연결해줄 때 사용
	
	//입력 값에 대한 자료형
	//들어오는 String형 입력을 공백 기준으로 노드 연결로 만든다음 그것을 connectLayer에 사용
	private class _NODE{
		//_NODE 변수 필드
		//입력형태
		private String token;
		//연결된 다음 노드
		private _NODE next;
		//생성자
		public _NODE(String input){
			this.token = input;
			this.next = null;
		}
	}
	
	//입력 값을 노드로 정리할 때 head와 tail
	_NODE mp_sListHead;
	_NODE mp_sListTail;
	
	//LayerManager 변수 필드
	private int m_nTop;
	private int m_nLayerCount;
	
	//계층화된 레이어를 스택 구조로 둔 것 (mp_aLayers에 있는 레이어를 지정한 대로 쌓아올리는 것)
	private ArrayList<BaseLayer> mp_Stack = new ArrayList<BaseLayer>();
	//입력으로 들어오는 레이어list
	private ArrayList<BaseLayer> mp_aLayers = new ArrayList<BaseLayer>() ;
	
	//LayerManager 생성자 (초기화)
	public LayerManager(){
		m_nLayerCount = 0;
		mp_sListHead = null;
		mp_sListTail = null;
		//스택 구조라 초기화 -1
		m_nTop = -1;
	}
	
	//매개변수로 들어온 레이어를 mp_aLayer에 집어넣음
	public void AddLayer(BaseLayer pLayer){
		mp_aLayers.add(m_nLayerCount++, pLayer);
		//m_nLayerCount++;
	}
	
	//mp_aLayer에 있는 레이어 중에 index위치에 있는 레이어를 꺼내옴
	public BaseLayer GetLayer(int nindex){
		return mp_aLayers.get(nindex);
	}
	
	//mp_aLayer에 있는 레이어 중에 매개변수와 이름이 같은 레이어를 꺼내옴
	public BaseLayer GetLayer(String pName){
		for( int i=0; i < m_nLayerCount; i++){
			if(pName.compareTo(mp_aLayers.get(i).GetLayerName()) == 0)
				return mp_aLayers.get(i);
		}
		return null;
	}
	
	//매개변수를 " "단위로 끊고
	//끊은 토큰의 List를 돌면서 명령어에 맞게 레이어를 연결
	public void ConnectLayers(String pcList){
		MakeList(pcList); //토큰 단위로 끊어줌
		LinkLayer(mp_sListHead); //레이어 연결
	}
	
	//토큰 단위로 끊어줌
	private void MakeList(String pcList){
		//매개 변수를 " "을 기준으로 token단위로 하나 씩 끊음
		StringTokenizer tokens = new StringTokenizer(pcList, " ");
		
		//token들을 Node로 만든 다음, Node연결에 더해줌
		for(; tokens.hasMoreElements();){
			_NODE pNode = AllocNode(tokens.nextToken());
			AddNode(pNode);
			
		}	
	}
	
	//매개변수를 노드로 만들어줌
	private _NODE AllocNode(String pcName){
		_NODE node = new _NODE(pcName);
				
		return node;				
	}
	
	//들어온 노드를 새로 추가
	private void AddNode(_NODE pNode){
		//mp_sListHead가 없으면 첫 노드이기 때문에 첫 노드이자 끝노드로 지정
		if(mp_sListHead == null){
			mp_sListHead = mp_sListTail = pNode;
		}else{
			//첫 노드가 아닐 경우 현재 끝노드의 다음에 pNode를 넣어둔 다음
			//끝 노드를 pNode로 변경
			mp_sListTail.next = pNode;
			mp_sListTail = pNode;
		}
	}
	
	//mp_Stack에 매개변수 레이어를 추가(쌓아올림)
	private void Push (BaseLayer pLayer){
		mp_Stack.add(++m_nTop, pLayer);
		//mp_Stack.add(pLayer);
		//m_nTop++;
	}
	
	//가장 위에 있는 레이어(가장 최근에 들어간 레이어)를 꺼냄 (꺼내고 삭제)
	private BaseLayer Pop(){
		BaseLayer pLayer = mp_Stack.get(m_nTop);
		mp_Stack.remove(m_nTop);
		m_nTop--;
		
		return pLayer;
	}
	
	//가장 위에 있는 레이어를 반환 (삭제 X)
	private BaseLayer Top(){
		return mp_Stack.get(m_nTop);
	}
	
	//들어온 pNode를 돌아보며 의도에 맞게 레이어 간 연결
	private void LinkLayer(_NODE pNode){
		BaseLayer pLayer = null;
		
		//pNode에 무언가가 있을 때
		while(pNode != null){
			//pNode가 첫 노드면 아직 pLayer에 할당된 레이어가 없기 때문에
			//pNode의 token(레이어 이름)에 맞는 레이어를 찾아서 할당
			if( pLayer == null)
				pLayer = GetLayer (pNode.token);
			else{
				//이미 할당된 경우 다음 노드 확인
				//'('인 경우 스택에 집어넣음
				if(pNode.token.equals("("))
					Push (pLayer);
				//')'인 경우 이미 pLayer는 들어간 노드로 할당이 되어있고 
				//문장이 끝났기 때문에 필요 없음으로 빼냄
				else if(pNode.token.equals(")"))
					Pop();
				else{
					// '(' ')' 이 아닌 경우 현재 노드는 +Chat형태이기 때문에
					//0번째 char인 기호만 분리
					char cMode = pNode.token.charAt(0);
					//뒷부분 분리(레이어 이름)
					String pcName = pNode.token.substring(1, pNode.token.length());
					//이름에 맞는 레이어를 찾아와서 할당
					pLayer = GetLayer (pcName);
					
					//기호에 맞춰 레이어 간의 관계 성립
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
			
			//다음 노드를 불러옴
			pNode = pNode.next;
				
		}
	}
	
	
}
