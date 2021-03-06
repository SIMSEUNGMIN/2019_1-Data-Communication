package chat_file;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.io.*;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import chat_file.BaseLayer;
import chat_file.ChatAppLayer;
import chat_file.LayerManager;
import chat_file.NILayer;

public class ChatFileDlg extends JFrame implements BaseLayer {

	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	BaseLayer UnderLayer;

	private static LayerManager m_LayerMgr = new LayerManager();

	private JTextField ChattingWrite;
	private JTextField filePathField;

	Container contentPane;

	JTextArea ChattingArea;
	JTextArea srcAddress;
	JTextArea dstAddress;

	JLabel lblsrc;
	JLabel lbldst;
	JLabel lblist;

	JButton Setting_Button;
	JButton Chat_send_Button;
	JButton file_Send_Button;
	
	JProgressBar progressBar;

	static JComboBox<String> NICComboBox;

	int adapterNumber = 0;

	String Text;
	String filePath;
	
	static ChatFileDlg GUI;
	static ChatAppLayer Chat;
	static FileAppLayer File;
	static EthernetLayer Ethernet;
	static NILayer Ni;
	
	public static void main(String[] args) {
		
		m_LayerMgr.AddLayer(Ni = new NILayer("Ni"));
		m_LayerMgr.AddLayer(Ethernet = new EthernetLayer("Ethernet"));
		m_LayerMgr.AddLayer(Chat = new ChatAppLayer("Chat"));
		m_LayerMgr.AddLayer(File = new FileAppLayer("File"));
		m_LayerMgr.AddLayer(GUI = new ChatFileDlg("GUI"));
		
		
		m_LayerMgr.ConnectLayers(" Ni ( *Ethernet ( *Chat ( *GUI ) *File ( *GUI ) ) ) ");
	}

