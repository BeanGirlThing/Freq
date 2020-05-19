package me.merhlim;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class mainPanel {

    String callsign;

    ToneGenerator toneGenerator = new ToneGenerator();

    private JPanel basePanel;
    private JPanel dataPanel;
    private JPanel InteractivePanel;
    private JButton txButton;
    private JLabel callsignLabel;
    private JTextArea mainTextInput;
    private JSlider volumeSlider;
    private JLabel volumeLabel;
    private JRadioButton tCRadioButton;
    private JButton cqButton;
    private JPanel mainPanel;
    private JTabbedPane tabbedPane1;
    private JPanel recievePanel;
    private JPanel transmitPanel;
    private JPanel dataSheetPanel;
    private JTextPane pleaseBeAwareThatTextPane;
    private JComboBox dataSheetDropdown;
    private JButton setDataSheet;
    private JLabel dataSheetLabel;
    private JTextField callsignEntryField;
    private JButton setCallsignButton;
    private JButton exitButton;
    private JComboBox audioMixerBox;
    private JButton reloadButton;
    private JTextPane waitingForDataTextPane;
    private JCheckBox listenCheckBox;
    private JComboBox inputMixerBox;
    private JButton reloadInputButton;
    private JLabel accuracyPercentLabel;
    private JMenuItem fileMenu;

    private static JSONObject dataSheet;
    private final String defaultDatasheet = "res/modes/default.json";
    private static String dataSheetURI = "res/modes/default.json";

    public mainPanel() {

        txButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if(tCRadioButton.isSelected()) {
                    transmitCallsign();
                }
                playMessage(mainTextInput.getText().replaceAll("\\s",""));
                if(tCRadioButton.isSelected()){
                    transmitCallsign();
                }

            }
        });
        volumeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                volumeLabel.setText("Volume: "+volumeSlider.getValue()+"%");
            }
        });
        cqButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String call = (String) dataSheet.get("CQCall");
                call = call.replaceAll("\\{CALLSIGN\\}",callsign);
                call = call.replaceAll("\\{DATA\\}","#");
                playMessage(call);
            }
        });

        setDataSheet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String selection = (String) dataSheetDropdown.getSelectedItem();
                if(selection.equals("default")) {
                    dataSheetURI = defaultDatasheet;
                    dataSheetLabel.setText("Data Sheet: Default");
                } else {
                    dataSheetURI = selection;
                    dataSheetLabel.setText("Data Sheet: "+dataSheetURI);
                }

                try {
                    getDataSheet();
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }

            }
        });
        setCallsignButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                callsign = callsignEntryField.getText();
                callsign = callsign.replaceAll("\\s","");
                callsign = callsign.toUpperCase();
                if(callsign.equals("")){
                    callsignLabel.setText("Callsign: None");
                    tCRadioButton.setEnabled(false);
                    tCRadioButton.setSelected(false);
                    cqButton.setEnabled(false);
                } else {
                    callsignLabel.setText("Callsign: " + callsign);
                    tCRadioButton.setEnabled(true);
                    tCRadioButton.setSelected(true);
                    cqButton.setEnabled(true);
                }
            }
        });
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.exit(0);
            }
        });
        reloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadSoundDriverList();
            }
        });
        reloadInputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadSoundDriverList();
            }
        });
    }

    public void transmitCallsign() {
        JSONObject toneList = (JSONObject) dataSheet.get("toneBase");
        try {
            tone((long) toneList.get("DATA"), 1000);
            playMessage(callsign);
            tone((long) toneList.get("DATA"), 1000);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

    public void playMessage(String message) {
        message.replaceAll("\\s","");
        JSONObject toneList = (JSONObject) dataSheet.get("toneBase");
        char[] messageCharArray = message.toCharArray();
        double[] darr = new double[messageCharArray.length];
        try {
            for (int i = 0; i < messageCharArray.length; i++) {
                char character = messageCharArray[i];
                if (character == '#') {
                    tone((long) toneList.get("DATA"), 2);
                } else {
                    String tmpC = String.valueOf(character);
                    tmpC = tmpC.toUpperCase();
                    long frequency = (long) toneList.get(tmpC);
                    tone(frequency, (long) dataSheet.get("TimePerToneMs"));

                }
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }




    public void tone(long frequency, long duration) throws LineUnavailableException {
        comboboxItem it = (comboboxItem) audioMixerBox.getSelectedItem();
        Mixer.Info mInfo = (Mixer.Info) it.value;
        toneGenerator.playTone(frequency,volumeSlider.getValue(),duration,mInfo);
    }

    private static void getDataSheet() throws IOException, ParseException {
        dataSheet = (JSONObject) new JSONParser().parse(new FileReader(dataSheetURI));
    }


    class comboboxItem {
        public String name;
        public Object value;

        public String toString(){
            return name;
        }
    }

    private void loadSoundDriverList() {
        audioMixerBox.removeAllItems();
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        boolean capture = false;
        for (Mixer.Info minfo : mixers) {
            if (minfo.getName().equals("Primary Sound Capture Driver")) {
                capture = true;
            }
            if(minfo.getName().split(" ")[0].equals("Port")) {
                break;
            }
            comboboxItem cbi = new comboboxItem();
            cbi.name = minfo.getName();
            cbi.value = minfo;

            if(capture == false) {
                audioMixerBox.addItem(cbi);
            } else {
                inputMixerBox.addItem(cbi);
            }
        }

    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("Freq");
        mainPanel contentPane = new mainPanel();
        frame.setContentPane(contentPane.basePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            getDataSheet();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        contentPane.dataSheet = dataSheet;

        contentPane.callsign = "None";

        contentPane.dataSheetLabel.setText("Data Sheet: Default");

        try (Stream<Path> walk = Files.walk(Paths.get("res/modes"))) {

            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());

            contentPane.dataSheetDropdown.addItem("default");
            for(String item : result){
                if (!item.equals("res\\modes\\default.json")) {
                    contentPane.dataSheetDropdown.addItem(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        contentPane.loadSoundDriverList();

        frame.pack();

        frame.setResizable(false);

        frame.setVisible(true);


    }
}