package stopwait;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.jnetpcap.*;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;


public class NILayer implements BaseLayer{
   
      static {
         try {
            //native Library Load
            System.load(new File("jnetpcap.dll").getAbsolutePath());
            System.out.println(new File("jnetpcap.dll").getAbsolutePath());
         }
         catch(UnsatisfiedLinkError e){
            System.out.println("Native code library failed to load.\n" + e);
            System.exit(1);
         }
         
      }
      
      int m_iNumAdapter;   //네트워크 어뎁터 인덱스
      public Pcap m_AdapterObject;   //네트워크 어뎁터 객체
      public PcapIf device;   //네트워크 인터페이스 객체
      public List<PcapIf> m_pAdapterList;   //네트워크 인터페이스 목록
      StringBuilder errbuf = new StringBuilder();   //에러 버퍼
   
      public int nUpperLayerCount = 0;
      public String pLayerName = null;
      public BaseLayer p_UnderLayer = null;
      public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
      public int dst_port;
      public int src_port;
      
      
      
      //생성자
      public NILayer(String pName) {
         
         pLayerName = pName;
         
         m_pAdapterList = new ArrayList<PcapIf>();   //변수 동적할당
         m_iNumAdapter = 0;   
         SetAdapterList();   //네트워크 어뎁터 목록 가져오기 함수 호출
      }
      
      
      
      public void SetAdapterList() {   //네트워크 어뎁터 목록 생성 함수
         
         //현재 컴퓨터에 존재하는 모든 네트워크 어뎁터 목록 가져오기
         int r = Pcap.findAllDevs(m_pAdapterList, errbuf);
         
         // 네트워크 어뎁터가 하나도 존재하지 않을 경우 에러 처리
         if(r == Pcap.NOT_OK || m_pAdapterList.isEmpty()) {
            System.err.printf("Can't read list of devices, error is %s", errbuf.toString());
            return;
         }
      
      }
      
      public void SetAdapterNumber(int iNum) {
         m_iNumAdapter = iNum;   //선택된 네트워크 어뎁터 인덱스로 변수 초기화
         PacketStartDriver();   //패킷 드라이버 시작 함수 (네트워크 어데버 객체 open)
         Receive();            //패킷 수신 함수
      }
      
      
      
    private void PacketStartDriver() {
       int snaplen = 64 * 1024;   //패킷 캡처 길이
       int flags = Pcap.MODE_PROMISCUOUS;   //패킷 캡처 플래그(PROMISCUOUS : 모든 패킷)
       int timeout = 10 * 1000;   //패킷 캡처 시간(설정 시간 동안 패킷이 수신되지 않은 경우 에러버퍼에 입력)
       m_AdapterObject = Pcap.openLive(m_pAdapterList.get(m_iNumAdapter).getName(), snaplen, flags, timeout, errbuf);
       //선택된 네트워크 어뎁터 및 설정된 옵션에 맞춰진 pcap 작동 시작
    }

    public boolean Receive() {
       //패킷 수신 시 패킷 처리를 위한 runnable 클래스 생성
       Receive_Thread thread = new Receive_Thread(m_AdapterObject, this.GetUpperLayer(0));
       Thread obj = new Thread(thread);
       obj.start();   //thread 시작
       return false;
    }
    
    class Receive_Thread implements Runnable{

       byte[] data;
       Pcap AdapterObject;
       BaseLayer UpperLayer;
       
       public Receive_Thread(Pcap m_AdapterObject, BaseLayer m_UpperLayer) {
          AdapterObject = m_AdapterObject;
          UpperLayer = m_UpperLayer;
       }//Pcap 처리에 필요한 네트워크 어뎁터 및 상위 레이어 객체 초기화
       
      @Override
      public void run() {
         while(true) {
            //패킷 수신을 위한 라이브러리 함수
            PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {
               public void nextPacket(PcapPacket packet, String user) {
                  data = packet.getByteArray(0, packet.size());   //수신된 패킷의 데이터와 패킷 크기를 알아냄
                  UpperLayer.Receive(data);   //수신된 데이터를 상위 레이어로 전달
               }
            };
            AdapterObject.loop(100000, jpacketHandler,"");   //네트워크 어뎁터에서 PcapPacketHandler 무한반복
         }
      } 
      
      
    }
    
    public boolean Send(byte[] input, int length) {
       ByteBuffer buf = ByteBuffer.wrap(input);   //상위레이어로부터 전달받은 데이터를 바이트 버퍼에 담음
       if(m_AdapterObject.sendPacket(buf) != Pcap.OK) {   //네트워크 어뎁터의 sendPacket()함수를 통해 데티어 전송
          System.err.println(m_AdapterObject.getErr());   //패킷 전송이 실패한 경우 에러메시지 출력 및 false 반환
          return false;
       }
       return true;   //패킷 전송이 성공한 겨우 true 반환
    }

   @Override
      public void SetUnderLayer(BaseLayer pUnderLayer) {
         // TODO Auto-generated method stub
         if (pUnderLayer == null)
            return;
         p_UnderLayer = pUnderLayer;
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
      public String GetLayerName() {
         // TODO Auto-generated method stub
         return pLayerName;
      }

      @Override
      public BaseLayer GetUnderLayer() {
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
      public void SetUpperUnderLayer(BaseLayer pUULayer) {
         this.SetUpperLayer(pUULayer);
         pUULayer.SetUnderLayer(this);

      }
}