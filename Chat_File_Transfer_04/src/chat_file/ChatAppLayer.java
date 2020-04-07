package chat_file;

import java.util.ArrayList;
import java.util.List;

public class ChatAppLayer implements BaseLayer{
   public int nUpperLayerCount = 0;
   public String pLayerName = null;
   public BaseLayer p_UnderLayer = null;
   public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
   
   public static byte[] buffer; //데이터를 모아놓는 buffer
   public static int bufferSize = 0;
   public static int numberOfData = 0;

   private class _CAPP_HEADER { //chatAppHeader
      byte[] capp_totlen;
      byte capp_type;
      byte capp_unused;

      public _CAPP_HEADER() {
         this.capp_totlen = new byte[2];
         this.capp_type = 0x00;
         this.capp_unused = 0x00;
      }
   }

   _CAPP_HEADER c_sHeader = new _CAPP_HEADER();

   public ChatAppLayer(String pName) {
      // super(pName);
      // TODO Auto-generated constructor stub
      pLayerName = pName;
      ResetHeader();
   }

   public void ResetHeader() { //header을 초기화
      for (int i = 0; i < 2; i++) {
         c_sHeader.capp_totlen[i] = (byte) 0x00;
      }
   }
   
   public void header0x00(int length) { //header0x00에 들어가는 헤더
	   byte[] lengthToByte = intToByte2(length);
	   c_sHeader.capp_totlen[0] = lengthToByte[0];
	   c_sHeader.capp_totlen[1] = lengthToByte[1];
	   c_sHeader.capp_type = 0x00;
   }
   
   public void header0x01(int length) { //header0x01에 들어가는 헤더(전체 길이가 들어가야함)
      byte[] lengthToByte = intToByte2(length);
      c_sHeader.capp_totlen[0] = lengthToByte[0];
      c_sHeader.capp_totlen[1] = lengthToByte[1];
      c_sHeader.capp_type = 0x01;
   }
   
   public void header0x02() { //header0x02에 들어가는 헤더
	   byte[] lengthToByte = intToByte2(1456);
	   c_sHeader.capp_totlen[0] = lengthToByte[0];
	   c_sHeader.capp_totlen[1] = lengthToByte[1];
	   c_sHeader.capp_type = 0x02;
   }
   
   public void header0x03(int length) { //header0x03에 들어가는 헤더(나머지 길이가 들어가야함)
      byte[] lengthToByte = intToByte2(length%1456);
      c_sHeader.capp_totlen[0] = lengthToByte[0];
      c_sHeader.capp_totlen[1] = lengthToByte[1];
      c_sHeader.capp_type = 0x03;
   }
   
   public byte[] ObjToByte(_CAPP_HEADER Header, byte[] input, int length) { //데이터에 헤더를 붙이는 작업 
      byte[] buf = new byte[length + 4];

      buf[0] = Header.capp_totlen[0];
      buf[1] = Header.capp_totlen[1];
      buf[2] = Header.capp_type;
      buf[3] = Header.capp_unused;

      for (int i = 0; i < length; i++) //데이터 저장
         buf[4 + i] = input[i];

      return buf;
   }
   
   public synchronized boolean chatSend(byte[] input, int length) {
	   BaseLayer ethernet = this.GetUnderLayer();
	   Send_Thread thread = new Send_Thread(input, length, ethernet);
	   Thread obj = new Thread(thread);
	   obj.start();
	   return false;
   }
   
   class Send_Thread implements Runnable{
	   
	   byte[] input;
	   int length;
	   EthernetLayer ethernet;
	   
	   public Send_Thread(byte[] input, int length, BaseLayer ethernet) {
		   this.input = input;
		   this.length = length;
		   this.ethernet = (EthernetLayer)ethernet;
	   }

