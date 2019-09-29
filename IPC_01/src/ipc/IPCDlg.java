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
	
	//���̾� �߰��� ������ ���� ��ü ����
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
		
		//��ü ����
		m_LayerMgr.AddLayer(new SocketLayer("Socket"));
		m_LayerMgr.AddLayer(new ChatAppLayer("Chat"));
		m_LayerMgr.AddLayer(new IPCDlg("GUI"));
		
		//���̾� ����
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
				//Setting ��ư�� Ŭ������ �� 
				if(Setting_Button.getText().equals("Setting")) {
					//�ƹ��͵� ���� ���� ������ �ּ� ���� ���� ���
					if((srcAddress.getText().equals("")) || (dstAddress.getText().equals(""))) {
						JOptionPane.showMessageDialog(null, "�ּ� ���� ����");
					}
					else {
						//�ּҰ� �� �ԷµǾ������� ���̻� �� ���� ����
						srcAddress.setEnabled(false);
						dstAddress.setEnabled(false);
						//setting��ư�� reset�� ����
						Setting_Button.setText("Reset");
						
						//���̾� ����
						ChatAppLayer chat = (ChatAppLayer)GetUnderLayer();
						SocketLayer socket = (SocketLayer)chat.GetUnderLayer();
						//���� ���̾�� ê���̾ Port�� Ethernet �ּ� �ھ���
						socket.setServerPort(Integer.parseInt(dstAddress.getText()));
						socket.setClientPort(Integer.parseInt(srcAddress.getText()));
						chat.SetEnetDstAddress(Integer.parseInt(dstAddress.getText()));
						chat.SetEnetSrcAddress(Integer.parseInt(srcAddress.getText()));
						//���Ͽ��� ���� ���α׷��� ������ �����ϵ��� �� (�����Ͱ� ���� ���� �� �ֵ��� Ȱ��ȭ)
						socket.Receive();
					}
				}
				else {
					//��ư�� ������ �� reset�� ��� setting���� �ٲٰ�
					//�ּҵ��� �ٽ� �Է¹��� �� �ֵ��� ����
					Setting_Button.setText("Setting");
					srcAddress.setEnabled(true);
					dstAddress.setEnabled(true);
					srcAddress.setText("");
					dstAddress.setText("");
					ChattingArea.setText(null);
				}
				
			}
			else if(e.getSource() == Chat_send_Button) {
				//������ ���� ��ư�� ������ ��
				if(Setting_Button.getText().equals("Reset")) { //�ּҰ� ���� �Ǿ��ִ��� Ȯ��(�ּҰ� ���õǾ� ������ Setting��ư�� reset��ư���� �ٲ���ֱ� ����)
					ChattingArea.append("[SEND] : " + ChattingWrite.getText() + "\n");
					//ä��â�� �Էµ� �����͸� ����Ʈ�迭�� �ٲ� ���� ���� ���̾�� ����
					GetUnderLayer().Send(ChattingWrite.getText().getBytes(), ChattingWrite.getText().getBytes().length);
					ChattingWrite.setText(null);
				}
				else {
					JOptionPane.showMessageDialog(null, "�ּ� ���� ����");
				}
			}
		}
	}
	
	//���� �����͸� ä��â�� �����
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
