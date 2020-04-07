package chat_file;

import java.util.ArrayList;
import java.io.*;
import java.nio.file.Files;

public class FileAppLayer implements BaseLayer{
   public int nUpperLayerCount = 0;
   public String pLayerName = null;
   public BaseLayer p_UnderLayer = null;
   public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
   
   //��ü ����
   public static FileOutputStream receiveFile = null;
   //���� ���� ����
   public static byte[] buffer = null;
   //��ü ���� ũ��
   public static int allLength = 0;
   //���̴� ���� ũ��
   public static int fileSize = 0;
   //������ sequence�� ���ϱ� ���� ����
   public static int currentSequence = 0;
   //��ü sequence
   public static int allSequence = 0;
   
   String receiveFileName = null;
   
   
   private class _FAPP_HEADER {
      byte[] fapp_totlen;
      byte[] fapp_type;
      byte fapp_msg_type;
      byte ed;
      byte[] fapp_seq_num;
      byte[] fapp_data;

      public _FAPP_HEADER() {
         this.fapp_totlen = new byte[4];
         this.fapp_type = new byte[2];
         this.fapp_msg_type = 0x00;
         this.ed = 0x00;
         this.fapp_seq_num = new byte[4];
         this.fapp_data = null;
      }
   }

   _FAPP_HEADER f_sHeader = new _FAPP_HEADER();

   public FileAppLayer(String pName) {
      // super(pName);
      // TODO Auto-generated constructor stub
      pLayerName = pName;
      ResetHeader();
   }

   public void ResetHeader() { //header�� �ʱ�ȭ
      for (int i = 0; i < 4; i++) {
         f_sHeader.fapp_totlen[i] = (byte) 0x00;
         f_sHeader.fapp_seq_num[i] = (byte)0x00;
      }
      
      for(int i = 0; i < 2; i++) {
    	  f_sHeader.fapp_type[i] = (byte) 0x00;
      }
      
      f_sHeader.fapp_msg_type = 0x00;
      f_sHeader.ed = 0x00;
      f_sHeader.fapp_data = null;
      
   }
   
   //���00���� ��ü ����(4byte), type(00)(2byte), msg_type(00)(1byte)(���� ���ۿ�), ��ü sequenceNumber(4byte)
   public void header0x00(int length, int sequence) {
	  //��ü ���� ����
      byte[] lengthToByte = intToByte4(length);
      f_sHeader.fapp_totlen[0] = lengthToByte[0];
      f_sHeader.fapp_totlen[1] = lengthToByte[1];
      f_sHeader.fapp_totlen[2] = lengthToByte[2];
      f_sHeader.fapp_totlen[3] = lengthToByte[3];
      
      //type ����
      byte[] typeToByte = intToByte2(0x00);
      f_sHeader.fapp_type[0] = typeToByte[0];
      f_sHeader.fapp_type[1] = typeToByte[1];
      
      //msg_type ����
      f_sHeader.fapp_msg_type = 0x00;
      
      //��ü sequenceNumber ����
      byte[] numberToByte = intToByte4(sequence);
      f_sHeader.fapp_seq_num[0] = numberToByte[0];
      f_sHeader.fapp_seq_num[1] = numberToByte[1];
      f_sHeader.fapp_seq_num[2] = numberToByte[2];
      f_sHeader.fapp_seq_num[3] = numberToByte[3];

   }
   
