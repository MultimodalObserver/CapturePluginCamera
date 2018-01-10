package webcamcaptureplugin;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDiscoveryService;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mo.core.ui.GridBConstraints;
import mo.core.ui.Utils;
import mo.organization.ProjectOrganization;

public class WebcamCaptureConfigurationDialog extends JDialog implements DocumentListener {

    JLabel errorLabel;
    JTextField nameField;
    JButton accept;
    JComboBox cbCamera;
    JComboBox cbFPS;
    JComboBox cbDIM;
    public int id_camera;
    public int fps_option;
    public int dim_option;
    ProjectOrganization org;    
    private static WebcamDiscoveryService discovery = Webcam.getDiscoveryService(); 

    boolean accepted = false;
    private static final java.util.List<Webcam> wCam = Webcam.getWebcams();

    public WebcamCaptureConfigurationDialog() {
        super(null, "WebCam Capture Configuration", Dialog.ModalityType.APPLICATION_MODAL);
    }

    public WebcamCaptureConfigurationDialog(ProjectOrganization organization) {
        super(null, "WebCam Capture Configuration", Dialog.ModalityType.APPLICATION_MODAL);
        org = organization;
    }

    public boolean showDialog() {

        setLayout(new GridBagLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                accepted = false;
                super.windowClosing(e);
            }
        });

        setLayout(new GridBagLayout());
        GridBConstraints gbc = new GridBConstraints();

        JLabel label = new JLabel("Configuration name: ");
        JLabel webcam = new JLabel("Select your device:");
        JLabel fps = new JLabel("FPS:");
        String[] frames = {"5","15","30","45","60"};
        JLabel dim = new JLabel("Dimension:");
        String[] dimensiones = {"176x144","320x240","640x480","800x600","1024x768","1280x720","1366x768","1920x1080"};
        String[ ] cameras = new String[wCam.size()];
        for(int i=0;i<wCam.size();i++){
            cameras[i]=wCam.get(i).getName();
        }
        //discovery.stop se encarga de detener la busqueda de dispositivos, una vez se ha iniciado el layout
        discovery.stop();
        cbCamera = new JComboBox(cameras);
        cbFPS = new JComboBox(frames);
        cbDIM = new JComboBox(dimensiones);
        cbFPS.setSelectedIndex(4);
        nameField = new JTextField();
        nameField.getDocument().addDocumentListener(this);

        gbc.gx(0).gy(0).f(GridBConstraints.HORIZONTAL).a(GridBConstraints.FIRST_LINE_START).i(new Insets(5, 5, 5, 5));
        add(label, gbc);
        add(nameField, gbc.gx(2).wx(1).gw(3));
        add(webcam, gbc.gy(2).gx(0));
        add(cbCamera,gbc.gx(2).gy(2).wx(1).gw(3));
        add(fps,gbc.gx(0).gy(4));
        add(cbFPS,gbc.gx(2).gy(4).wx(1).gw(3));
        add(dim,gbc.gx(0).gy(6));
        add(cbDIM,gbc.gx(2).gy(6).wx(1).gw(3));
              

        errorLabel = new JLabel("");
        errorLabel.setForeground(Color.red);
        add(errorLabel, gbc.gx(0).gy(7).gw(5).a(GridBConstraints.LAST_LINE_START).wy(1));

        accept = new JButton("Accept");
        
        accept.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accepted = true;
                id_camera=cbCamera.getSelectedIndex();
                fps_option=cbFPS.getSelectedIndex(); //0==15; 1 == 30; 2== 45; 3==60
                dim_option=cbDIM.getSelectedIndex(); 
                setVisible(false);
                dispose();
            }
        });

        gbc.gx(0).gy(6).a(GridBConstraints.LAST_LINE_END).gw(3).wy(1).f(GridBConstraints.NONE);
        add(accept, gbc);

        setMinimumSize(new Dimension(400, 150));
        setPreferredSize(new Dimension(400, 300));
        pack();
        Utils.centerOnScreen(this);
        updateState();
        setVisible(true);

        return accepted;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateState();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateState();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateState();
    }

    private void updateState() {
        if (nameField.getText().isEmpty()) {
            errorLabel.setText("A name for this configuration must be specified");
            accept.setEnabled(false);
        } else {
            errorLabel.setText("");
            accept.setEnabled(true);
        }
    }

    public String getConfigurationName() {
        return nameField.getText();
    }
}

