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
	private JTextField textFieldH, textFieldC, textFieldVentilation;
	private JButton btnExec;
	private JLabel lblSollwertHeizungs;
	private JLabel lblSollwertCooling;
	private JLabel lblActualTemperatures;
	private JLabel lblVentilation;

	private JCheckBox chckbxFreigabeAnlage;
	private JCheckBox chckbxHeatingPump;
	private JCheckBox chckbxCoolingPump;
	private JCheckBox chckbxManualMode;

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

		// frame.setBounds(100, 100, 450, 300);

		frame.setMinimumSize(new Dimension(450, 300));

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(6, 3));

		// Actual Temp
		lblActualTemperatures = new JLabel("Actual Temperature: ");

		// Ventilation
		lblVentilation = new JLabel("Ventilation: ");
		
		// Empty Label
		JLabel lblEmpty = new JLabel("");

		// Sollwert Heizung
		lblSollwertHeizungs = new JLabel("Sollwert Heizung:");

		// FreigabeAnlage
		chckbxFreigabeAnlage = new JCheckBox("Freigabe Anlage: ");
		
		//
		textFieldH = new JTextField("20", 1);

		//
		textFieldVentilation = new JTextField("100", 1);
		
		// Manual mode
		chckbxManualMode = new JCheckBox("Manual Mode: off");

		// HeatingPump
		chckbxHeatingPump = new JCheckBox("Heating Pump: ");

		// Sollwert Kuhlung
		lblSollwertCooling = new JLabel("Sollwert Cooling: ");

		// CoolingPump
		chckbxCoolingPump = new JCheckBox("Cooling Pump: ");

		textFieldC = new JTextField("30", 1);

		btnExec = new JButton("Execute");
		btnExec.addActionListener(this);

		// Add everything to <gridlayout
		frame.getContentPane().add(lblVentilation);
		frame.getContentPane().add(chckbxManualMode);
		frame.getContentPane().add(textFieldVentilation);
		frame.getContentPane().add(chckbxFreigabeAnlage);
		frame.getContentPane().add(lblSollwertHeizungs);
		frame.getContentPane().add(chckbxHeatingPump);
		frame.getContentPane().add(textFieldH);
		frame.getContentPane().add(chckbxCoolingPump);
		frame.getContentPane().add(lblSollwertCooling);
		frame.getContentPane().add(lblActualTemperatures);
		frame.getContentPane().add(textFieldC);
		frame.getContentPane().add(btnExec);

		chckbxHeatingPump.setEnabled(false);
		chckbxCoolingPump.setEnabled(false);
		textFieldVentilation.setEnabled(false);
		
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
				lblVentilation
				.setText("Ventilation: "
						+ system.readDevice(system.d_M04_VG)
								.toString());
				
				lblSollwertCooling
						.setText("Sollwert Cooling: "
								+ system.readDevice(system.d_sollwertKalten)
										.toString());
				lblSollwertHeizungs.setText("Sollwert Heating: "
						+ system.readDevice(system.d_sollwertHeizung)
								.toString());
				lblActualTemperatures.setText("Actual temperature: "
						+ system.readDevice(system.d_temperature).toString());

				chckbxFreigabeAnlage.setText("Freigabe Anlage: "
						+ (system.readDevice(system.d_freigabeAnlagen)
								.toString().equals("1") ? "on" : "off"));
				chckbxHeatingPump.setText("Heating Pump: "
						+ (system.readDevice(system.d_A06_FRG_Durchlauf)
								.toString().equals("1") ? "on" : "off"));
				chckbxCoolingPump.setText("Cooling Pump: "
						+ (system.readDevice(system.d_A03_FRG_Kalten)
								.toString().equals("1") ? "on" : "off"));

			} catch (BACnetException e) {
				e.printStackTrace();
			}

		} else if (event.getSource() == btnExec) {

			try {
				system.writeDevice(system.d_freigabeAnlagen,
						chckbxFreigabeAnlage.isSelected());
				chckbxManualMode.setText("Manual Mode: "
						+ (chckbxManualMode.isSelected()? "on" : "off"));
				if (chckbxManualMode.isSelected()) {
					textFieldVentilation.setEnabled(true);
					chckbxCoolingPump.setEnabled(true);
					chckbxHeatingPump.setEnabled(true);
					textFieldC.setEnabled(false);
					textFieldH.setEnabled(false);
					system.writeDevice(system.d_A03_FRG_Kalten,
							chckbxCoolingPump.isSelected());
					system.writeDevice(system.d_A06_FRG_Durchlauf,
							chckbxHeatingPump.isSelected());
					

					if (!textFieldVentilation.getText().isEmpty()
							&& isNumeric(textFieldVentilation.getText())) {

						int ventSpeed = (int) ((Double.parseDouble(textFieldVentilation.getText())));
						ventSpeed=ventSpeed<0 ? 0:ventSpeed;
						ventSpeed=ventSpeed>100 ? 100:ventSpeed;
						
						system.writeDevice(system.d_M04_VG, 
								ventSpeed);
						system.writeDevice(system.d_M05_VG, 
								ventSpeed);
						
					}
					
					if (chckbxCoolingPump.isSelected()){
						system.writeDevice(system.d_M02_VG, 100);
					}
					else {
						system.writeDevice(system.d_M02_VG, 0);
					}
					if (chckbxHeatingPump.isSelected()){
						system.writeDevice(system.d_M03_VG, 100);
					}
					else {
						system.writeDevice(system.d_M03_VG, 0);
					}

					
				} else {
					textFieldVentilation.setEnabled(false);
					textFieldC.setEnabled(true);
					textFieldH.setEnabled(true);
					chckbxCoolingPump.setEnabled(false);
					chckbxHeatingPump.setEnabled(false);

					if (!textFieldH.getText().isEmpty()
							&& isNumeric(textFieldH.getText())) {

						system.writeDevice(
								system.d_sollwertHeizung,
								(int) ((Double.parseDouble(textFieldH.getText()))));
					}
					if (!textFieldH.getText().isEmpty()
							&& isNumeric(textFieldH.getText())) {

						system.writeDevice(
								system.d_sollwertKalten,
								(int) ((Double.parseDouble(textFieldC.getText()))));
					}
				}
			} catch (BACnetException e1) {
				e1.printStackTrace();
			}
		}
	}

	public static boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

}