   //���01���� ����ȭ ����(4byte), type(01)(2byte), msg_type(01)(1byte)(���� ���ۿ�), sequenceNumber(4byte)
   public void header0x01(int length, int sequence) {
	   	  //��ü ���� ����
	      byte[] lengthToByte = intToByte4(length);
	      f_sHeader.fapp_totlen[0] = lengthToByte[0];
	      f_sHeader.fapp_totlen[1] = lengthToByte[1];
	      f_sHeader.fapp_totlen[2] = lengthToByte[2];
	      f_sHeader.fapp_totlen[3] = lengthToByte[3];
	      
	      //type ����
	      byte[] typeToByte = intToByte2(0x01);
	      f_sHeader.fapp_type[0] = typeToByte[0];
	      f_sHeader.fapp_type[1] = typeToByte[1];
	      
	      //msg_type ����
	      f_sHeader.fapp_msg_type = 0x01;
	      
	      //��ü sequenceNumber ����
	      byte[] numberToByte = intToByte4(sequence);
	      f_sHeader.fapp_seq_num[0] = numberToByte[0];
	      f_sHeader.fapp_seq_num[1] = numberToByte[1];
	      f_sHeader.fapp_seq_num[2] = numberToByte[2];
	      f_sHeader.fapp_seq_num[3] = numberToByte[3];
   }
   
   //���02���� ����ȭ ������ ���� ����(4byte), type(02)(2byte), msg_type(01)(1byte)(���� ���ۿ�), sequenceNumber(4byte)
   public void header0x02(int length, int sequence) {
	   	  //��ü ���� ����
	      byte[] lengthToByte = intToByte4(length);
	      f_sHeader.fapp_totlen[0] = lengthToByte[0];
	      f_sHeader.fapp_totlen[1] = lengthToByte[1];
	      f_sHeader.fapp_totlen[2] = lengthToByte[2];
	      f_sHeader.fapp_totlen[3] = lengthToByte[3];
	      
	      //type ����
	      byte[] typeToByte = intToByte2(0x02);
	      f_sHeader.fapp_type[0] = typeToByte[0];
	      f_sHeader.fapp_type[1] = typeToByte[1];
	      
	      //msg_type ����
	      f_sHeader.fapp_msg_type = 0x01;
	      
	      //��ü sequenceNumber ����
	      byte[] numberToByte = intToByte4(sequence);
	      f_sHeader.fapp_seq_num[0] = numberToByte[0];
	      f_sHeader.fapp_seq_num[1] = numberToByte[1];
	      f_sHeader.fapp_seq_num[2] = numberToByte[2];
	      f_sHeader.fapp_seq_num[3] = numberToByte[3];
	      
   }
   
   //�����Ϳ� ����� ���̴� �۾� 
   public byte[] ObjToByte(_FAPP_HEADER Header, byte[] input, int length) {
      byte[] buf = new byte[length + 12];

      for (int i = 0; i < 4; i++) {
          buf[0 + i] = Header.fapp_totlen[i]; // 0, 1, 2, 3
          buf[8 + i] = Header.fapp_seq_num[i]; // 8, 9, 10, 11
       }
       
       for(int i = 0; i < 2; i++) {
     	  buf[4 + i] = Header.fapp_type[i]; // 4, 5
       }
       
       buf[6] = Header.fapp_msg_type;
       buf[7] = Header.ed;
  
       for (int i = 0; i < length; i++) //������ ����
         buf[12 + i] = input[i];

       return buf;
   }
   
   public synchronized boolean fileSend(byte[] input, int length) {
	   BaseLayer cfd = this.GetUpperLayer(0);
	   BaseLayer ethernet = this.GetUnderLayer();
	   Send_Thread thread = new Send_Thread(input, length, ethernet, cfd);
	   Thread obj = new Thread(thread);
	   obj.start();
	   return false;
   }
   
   //�����͸� ���� ���̾�� ������ ���ؼ� ó���ϴ� �۾�
   //�޾ƿ� ���� ��θ� ����ؼ� ���� ���� ��� ������ ��� ���ۿ� �����Ѵ�.
   //����ȭ�� ù �κ�(00)�� ���Ͽ� ���� ������ ����(�̸�, ũ��, Ÿ��)
   //�߰� �κ�(01)�� ������ ������ ����(������ ��� �����Ѵ�)
   //������ �κ�(02)�� ��� �����Ͱ� �� ���۵Ǿ��� �������̶�� �޼���
   class Send_Thread implements Runnable{
	   