	public ChatFileDlg(String pName) {
		pLayerName = pName;

		setTitle("201702034_ChatFileTransfer");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(250, 250, 644, 425);
		contentPane = new JPanel();
		((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel chattingPanel = new JPanel();// chatting panel
		chattingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "chatting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		chattingPanel.setBounds(10, 5, 360, 276);
		contentPane.add(chattingPanel);
		chattingPanel.setLayout(null);

		JPanel chattingEditorPanel = new JPanel();// chatting write panel
		chattingEditorPanel.setBounds(10, 15, 340, 210);
		chattingPanel.add(chattingEditorPanel);
		chattingEditorPanel.setLayout(null);

		ChattingArea = new JTextArea();
		ChattingArea.setEditable(false);
		ChattingArea.setBounds(0, 0, 340, 210);
		chattingEditorPanel.add(ChattingArea);// chatting edit

		JPanel chattingInputPanel = new JPanel();// chatting write panel
		chattingInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		chattingInputPanel.setBounds(10, 230, 250, 20);
		chattingPanel.add(chattingInputPanel);
		chattingInputPanel.setLayout(null);

		ChattingWrite = new JTextField();
		ChattingWrite.setBounds(2, 2, 250, 20);// 249
		chattingInputPanel.add(ChattingWrite);
		ChattingWrite.setColumns(10);// writing area

		JPanel settingPanel = new JPanel();
		settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "setting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingPanel.setBounds(380, 5, 236, 371);
		contentPane.add(settingPanel);
		settingPanel.setLayout(null);

		JPanel sourceAddressPanel = new JPanel();
		sourceAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		sourceAddressPanel.setBounds(10, 96, 170, 20);
		settingPanel.add(sourceAddressPanel);
		sourceAddressPanel.setLayout(null);
		
		lblist = new JLabel("NIC 선택");
		lblist.setBounds(10, 20, 170, 20);
		settingPanel.add(lblist);
		
		NICComboBox = new JComboBox<String>();
		NICComboBox.setBounds(10, 45, 170, 20);
		SetItem();
		settingPanel.add(NICComboBox);
		NICComboBox.addActionListener(new setAddressListener());

		lblsrc = new JLabel("Source Address");
		lblsrc.setBounds(10, 75, 170, 20);
		settingPanel.add(lblsrc);

		srcAddress = new JTextArea();
		srcAddress.setBounds(2, 2, 170, 20);
		sourceAddressPanel.add(srcAddress);// src address

		JPanel destinationAddressPanel = new JPanel();
		destinationAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		destinationAddressPanel.setBounds(10, 212, 170, 20);
		settingPanel.add(destinationAddressPanel);
		destinationAddressPanel.setLayout(null);

		lbldst = new JLabel("Destination Address");
		lbldst.setBounds(10, 187, 190, 20);
		settingPanel.add(lbldst);

		dstAddress = new JTextArea();
		dstAddress.setBounds(2, 2, 170, 20);
		destinationAddressPanel.add(dstAddress);// dst address

		Setting_Button = new JButton("Setting");// setting
		Setting_Button.setBounds(80, 270, 100, 20);
		Setting_Button.addActionListener(new setAddressListener());
		settingPanel.add(Setting_Button);// setting

		Chat_send_Button = new JButton("Send");
		Chat_send_Button.setBounds(270, 230, 80, 20);
		Chat_send_Button.addActionListener(new setAddressListener());
		chattingPanel.add(Chat_send_Button);// chatting send button
		
		//새로 추가한 부분
		JPanel fileSendPanel = new JPanel();
        fileSendPanel.setBorder(new TitledBorder(null, "File Send", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        fileSendPanel.setBounds(10, 282, 360, 94);
        contentPane.add(fileSendPanel);
        fileSendPanel.setLayout(null);

        filePathField = new JTextField();
        filePathField.setBounds(10, 25, 250, 20);
        fileSendPanel.add(filePathField);
        filePathField.setColumns(10);

        JFileChooser fileChooser = new JFileChooser();
        JButton file_Choice_Button = new JButton("File");
        
        file_Choice_Button.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
              int val = fileChooser.showOpenDialog(getParent());
              
              if(val == fileChooser.APPROVE_OPTION) {
                 filePath = fileChooser.getSelectedFile().getAbsolutePath();
                 filePathField.setText(filePath);
                 System.out.println("directory :" + filePath);
              }
           }
        });
        
        file_Choice_Button.setBounds(270, 25, 80, 20);
        fileSendPanel.add(file_Choice_Button);

        progressBar = new JProgressBar();
        progressBar.setBounds(10, 57, 250, 20);
        fileSendPanel.add(progressBar);

        file_Send_Button = new JButton("Send");
        file_Send_Button.setBounds(270, 57, 80, 20);
        fileSendPanel.add(file_Send_Button);
        
        file_Send_Button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				String[] split = filePath.split("\\\\");
				
				ChattingArea.append("[SEND] : " + split[split.length-1] + " 전송 시작 \n");
				File.fileSend(filePath.getBytes(), filePath.getBytes().length);
			}
        	
        });
        
        setVisible(true);
	}

	class setAddressListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == Setting_Button) {
				if(Setting_Button.getText().equals("Setting")) {
					if((srcAddress.getText().equals("")) || (dstAddress.getText().equals(""))) {
						JOptionPane.showMessageDialog(null, "주소 설정 오류");
					}
					else {
						srcAddress.setEnabled(false);
						dstAddress.setEnabled(false);
						Setting_Button.setText("Reset");
						try {
							Ethernet.SetEnetSrcAddress(Ni.m_pAdapterList.get(adapterNumber).getHardwareAddress());
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						Ethernet.SetEnetDstAddress(StringToByteArray(dstAddress.getText()));
						System.out.println(StringToByteArray(dstAddress.getText()));
						Ni.SetAdapterNumber(adapterNumber);
					}
				}
				else {
					Setting_Button.setText("Setting");
					srcAddress.setEnabled(true);
					dstAddress.setEnabled(true);
					srcAddress.setText("");
					dstAddress.setText("");
					ChattingArea.setText(null);
				}
				
			}
			else if(e.getSource() == Chat_send_Button) {
				if(Setting_Button.getText().equals("Reset")) {
					ChattingArea.append("[SEND] : " + ChattingWrite.getText() + "\n");
					Chat.chatSend(ChattingWrite.getText().getBytes(), ChattingWrite.getText().getBytes().length);
					ChattingWrite.setText(null);
				}
				else {
					JOptionPane.showMessageDialog(null, "주소 설정 오류");
					
				}
			}
			
			if(e.getSource() == NICComboBox) {
				adapterNumber = NICComboBox.getSelectedIndex();
				byte[] address = null;
				
				try {
					address = Ni.m_pAdapterList.get(adapterNumber).getHardwareAddress();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				srcAddress.setText(byteArrayToHexString(address));
				
			}
		}
	}
	
	public String byteArrayToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for(byte b : bytes) {
			sb.append(String.format("%02X", b&0xff));
		}
		return sb.toString();
		
	}
	
	public byte[] StringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len/2];
		for(int i = 0; i < len; i += 2) {
			data[i/2] = (byte)((Character.digit(s.charAt(i), 16) << 4) 
					+ Character.digit(s.charAt(i+1), 16));
		}
		
		return data;
	}
	

	public void progressbar(int i, int all) {
		int percent = (i*100) / (all);
		//int percent = (int)(((float)i/(float)all)*100);
		progressBar.setValue(percent);
	}

	//채팅 받았을 때 receive
	public boolean chatReceive(byte[] input) {
		ChattingArea.append("[RECV] : " + new String(input) + "\n");
		return true;
	}
	
	//파일 받았을 때 receive
	public boolean fileReceive(String fileName) {
		//파일 알람 완료
		ChattingArea.append("[RECV] : " + fileName + " 전송 완료\n");
		return true;
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
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		// TODO Auto-generated method stub
		if (p_UnderLayer == null) {
			System.out.println("null");
			return null;}
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
	
	public void SetItem() {
		Ni.SetAdapterList();
		
		for(int i = 0; i < Ni.m_pAdapterList.size(); i++) {
			NICComboBox.addItem(Ni.m_pAdapterList.get(i).getDescription());
		}
		
	}

}
