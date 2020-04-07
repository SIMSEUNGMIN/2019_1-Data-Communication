package chat_file;

import java.util.ArrayList;
import java.io.*;
import java.nio.file.Files;

public class FileAppLayer implements BaseLayer{
   public int nUpperLayerCount = 0;
   public String pLayerName = null;
   public BaseLayer p_UnderLayer = null;
   public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
   
   //전체 파일
   public static FileOutputStream receiveFile = null;
   //파일 저장 버퍼
   public static byte[] buffer = null;
   //전체 파일 크기
   public static int allLength = 0;
   //쌓이는 파일 크기
   public static int fileSize = 0;
   //들어오는 sequence랑 비교하기 위한 변수
   public static int currentSequence = 0;
   //전체 sequence
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

   public void ResetHeader() { //header을 초기화
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
   
   //헤더00에는 전체 길이(4byte), type(00)(2byte), msg_type(00)(1byte)(정보 전송용), 전체 sequenceNumber(4byte)
   public void header0x00(int length, int sequence) {
	  //전체 길이 설정
      byte[] lengthToByte = intToByte4(length);
      f_sHeader.fapp_totlen[0] = lengthToByte[0];
      f_sHeader.fapp_totlen[1] = lengthToByte[1];
      f_sHeader.fapp_totlen[2] = lengthToByte[2];
      f_sHeader.fapp_totlen[3] = lengthToByte[3];
      
      //type 설정
      byte[] typeToByte = intToByte2(0x00);
      f_sHeader.fapp_type[0] = typeToByte[0];
      f_sHeader.fapp_type[1] = typeToByte[1];
      
      //msg_type 설정
      f_sHeader.fapp_msg_type = 0x00;
      
      //전체 sequenceNumber 설정
      byte[] numberToByte = intToByte4(sequence);
      f_sHeader.fapp_seq_num[0] = numberToByte[0];
      f_sHeader.fapp_seq_num[1] = numberToByte[1];
      f_sHeader.fapp_seq_num[2] = numberToByte[2];
      f_sHeader.fapp_seq_num[3] = numberToByte[3];

   }
   
   //헤더01에는 단편화 길이(4byte), type(01)(2byte), msg_type(01)(1byte)(내용 전송용), sequenceNumber(4byte)
   public void header0x01(int length, int sequence) {
	   	  //전체 길이 설정
	      byte[] lengthToByte = intToByte4(length);
	      f_sHeader.fapp_totlen[0] = lengthToByte[0];
	      f_sHeader.fapp_totlen[1] = lengthToByte[1];
	      f_sHeader.fapp_totlen[2] = lengthToByte[2];
	      f_sHeader.fapp_totlen[3] = lengthToByte[3];
	      
	      //type 설정
	      byte[] typeToByte = intToByte2(0x01);
	      f_sHeader.fapp_type[0] = typeToByte[0];
	      f_sHeader.fapp_type[1] = typeToByte[1];
	      
	      //msg_type 설정
	      f_sHeader.fapp_msg_type = 0x01;
	      
	      //전체 sequenceNumber 설정
	      byte[] numberToByte = intToByte4(sequence);
	      f_sHeader.fapp_seq_num[0] = numberToByte[0];
	      f_sHeader.fapp_seq_num[1] = numberToByte[1];
	      f_sHeader.fapp_seq_num[2] = numberToByte[2];
	      f_sHeader.fapp_seq_num[3] = numberToByte[3];
   }
   
   //헤더02에는 단편화 마지막 남은 길이(4byte), type(02)(2byte), msg_type(01)(1byte)(내용 전송용), sequenceNumber(4byte)
   public void header0x02(int length, int sequence) {
	   	  //전체 길이 설정
	      byte[] lengthToByte = intToByte4(length);
	      f_sHeader.fapp_totlen[0] = lengthToByte[0];
	      f_sHeader.fapp_totlen[1] = lengthToByte[1];
	      f_sHeader.fapp_totlen[2] = lengthToByte[2];
	      f_sHeader.fapp_totlen[3] = lengthToByte[3];
	      
	      //type 설정
	      byte[] typeToByte = intToByte2(0x02);
	      f_sHeader.fapp_type[0] = typeToByte[0];
	      f_sHeader.fapp_type[1] = typeToByte[1];
	      
	      //msg_type 설정
	      f_sHeader.fapp_msg_type = 0x01;
	      
	      //전체 sequenceNumber 설정
	      byte[] numberToByte = intToByte4(sequence);
	      f_sHeader.fapp_seq_num[0] = numberToByte[0];
	      f_sHeader.fapp_seq_num[1] = numberToByte[1];
	      f_sHeader.fapp_seq_num[2] = numberToByte[2];
	      f_sHeader.fapp_seq_num[3] = numberToByte[3];
	      
   }
   
   //데이터에 헤더를 붙이는 작업 
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
  
       for (int i = 0; i < length; i++) //데이터 저장
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
   
   //데이터를 하위 레이어로 보내기 위해서 처리하는 작업
   //받아온 파일 경로를 사용해서 파일 명을 얻고 파일을 열어서 버퍼에 저장한다.
   //단편화의 첫 부분(00)은 파일에 대한 정보를 전송(이름, 크기, 타입)
   //중간 부분(01)은 파일의 데이터 전송(파일을 계속 전송한다)
   //마지막 부분(02)은 모든 데이터가 다 전송되었고 마지막이라는 메세지
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
		   
		   //받아온 파일 경로를 string으로 변환
		   String filePath = new String(input);
		   //받아온 파일 경로로 파일을 구함
		   File file = new File(filePath);

		   //파일의 이름과 확장자를 구하고 byte배열로 변환
		   String[] fileSplit = filePath.split("\\\\"); //분리하기 위해서 백슬래쉬 네개로 해야함
		   String fileName = fileSplit[fileSplit.length-1]; //파일 이름과 확장자가 들어있음
		   System.out.println("파일 이름 : " + fileName);

		   //파일을 담을 버퍼 생성(byte로 읽어와 buffer배열에 담는다)
		   byte[] allBuffer = null;
		   try {
			   allBuffer = Files.readAllBytes(file.toPath());
		   } catch (IOException e) {
			   // TODO Auto-generated catch block
			   e.printStackTrace();
		   }

		   //버퍼 사이즈 크기
		   int bufferSize = allBuffer.length;
		   System.out.println("버퍼 사이즈 : " + bufferSize);

		   //모든 파일은 단편화를 통해서 전송된다고 가정.
		   //SequenceNumber를 측정해서 거기에 맞춰서 데이터를 전송해야함.
		   //맨 마지막 부분과 맨 앞의 정보 전송을 위해서 2 더함.
		   int sequenceNumber = (bufferSize / 1448) + 2; 
		   System.out.println("단편화 개수 : " + sequenceNumber);

		   for(int i = 0; i < sequenceNumber; i++) {
			   System.out.println(i);
			   if(i == 0) { //첫번 째 단편화, 파일에 대한 정보 전송
				   //00헤더에 전체 길이랑 전체 sequenceNumber 전송.
				   header0x00(bufferSize, sequenceNumber);

				   //00 헤더를 붙여서 하위 레이어로 전송.
				   byte[] sendData = fileName.getBytes();
//				   System.out.println("filenamegetBytes : " + fileName.getBytes().length);
//				   System.out.println("sendData : " + sendData.length);
//				   System.out.println("다시 파일 이름 : " + new String(sendData));
				   sendData = ObjToByte(f_sHeader, sendData, sendData.length);
				   ethernet.fileSend(sendData, sendData.length);
			   }
			   else if(i == sequenceNumber-1) { //마지막 단편화, 파일의 남은 부분을 전송
				   //02헤더에 마지막 남은 길이랑 현재 SequenceNumber 전송.
				   header0x02(bufferSize%1448, i);

				   //보낼 데이터
				   byte[] sendBuffer = new byte[bufferSize%1448];
				   System.arraycopy(allBuffer, 0 + ((i-1) * 1448), sendBuffer, 0, bufferSize%1448);

				   //02 헤더를 붙여서 하위 레이어로 전송
				   byte[] sendData = ObjToByte(f_sHeader, sendBuffer, bufferSize%1448);
				   ethernet.fileSend(sendData, sendData.length);
				   System.out.println("fileAppLayer file send 02");
			   }
			   else { //중간 단편화, 파일을 항상 1448byte로 전송
				   //01헤더에 1448byte랑 현재 sequenceNumber 전송.
				   header0x01(1448, i);
				   System.out.println("01보내고 있음");
				   //보낼 데이터
				   byte[] sendBuffer = new byte[1448];
				   System.arraycopy(allBuffer, 0 + ((i-1) * 1448), sendBuffer, 0, 1448);

				   //01 헤더를 붙여서 하위 레이어로 전송
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
   
   byte[] intToByte4(int value) { //int를 4byte로 바꿈.
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
   
   byte[] intToByte2(int value) { //int를 2byte로 바꿈.
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
      //앞의 12칸 삭제
      byte[] input2 = new byte[length-12];
      System.arraycopy(input, 12, input2, 0, length-12);
      
      return input2;
   }

   public synchronized boolean Receive(byte[] input) {
	   
      //받아온 데이터가 00일 경우 파일 정보를 받아서 파일 생성. (받아온 데이터의 파일 타입 부분 4, 5)
	  //생성시 파일명으로 파일 크기만큼의 파일을 저장할 공간 확보.
	  //01인 경우 생성된 파일에 데이터를 덧붙인다.
	  //02인 경우 생성된 파일의 크기 및 순서를 고려, 잘 전송 받았는지 확인하고 파일을 닫는다.
	   
	  //들어온 데이터의 타입 부분을 int로 변환한다.
	  int dataType = byte2ToInt(input[4], input[5]);
	  
	  ChatFileDlg cfd = (ChatFileDlg) this.GetUpperLayer(0);
	  
	  byte[] data;
	  
	  if(dataType == (byte)0x00) { //데이터 타입이 00일 경우
		  //msg_type가 00일 때
		  //전체 길이, 전체 sequence 구하고 나서 헤더를 뗀 다음 파일 명을 가져옴
		  //가져온 파일 명으로 파일 생성, 파일의 크기를 지정. 
		  
		  if(input[6] == (byte)0x00) {
			  //파일 크기 지정
			  allLength = byte4ToInt(input[0], input[1], input[2], input[3]);
			  System.out.println("받은 전체 데이터 길이 : " + allLength);
			  //파일 전체 sequenceNumber 지정
			  allSequence = byte4ToInt(input[8], input[9], input[10], input[11]);
			  //input의 헤더를 뗀다. (파일 이름이 바이트 배열)
			  data = RemoveFappHeader(input, input.length);
			  //파일 이름 지정
			  receiveFileName = new String(data);
			  receiveFileName = receiveFileName.trim(); //공백 제거

			  //파일 생성
			  try {
				  receiveFile = new FileOutputStream(receiveFileName.trim());
				  System.out.println("file receive 00 파일 생성");
			  } catch (FileNotFoundException e) {
				  // TODO Auto-generated catch block
				  e.printStackTrace();
			  }
			  //파일 크기 지정
			  buffer = new byte[allLength];
			  System.out.println("file receive 00 파일 생성 완료");
			  
			  currentSequence = 1;
			  
			  
			  cfd.progressbar(currentSequence, allSequence);
		  }
		  else return false;

	  }
	  else if(dataType == (byte)0x01) { //데이터 타입이 01일 경우

		  System.out.println("file receive 01파일 생성");
		  //msg_type가 01일 때
		  //currentSequence와 input의 sequence가 같을 때
		  //데이터를 받아와서 붙임
		  //데이터 크기 저장
		  //currentSequence 증가
		  if(input[6] == (byte)0x01) {
			  System.out.println("file receive 01파일 넣을 준비");
			  int inputSequence = byte4ToInt(input[8], input[9], input[10], input[11]);

			  System.out.println("file receive 01파일 넣을 준비 (Sequence같을 때)");
			  //input의 헤더를 뗀다. (파일 내용)
			  data = RemoveFappHeader(input, input.length);

			  //파일에 넣어줌(들어온 sequence에 맞게 위치를 선정하여 파일을 넣어줌)
			  for(int i = 0; i < 1448; i++) {
				  buffer[i + ((inputSequence-1) * 1448)] = data[i];
			  }

			  //파일 사이즈 더해줌
			  fileSize = fileSize + data.length;

			  //다음 currentSequence 받을 준비
			  currentSequence++;
			  
			  cfd.progressbar(currentSequence, allSequence);
		  } 
		  return false;
		  
	  }
	  else { //데이터 타입이 02일 경우
			//msg_type가 01일 때
			//currentSequence와 input의 sequence가 같을 때
			//데이터를 받아와서 붙임
			//데이터 크기 저장
			if(input[6] == (byte)0x01) {
				System.out.println("file receive 02파일 넣을 준비");
				int inputSequence = byte4ToInt(input[8], input[9], input[10], input[11]);
				
				System.out.println("file receive 02파일 넣을 준비 (Sequence같을 때)");
				//input의 헤더를 뗀다. (파일 내용)
				data = RemoveFappHeader(input, input.length);
				
				//파일에 넣어줌
				System.out.println("마지막 inputSequence : " + inputSequence);
				for(int i = 0; i < allLength%1448 ; i++) {
					  buffer[i + ((inputSequence-1) * 1448)] = data[i];
				  }
				
				try {
					receiveFile.write(buffer);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				//파일 닫아준다.
				try {
					
					receiveFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//파일 사이즈 더해줌
				fileSize = fileSize + allLength%1448;
				System.out.println("file receive 나감");

				currentSequence++;
				
				cfd.progressbar(currentSequence, allSequence);
				
				//순서대로 들어와 마지막 데이터까지 받았고 쌓인 데이터의 크기와 파일 크기가 같다면
				//상위 레이어로 올리고
				//전체 파일, 전체 길이, 쌓이는 파일 크기, 들어오는 sequence랑 비교하기 위한 변수, 전체 sequence 초기화.
				System.out.println("currentSequence : " + currentSequence);
				System.out.println("allSequence : " + allSequence);
				System.out.println("file size : " + fileSize);
				System.out.println("allLength : " + allLength);
				if((currentSequence == allSequence) && (fileSize == allLength)) {
					System.out.println("file receive 상위 레이어 나감");
					cfd.fileReceive(receiveFileName);
				}
				
				//초기화
				receiveFile = null;
				//전체 파일 크기
				allLength = 0;
				//쌓이는 파일 크기
				fileSize = 0;
				//들어오는 sequence랑 비교하기 위한 변수
				currentSequence = 1;
				//전체 sequence
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