	   byte[] input;
	   int length;
	   EthernetLayer ethernet;
	   ChatFileDlg dlg;
	   
	   public Send_Thread(byte[] filePath, int fileLength, BaseLayer ethernet, BaseLayer dlg) {
		   this.input = filePath;
		   this.length = fileLength;
		   this.ethernet = (EthernetLayer)ethernet;
		   this.dlg = (ChatFileDlg)dlg;
	   }
	   
	   @Override
	   public void run() {
		   
		   //�޾ƿ� ���� ��θ� string���� ��ȯ
		   String filePath = new String(input);
		   //�޾ƿ� ���� ��η� ������ ����
		   File file = new File(filePath);

		   //������ �̸��� Ȯ���ڸ� ���ϰ� byte�迭�� ��ȯ
		   String[] fileSplit = filePath.split("\\\\"); //�и��ϱ� ���ؼ� �齽���� �װ��� �ؾ���
		   String fileName = fileSplit[fileSplit.length-1]; //���� �̸��� Ȯ���ڰ� �������
		   System.out.println("���� �̸� : " + fileName);

		   //������ ���� ���� ����(byte�� �о�� buffer�迭�� ��´�)
		   byte[] allBuffer = null;
		   try {
			   allBuffer = Files.readAllBytes(file.toPath());
		   } catch (IOException e) {
			   // TODO Auto-generated catch block
			   e.printStackTrace();
		   }

		   //���� ������ ũ��
		   int bufferSize = allBuffer.length;
		   System.out.println("���� ������ : " + bufferSize);

		   //��� ������ ����ȭ�� ���ؼ� ���۵ȴٰ� ����.
		   //SequenceNumber�� �����ؼ� �ű⿡ ���缭 �����͸� �����ؾ���.
		   //�� ������ �κа� �� ���� ���� ������ ���ؼ� 2 ����.
		   int sequenceNumber = (bufferSize / 1448) + 2; 
		   System.out.println("����ȭ ���� : " + sequenceNumber);

		   for(int i = 0; i < sequenceNumber; i++) {
			   System.out.println(i);
			   if(i == 0) { //ù�� ° ����ȭ, ���Ͽ� ���� ���� ����
				   //00����� ��ü ���̶� ��ü sequenceNumber ����.
				   header0x00(bufferSize, sequenceNumber);

				   //00 ����� �ٿ��� ���� ���̾�� ����.
				   byte[] sendData = fileName.getBytes();
//				   System.out.println("filenamegetBytes : " + fileName.getBytes().length);
//				   System.out.println("sendData : " + sendData.length);
//				   System.out.println("�ٽ� ���� �̸� : " + new String(sendData));
				   sendData = ObjToByte(f_sHeader, sendData, sendData.length);
				   ethernet.fileSend(sendData, sendData.length);
			   }
			   else if(i == sequenceNumber-1) { //������ ����ȭ, ������ ���� �κ��� ����
				   //02����� ������ ���� ���̶� ���� SequenceNumber ����.
				   header0x02(bufferSize%1448, i);

				   //���� ������
				   byte[] sendBuffer = new byte[bufferSize%1448];
				   System.arraycopy(allBuffer, 0 + ((i-1) * 1448), sendBuffer, 0, bufferSize%1448);

				   //02 ����� �ٿ��� ���� ���̾�� ����
				   byte[] sendData = ObjToByte(f_sHeader, sendBuffer, bufferSize%1448);
				   ethernet.fileSend(sendData, sendData.length);
				   System.out.println("fileAppLayer file send 02");
			   }
			   else { //�߰� ����ȭ, ������ �׻� 1448byte�� ����
				   //01����� 1448byte�� ���� sequenceNumber ����.
				   header0x01(1448, i);
				   System.out.println("01������ ����");
				   //���� ������
				   byte[] sendBuffer = new byte[1448];
				   System.arraycopy(allBuffer, 0 + ((i-1) * 1448), sendBuffer, 0, 1448);

				   //01 ����� �ٿ��� ���� ���̾�� ����
				   byte[] sendData = ObjToByte(f_sHeader, sendBuffer, 1448);
				   ethernet.fileSend(sendData, sendData.length);
				   System.out.println("fileAppLayer file send 01");
			   }
			   int u = i + 1;
			   dlg.progressbar(u, sequenceNumber);
			   
			   try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		   }
	   }
	   
   }
   
