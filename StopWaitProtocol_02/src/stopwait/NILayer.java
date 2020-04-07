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
      
      int m_iNumAdapter;   //��Ʈ��ũ ��� �ε���
      public Pcap m_AdapterObject;   //��Ʈ��ũ ��� ��ü
      public PcapIf device;   //��Ʈ��ũ �������̽� ��ü
      public List<PcapIf> m_pAdapterList;   //��Ʈ��ũ �������̽� ���
      StringBuilder errbuf = new StringBuilder();   //���� ����
   
      public int nUpperLayerCount = 0;
      public String pLayerName = null;
      public BaseLayer p_UnderLayer = null;
      public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
      public int dst_port;
      public int src_port;
      
      
      
      //������
      public NILayer(String pName) {
         
         pLayerName = pName;
         
         m_pAdapterList = new ArrayList<PcapIf>();   //���� �����Ҵ�
         m_iNumAdapter = 0;   
         SetAdapterList();   //��Ʈ��ũ ��� ��� �������� �Լ� ȣ��
      }
      
      
      
      public void SetAdapterList() {   //��Ʈ��ũ ��� ��� ���� �Լ�
         
         //���� ��ǻ�Ϳ� �����ϴ� ��� ��Ʈ��ũ ��� ��� ��������
         int r = Pcap.findAllDevs(m_pAdapterList, errbuf);
         
         // ��Ʈ��ũ ��Ͱ� �ϳ��� �������� ���� ��� ���� ó��
         if(r == Pcap.NOT_OK || m_pAdapterList.isEmpty()) {
            System.err.printf("Can't read list of devices, error is %s", errbuf.toString());
            return;
         }
      
      }
      
      public void SetAdapterNumber(int iNum) {
         m_iNumAdapter = iNum;   //���õ� ��Ʈ��ũ ��� �ε����� ���� �ʱ�ȭ
         PacketStartDriver();   //��Ŷ ����̹� ���� �Լ� (��Ʈ��ũ ��� ��ü open)
         Receive();            //��Ŷ ���� �Լ�
      }
      
      
      
    private void PacketStartDriver() {
       int snaplen = 64 * 1024;   //��Ŷ ĸó ����
       int flags = Pcap.MODE_PROMISCUOUS;   //��Ŷ ĸó �÷���(PROMISCUOUS : ��� ��Ŷ)
       int timeout = 10 * 1000;   //��Ŷ ĸó �ð�(���� �ð� ���� ��Ŷ�� ���ŵ��� ���� ��� �������ۿ� �Է�)
       m_AdapterObject = Pcap.openLive(m_pAdapterList.get(m_iNumAdapter).getName(), snaplen, flags, timeout, errbuf);
       //���õ� ��Ʈ��ũ ��� �� ������ �ɼǿ� ������ pcap �۵� ����
    }

    public boolean Receive() {
       //��Ŷ ���� �� ��Ŷ ó���� ���� runnable Ŭ���� ����
       Receive_Thread thread = new Receive_Thread(m_AdapterObject, this.GetUpperLayer(0));
       Thread obj = new Thread(thread);
       obj.start();   //thread ����
       return false;
    }
    
    class Receive_Thread implements Runnable{

       byte[] data;
       Pcap AdapterObject;
       BaseLayer UpperLayer;
       
       public Receive_Thread(Pcap m_AdapterObject, BaseLayer m_UpperLayer) {
          AdapterObject = m_AdapterObject;
          UpperLayer = m_UpperLayer;
       }//Pcap ó���� �ʿ��� ��Ʈ��ũ ��� �� ���� ���̾� ��ü �ʱ�ȭ
       
      @Override
      public void run() {
         while(true) {
            //��Ŷ ������ ���� ���̺귯�� �Լ�
            PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {
               public void nextPacket(PcapPacket packet, String user) {
                  data = packet.getByteArray(0, packet.size());   //���ŵ� ��Ŷ�� �����Ϳ� ��Ŷ ũ�⸦ �˾Ƴ�
                  UpperLayer.Receive(data);   //���ŵ� �����͸� ���� ���̾�� ����
               }
            };
            AdapterObject.loop(100000, jpacketHandler,"");   //��Ʈ��ũ ��Ϳ��� PcapPacketHandler ���ѹݺ�
         }
      } 
      
      
    }
    
    public boolean Send(byte[] input, int length) {
       ByteBuffer buf = ByteBuffer.wrap(input);   //�������̾�κ��� ���޹��� �����͸� ����Ʈ ���ۿ� ����
       if(m_AdapterObject.sendPacket(buf) != Pcap.OK) {   //��Ʈ��ũ ����� sendPacket()�Լ��� ���� ��Ƽ�� ����
          System.err.println(m_AdapterObject.getErr());   //��Ŷ ������ ������ ��� �����޽��� ��� �� false ��ȯ
          return false;
       }
       return true;   //��Ŷ ������ ������ �ܿ� true ��ȯ
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