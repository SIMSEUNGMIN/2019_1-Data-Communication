package ipc;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class IPCDlg extends JFrame implements BaseLayer {

	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	BaseLayer UnderLayer;
	
	//레이어 추가와 연결을 위한 객체 생성
	private static LayerManager m_LayerMgr = new LayerManager();
	
	//GUI
	private JTextField ChattingWrite;

	Container contentPane;

	JTextArea ChattingArea;
	JTextArea srcAddress;
	JTextArea dstAddress;

	JLabel lblsrc;
	JLabel lbldst;

	JButton Setting_Button;
	JButton Chat_send_Button;

	static JComboBox<String> NICComboBox;

	int adapterNumber = 0;

	String Text;
	
	public static void main(String[] args) {
		
		//객체 생성
		m_LayerMgr.AddLayer(new SocketLayer("Socket"));
		m_LayerMgr.AddLayer(new ChatAppLayer("Chat"));
		m_LayerMgr.AddLayer(new IPCDlg("GUI"));
		
		//레이어 연결
		m_LayerMgr.ConnectLayers(" Socket ( *Chat ) ");
		m_LayerMgr.ConnectLayers(" Socket ( +GUI )");
		m_LayerMgr.ConnectLayers(" Chat ( *GUI ) ");
	}
	
	//GUI
	public IPCDlg(String pName) {
		pLayerName = pName;

		setTitle("IPC");
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

		setVisible(true);

	}
	
	class setAddressListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == Setting_Button) {
				//Setting 버튼을 클릭했을 시 
				if(Setting_Button.getText().equals("Setting")) {
					//아무것도 써져 있지 않으면 주소 설정 오류 출력
					if((srcAddress.getText().equals("")) || (dstAddress.getText().equals(""))) {
						JOptionPane.showMessageDialog(null, "주소 설정 오류");
					}
					else {
						//주소가 잘 입력되어있으면 더이상 못 쓰게 막음
						srcAddress.setEnabled(false);
						dstAddress.setEnabled(false);
						//setting버튼을 reset로 변경
						Setting_Button.setText("Reset");
						
						//레이어 생성
						ChatAppLayer chat = (ChatAppLayer)GetUnderLayer();
						SocketLayer socket = (SocketLayer)chat.GetUnderLayer();
						//소켓 레이어와 챗레이어에 Port와 Ethernet 주소 박아줌
						socket.setServerPort(Integer.parseInt(dstAddress.getText()));
						socket.setClientPort(Integer.parseInt(srcAddress.getText()));
						chat.SetEnetDstAddress(Integer.parseInt(dstAddress.getText()));
						chat.SetEnetSrcAddress(Integer.parseInt(srcAddress.getText()));
						//소켓에서 현재 프로그램이 서버로 동작하도록 함 (데이터가 오면 받을 수 있도록 활성화)
						socket.Receive();
					}
				}
				else {
					//버튼에 쓰여진 게 reset일 경우 setting으로 바꾸고
					//주소들을 다시 입력받을 수 있도록 설정
					Setting_Button.setText("Setting");
					srcAddress.setEnabled(true);
					dstAddress.setEnabled(true);
					srcAddress.setText("");
					dstAddress.setText("");
					ChattingArea.setText(null);
				}
				
			}
			else if(e.getSource() == Chat_send_Button) {
				//데이터 전송 버튼을 눌렀을 시
				if(Setting_Button.getText().equals("Reset")) { //주소가 세팅 되어있는지 확인(주소가 세팅되어 있으면 Setting버튼이 reset버튼으로 바뀌어있기 때문)
					ChattingArea.append("[SEND] : " + ChattingWrite.getText() + "\n");
					//채팅창에 입력된 데이터를 바이트배열로 바꾼 다음 하위 레이어로 전송
					GetUnderLayer().Send(ChattingWrite.getText().getBytes(), ChattingWrite.getText().getBytes().length);
					ChattingWrite.setText(null);
				}
				else {
					JOptionPane.showMessageDialog(null, "주소 설정 오류");
				}
			}
		}
	}
	
	//받은 데이터를 채팅창에 띄워줌
	public boolean Receive(byte[] input) {
		ChattingArea.append("[RECV] : " + new String(input) + "\n");
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