   byte[] intToByte4(int value) { //int�� 4byte�� �ٲ�.
		byte[] temp = new byte[4];
		
		temp[0] |= (byte) ((value & 0xFF000000) >> 24);
		temp[1] |= (byte) ((value & 0xFF0000) >> 16);
		temp[2] |= (byte) ((value & 0xFF00) >> 8);
		temp[3] |= (byte) (value & 0xFF);
		
		return temp;
	}
   
   int byte4ToInt(byte zero, byte one, byte two, byte three) {
	   int number = 
			   (zero & 0xff) << 24 |
			   (one & 0xff) << 16 |
			   (two & 0xff) << 8 |
			   (three & 0xff);
	   return number;
   }
   
   byte[] intToByte2(int value) { //int�� 2byte�� �ٲ�.
	   byte[] temp = new byte[2];
	   temp[1] = (byte) (value >> 8);
	   temp[0] = (byte) value;

	   return temp;
   }
   
   int byte2ToInt(byte one0, byte two1) {
	   int number = one0 | (two1 << 8);
	   return number;
   }

   
   public byte[] RemoveFappHeader(byte[] input, int length) {
      //���� 12ĭ ����
      byte[] input2 = new byte[length-12];
      System.arraycopy(input, 12, input2, 0, length-12);
      
      return input2;
   }

