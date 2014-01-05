package magicbox;
import java.awt.Dimension;
import java.awt.EventQueue;
import javax.swing.JFrame;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JSlider;
import javax.swing.JButton;
import javax.swing.Timer;

import com.serotonin.bacnet4j.exception.BACnetException;

public class GuiBacnet implements ActionListener {
	
	private JFrame frame;
	private JTextField newSollH;
	private JTextField newSollC;
	private JTextField textFieldH, 	textFieldC;
	private JButton btnExec;
	private JLabel lblSollwertHeizungs;
	private JLabel lblSollwertCooling;
	private JLabel lblActualTemperatures;
		
	private JCheckBox chckbxFreigabeAnlage;
	private JCheckBox chckbxHeatingPump;
	private JCheckBox chckbxCoolingPump;
	
	private Timer timer;
	
	
	
	private int tempFahr = 0;
	
	private BacnetLogic system;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuiBacnet window = new GuiBacnet();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	/**
	 * Create the application.
	 */
	public GuiBacnet() {
		initialize();
	}
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("GUI");
		timer = new Timer(1000, this);
		timer.start();
		
		//frame.setBounds(100, 100, 450, 300);
		
		frame.setMinimumSize(new Dimension(450, 300));
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(5, 3));

		
		//Actual Temp
		lblActualTemperatures = new JLabel("Actual Temperature: %s");

		//Empty Label
		JLabel lblEmpty = new JLabel("");

		
		//Sollwert Heizung
		lblSollwertHeizungs = new JLabel("Sollwert Heizung: %s");
		
		//FreigabeAnlage		
		chckbxFreigabeAnlage = new JCheckBox("FreigabeAnlage");

		textFieldH = new JTextField(1);
	
		//HeatingPump	
		chckbxHeatingPump = new JCheckBox("HeatingPump");
		
		//Sollwert Kuhlung
		lblSollwertCooling = new JLabel("Sollwert Cooling: %s");
		
		//CoolingPump	
		chckbxCoolingPump = new JCheckBox("CoolingPump");

		textFieldC = new JTextField(1);

		btnExec = new JButton("Execute");
		btnExec.addActionListener(this);

		
		// Add everything to <gridlayout
		frame.getContentPane().add(lblActualTemperatures);
		frame.getContentPane().add(lblEmpty);
		frame.getContentPane().add(lblSollwertHeizungs);
		frame.getContentPane().add(chckbxFreigabeAnlage);
		frame.getContentPane().add(textFieldH);
		frame.getContentPane().add(chckbxHeatingPump);
		frame.getContentPane().add(lblSollwertCooling);
		frame.getContentPane().add(chckbxCoolingPump);
		frame.getContentPane().add(textFieldC);
		frame.getContentPane().add(btnExec);
		
		
		try {
			system = new BacnetLogic();
			system.doDiscover();
		} catch (Exception e) {
			system.terminate();
		}
		
	}
	
	
	
	public void actionPerformed(ActionEvent event) {
		
		if (event.getSource() == timer) {
			try {
				lblSollwertHeizungs.setText(system.readDevice(system.d_sollwertKalten).toString());
				lblSollwertHeizungs.setText(system.readDevice(system.d_sollwertHeizung).toString());
				lblActualTemperatures.setText(system.readDevice(system.d_temperature).toString());
			} catch (BACnetException e) {
				e.printStackTrace();
			}
			
		}
		else if (event.getSource() == btnExec) {
			
			System.out.println("Excecute");
			System.out.println(chckbxCoolingPump.isSelected());
			
			try {
				system.writeDevice(system.d_A03_FRG_Kalten ,chckbxCoolingPump.isSelected());
				system.writeDevice(system.d_A06_FRG_Durchlauf ,chckbxHeatingPump.isSelected());
				system.writeDevice(system.d_freigabeAnlagen ,chckbxFreigabeAnlage.isSelected());
			
			} catch (BACnetException e) {
				e.printStackTrace();
			}
		}
		/*			
        int tempFahr = (int)((Double.parseDouble(textFieldH.getText())));

        lblSollwertHeizungs.setText("Sollwert Heizung: " + tempFahr);
        */
    }
	
	
	
}