	   @Override
	   public void run() {

		   if(length <= 1456) {
			   header0x00(length);
			   byte[] data = ObjToByte(c_sHeader, input, length);
			   ethernet.chatSend(data, data.length);
		   }
		   else {
			   for(int i = 0; i < (length / 1456) + 1; i++) {
				   if(i == 0) { // 첫 번 째 단편화의 경우
					   header0x01(length);

					   byte[] input2 = new byte[1456];
					   System.arraycopy(input, 0, input2, 0, 1456);

					   byte[] data = ObjToByte(c_sHeader, input2, 1456);
					   ethernet.chatSend(data, data.length);

				   }
				   else if(i == (length / 1456)){ // 마지막 단편화의 경우
					   header0x03(length);

					   byte[] input2 = new byte[length%1456];
					   System.arraycopy(input, 0 + (i * 1456), input2, 0, length%1456);

					   byte[] data = ObjToByte(c_sHeader, input2, length%1456);
					   ethernet.chatSend(data, data.length);

				   }
				   else { // 중간 단편화의 경우
					   header0x02();

					   byte[] input2 = new byte[1456];
					   System.arraycopy(input, 0 + (i * 1456), input2, 0, 1456);

					   byte[] data = ObjToByte(c_sHeader, input2, 1456);
					   ethernet.chatSend(data, data.length);

				   }
			   }
		   }	
	   }
   }

//   public boolean chatSend(byte[] input, int length) { //데이터를 하위 레이어로 보내기 위해서 처리하는 작업
//	   
//	   EthernetLayer ethernet = (EthernetLayer)this.GetUnderLayer();
//	   
//      if(length <= 1456) {
//         header0x00(length);
//         byte[] data = ObjToByte(c_sHeader, input, length);
//         ethernet.chatSend(data, data.length);
//         
//         return true;
//      }
//      else {
//         for(int i = 0; i < (length / 1456) + 1; i++) {
//            if(i == 0) { // 첫 번 째 단편화의 경우
//               header0x01(length);
//               
//               byte[] input2 = new byte[1456];
//               System.arraycopy(input, 0, input2, 0, 1456);
//               
//               byte[] data = ObjToByte(c_sHeader, input2, 1456);
//               ethernet.chatSend(data, data.length);
//               
//            }
//            else if(i == (length / 1456)){ // 마지막 단편화의 경우
//               header0x03(length);
//               
//               byte[] input2 = new byte[length%1456];
//               System.arraycopy(input, 0 + (i * 1456), input2, 0, length%1456);
//               
//               byte[] data = ObjToByte(c_sHeader, input2, length%1456);
//               ethernet.chatSend(data, data.length);
//           
//            }
//            else { // 중간 단편화의 경우
//               header0x02();
//               
//               byte[] input2 = new byte[1456];
//               System.arraycopy(input, 0 + (i * 1456), input2, 0, 1456);
//               
//               byte[] data = ObjToByte(c_sHeader, input2, 1456);
//               ethernet.chatSend(data, data.length);
//      
//            }
//         }
//         return true;
//      }
//   }
   
   
   byte[] intToByte2(int value) { //정수형을 byte 2배열로 바꿈.
	   byte[] temp = new byte[2];
	   temp[1] = (byte) (value >> 8);
	   temp[0] = (byte) value;

	   return temp;
   }
   
   int byte2ToInt(byte one0, byte two1) {
	   int number = (one0 & 0xFF) | ((two1 & 0xFF ) << 8);
	   return number;
   }


   public byte[] RemoveCappHeader(byte[] input, int length) {
      //앞의 4칸 삭제
      byte[] input2 = new byte[length-4];
      System.arraycopy(input, 4, input2, 0, length-4);
      
      return input2;
   }
   
   public synchronized boolean Receive(byte[] input) {
	   System.out.println("Chatchatreceive들어옴");
      //받아온 데이터가 0x00일 경우에는 바로 상위레이어로 보내고 받아온 데이터가 0x00이 아닌 경우(단편화가 되는 경우) 모아서 상위레이어로 보낸다
      byte[] data;
      
      if(input[2] == (byte)0x00) {
         data = RemoveCappHeader(input, input.length);
         ChatFileDlg cfd = (ChatFileDlg)this.GetUpperLayer(0);
         cfd.chatReceive(data);
         return true;
      }
      else if(input[2] == (byte)0x01) {
    	 
    	 numberOfData = byte2ToInt(input[0], input[1]);
    	 System.out.println("전체 데이터 길이 : " + numberOfData);
    	 buffer = new byte[numberOfData];
         data = RemoveCappHeader(input, input.length);
         System.out.println("첫번째 : " + data.length);
         for(int i = 0; i < 1456; i++) {
        	 System.out.println("첫번째 반복문 : " + i);
            buffer[i] = data[i];
         }
         bufferSize = 1456;
      }
      else if(input[2] == (byte)0x02) {
    	  data = RemoveCappHeader(input, input.length);
    	  System.out.println("두번째 : " + data.length);
          for(int i = 0; i < 1456; i++) {
        	  System.out.println("두번째 반복문 : " + i);
             buffer[bufferSize + i] = data[i];
          }
          bufferSize = bufferSize + 1456;
      }
      else {
         data = RemoveCappHeader(input, input.length);
         System.out.println("세번째 길이 : " + data.length);
         for(int i = 0; i < data.length; i++) {
        	 System.out.println("세번째 반복문 : " + i);
            buffer[bufferSize + i] = data[i];
         }
         bufferSize = bufferSize + data.length;
         
         if(numberOfData == bufferSize) {
        	 //마지막에 데이터가 들어왔을 때 현재 버퍼에 쌓인 데이터와 마지막 데이터가 같을 경우에만 합쳐서 올림
        	 System.out.println("들어감");
        	 System.out.println(numberOfData);
        	 System.out.println(bufferSize);
        	 ChatFileDlg cfd = (ChatFileDlg) this.GetUpperLayer(0);
        	 cfd.chatReceive(buffer);
         }
         
         bufferSize = 0;
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