   public synchronized boolean Receive(byte[] input) {
	   
      //�޾ƿ� �����Ͱ� 00�� ��� ���� ������ �޾Ƽ� ���� ����. (�޾ƿ� �������� ���� Ÿ�� �κ� 4, 5)
	  //������ ���ϸ����� ���� ũ�⸸ŭ�� ������ ������ ���� Ȯ��.
	  //01�� ��� ������ ���Ͽ� �����͸� �����δ�.
	  //02�� ��� ������ ������ ũ�� �� ������ ���, �� ���� �޾Ҵ��� Ȯ���ϰ� ������ �ݴ´�.
	   
	  //���� �������� Ÿ�� �κ��� int�� ��ȯ�Ѵ�.
	  int dataType = byte2ToInt(input[4], input[5]);
	  
	  ChatFileDlg cfd = (ChatFileDlg) this.GetUpperLayer(0);
	  
	  byte[] data;
	  
	  if(dataType == (byte)0x00) { //������ Ÿ���� 00�� ���
		  //msg_type�� 00�� ��
		  //��ü ����, ��ü sequence ���ϰ� ���� ����� �� ���� ���� ���� ������
		  //������ ���� ������ ���� ����, ������ ũ�⸦ ����. 
		  
		  if(input[6] == (byte)0x00) {
			  //���� ũ�� ����
			  allLength = byte4ToInt(input[0], input[1], input[2], input[3]);
			  System.out.println("���� ��ü ������ ���� : " + allLength);
			  //���� ��ü sequenceNumber ����
			  allSequence = byte4ToInt(input[8], input[9], input[10], input[11]);
			  //input�� ����� ����. (���� �̸��� ����Ʈ �迭)
			  data = RemoveFappHeader(input, input.length);
			  //���� �̸� ����
			  receiveFileName = new String(data);
			  receiveFileName = receiveFileName.trim(); //���� ����

			  //���� ����
			  try {
				  receiveFile = new FileOutputStream(receiveFileName.trim());
				  System.out.println("file receive 00 ���� ����");
			  } catch (FileNotFoundException e) {
				  // TODO Auto-generated catch block
				  e.printStackTrace();
			  }
			  //���� ũ�� ����
			  buffer = new byte[allLength];
			  System.out.println("file receive 00 ���� ���� �Ϸ�");
			  
			  currentSequence = 1;
			  
			  
			  cfd.progressbar(currentSequence, allSequence);
		  }
		  else return false;

	  }
	  else if(dataType == (byte)0x01) { //������ Ÿ���� 01�� ���

		  System.out.println("file receive 01���� ����");
		  //msg_type�� 01�� ��
		  //currentSequence�� input�� sequence�� ���� ��
		  //�����͸� �޾ƿͼ� ����
		  //������ ũ�� ����
		  //currentSequence ����
		  if(input[6] == (byte)0x01) {
			  System.out.println("file receive 01���� ���� �غ�");
			  int inputSequence = byte4ToInt(input[8], input[9], input[10], input[11]);

			  System.out.println("file receive 01���� ���� �غ� (Sequence���� ��)");
			  //input�� ����� ����. (���� ����)
			  data = RemoveFappHeader(input, input.length);

			  //���Ͽ� �־���(���� sequence�� �°� ��ġ�� �����Ͽ� ������ �־���)
			  for(int i = 0; i < 1448; i++) {
				  buffer[i + ((inputSequence-1) * 1448)] = data[i];
			  }

			  //���� ������ ������
			  fileSize = fileSize + data.length;

			  //���� currentSequence ���� �غ�
			  currentSequence++;
			  
			  cfd.progressbar(currentSequence, allSequence);
		  } 
		  return false;
		  
	  }
	  else { //������ Ÿ���� 02�� ���
			//msg_type�� 01�� ��
			//currentSequence�� input�� sequence�� ���� ��
			//�����͸� �޾ƿͼ� ����
			//������ ũ�� ����
			if(input[6] == (byte)0x01) {
				System.out.println("file receive 02���� ���� �غ�");
				int inputSequence = byte4ToInt(input[8], input[9], input[10], input[11]);
				
				System.out.println("file receive 02���� ���� �غ� (Sequence���� ��)");
				//input�� ����� ����. (���� ����)
				data = RemoveFappHeader(input, input.length);
				
				//���Ͽ� �־���
				System.out.println("������ inputSequence : " + inputSequence);
				for(int i = 0; i < allLength%1448 ; i++) {
					  buffer[i + ((inputSequence-1) * 1448)] = data[i];
				  }
				
				try {
					receiveFile.write(buffer);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				//���� �ݾ��ش�.
				try {
					
					receiveFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//���� ������ ������
				fileSize = fileSize + allLength%1448;
				System.out.println("file receive ����");

				currentSequence++;
				
				cfd.progressbar(currentSequence, allSequence);
				
				//������� ���� ������ �����ͱ��� �޾Ұ� ���� �������� ũ��� ���� ũ�Ⱑ ���ٸ�
				//���� ���̾�� �ø���
				//��ü ����, ��ü ����, ���̴� ���� ũ��, ������ sequence�� ���ϱ� ���� ����, ��ü sequence �ʱ�ȭ.
				System.out.println("currentSequence : " + currentSequence);
				System.out.println("allSequence : " + allSequence);
				System.out.println("file size : " + fileSize);
				System.out.println("allLength : " + allLength);
				if((currentSequence == allSequence) && (fileSize == allLength)) {
					System.out.println("file receive ���� ���̾� ����");
					cfd.fileReceive(receiveFileName);
				}
				
				//�ʱ�ȭ
				receiveFile = null;
				//��ü ���� ũ��
				allLength = 0;
				//���̴� ���� ũ��
				fileSize = 0;
				//������ sequence�� ���ϱ� ���� ����
				currentSequence = 1;
				//��ü sequence
				allSequence = 0;
				
			}	
			return false;
	  }
	  
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