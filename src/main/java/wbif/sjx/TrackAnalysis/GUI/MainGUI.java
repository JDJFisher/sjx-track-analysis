package wbif.sjx.TrackAnalysis.GUI;

import wbif.sjx.common.Object.TrackCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 24/06/2017.
 */
public class MainGUI implements ActionListener {
    private JFrame frame = new JFrame();
    private int frameWidth = 300;
    private int frameHeight = 600;
    private int elementHeight = 30;
    private JScrollPane moduleScrollPane = new JScrollPane();

    private TrackCollection tracks;

    private static final String TRACK_SUMMARY = new TrackSummary(null,0,0).getTitle();
    private static final String DIRECTIONAL_PERSISTENCE = new DirectionalPersistenceControl(null,0,0).getTitle();
    private static final String DIRECTIONALITY_RATIO = new DirectionalityRatioControl(null,0,0).getTitle();
    private static final String EUCLIDEAN_DISTANCE = new EuclideanDistanceControl(null,0,0).getTitle();
    private static final String MEAN_SQUARED_DISPLACEMENT = new MSDControl(null,0,0).getTitle();
    private static final String MOTILITY_PLOT = new MotilityPlotControl(null,0,0).getTitle();
    private static final String TOTAL_PATH_LENGTH = new TotalPathLengthControl(null,0,0).getTitle();

    private static final String MODULE_CHANGED = "Module changed";

    String[] moduleList = new String[]{
            TRACK_SUMMARY,
            DIRECTIONAL_PERSISTENCE,
            DIRECTIONALITY_RATIO,
            EUCLIDEAN_DISTANCE,
            MEAN_SQUARED_DISPLACEMENT,
            MOTILITY_PLOT,
            TOTAL_PATH_LENGTH
    };

    public MainGUI(TrackCollection tracks) {
        this.tracks = tracks;

    }

    public void create() {
        // Setting location of panel
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frameWidth) / 2, (screenSize.height - frameHeight) / 2);

        frame.setLayout(new GridBagLayout());
        frame.setTitle("Track analysis (v"+getClass().getPackage().getImplementationVersion()+")");
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5,5,0,5);

        JComboBox<String> comboBox = new JComboBox<>(moduleList);
        comboBox.setPreferredSize(new Dimension(frameWidth,elementHeight));
        comboBox.addActionListener(this);
        comboBox.setActionCommand(MODULE_CHANGED);
        frame.add(comboBox,c);

        changeModule(moduleList[0]);
        moduleScrollPane.setBorder(null);
        c.gridy++;
        frame.add(moduleScrollPane,c);

        frame.validate();
        frame.setVisible(true);
        frame.pack();

    }

    private void changeModule(String module) {
        ModuleControl control;
        if (module.equals(TRACK_SUMMARY)) {
            control = new TrackSummary(tracks, frameWidth, elementHeight);

        } else if (module.equals(DIRECTIONAL_PERSISTENCE)) {
            control = new DirectionalPersistenceControl(tracks, frameWidth, elementHeight);

        } else if (module.equals(DIRECTIONALITY_RATIO)) {
            control = new DirectionalityRatioControl(tracks, frameWidth, elementHeight);

        } else if (module.equals(EUCLIDEAN_DISTANCE)) {
            control = new EuclideanDistanceControl(tracks, frameWidth, elementHeight);

        } else if (module.equals(MEAN_SQUARED_DISPLACEMENT)) {
            control = new MSDControl(tracks, frameWidth, elementHeight);

        } else if (module.equals(MOTILITY_PLOT)) {
            control = new MotilityPlotControl(tracks, frameWidth, elementHeight);

        } else if (module.equals(TOTAL_PATH_LENGTH)) {
            control = new TotalPathLengthControl(tracks, frameWidth, elementHeight);

        } else {
            return;

        }

        JPanel modulePanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;

        JPanel panel = control.getCommonControls();
        c.gridy++;
        modulePanel.add(panel,c);

        panel = control.getExtraControls();
        if (panel != null) {
            c.gridy++;
            modulePanel.add(panel, c);
        }

        moduleScrollPane.setViewportView(modulePanel);
        moduleScrollPane.repaint();
        moduleScrollPane.validate();

        frame.pack();

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(MODULE_CHANGED)) {
            String module = (String) ((JComboBox) e.getSource()).getSelectedItem();
            changeModule(module);

        }
    }
}
