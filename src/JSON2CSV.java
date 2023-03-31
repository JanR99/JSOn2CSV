import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import org.json.*;

public class JSON2CSV extends JFrame {

    private JLabel jsonLabel, csvLabel;
    private JTextField jsonTextField, csvTextField;
    private JButton jsonButton, convertButton;

    public static void main(String[] args) {
        new JSON2CSV();
    }

    public JSON2CSV() {
        super("JSON to CSV Converter");

        jsonLabel = new JLabel("JSON File:");
        csvLabel = new JLabel("CSV File:");
        jsonTextField = new JTextField(20);
        csvTextField = new JTextField(20);
        jsonButton = new JButton("Choose");
        convertButton = new JButton("Convert");

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(jsonLabel, constraints);
        constraints.gridx = 1;
        panel.add(jsonTextField, constraints);
        constraints.gridx = 2;
        panel.add(jsonButton, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(csvLabel, constraints);
        constraints.gridx = 1;
        panel.add(csvTextField, constraints);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 3;
        panel.add(convertButton, constraints);

        jsonButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
                int result = fileChooser.showOpenDialog(JSON2CSV.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    jsonTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                    csvTextField.setText(fileChooser.getSelectedFile().getParent() + File.separator + "output.csv");
                }
            }
        });
        convertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    convertJsonToCsv(jsonTextField.getText(), csvTextField.getText());
                    JOptionPane.showMessageDialog(JSON2CSV.this, "Conversion successful!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(JSON2CSV.this, "Error: " + ex.getMessage());
                }
            }
        });

        add(panel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    private void convertJsonToCsv(String jsonFilePath, String csvFilePath) throws IOException {
        File jsonFile = new File(jsonFilePath);
        if (!jsonFile.exists()) {
            throw new FileNotFoundException("JSON file not found: " + jsonFilePath);
        }
    
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(jsonFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            throw new IOException("Error reading JSON file: ");
        }
    
        String jsonString = sb.toString();
        if (jsonString.trim().isEmpty()) {
            throw new IOException("JSON file is empty: " + jsonFilePath);
        }
    
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath))) {
            if (jsonString.trim().startsWith("{")) {
                // JSON object
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(jsonString);
                } catch (JSONException e) {
                    throw new IOException("Error parsing JSON object: " + e.getMessage(), e);
                }
    
                Iterator<String> keysIterator = jsonObject.keys();
    
                // Write header row with property names
                StringBuilder header = new StringBuilder();
                while (keysIterator.hasNext()) {
                    String key = keysIterator.next();
                    header.append(key).append(",");
                }
                header.deleteCharAt(header.length() - 1);
                writer.write(header.toString());
                writer.newLine();
    
                // Write data row with property values
                StringBuilder row = new StringBuilder();
                keysIterator = jsonObject.keys();
                while (keysIterator.hasNext()) {
                    String key = keysIterator.next();
                    String value = jsonObject.get(key).toString();
                    row.append(value).append(",");
                }
                row.deleteCharAt(row.length() - 1);
                writer.write(row.toString());
                writer.newLine();
    
            } else if (jsonString.trim().startsWith("[")) {
                // JSON array
                JSONArray jsonArray;
                try {
                    jsonArray = new JSONArray(jsonString);
                } catch (JSONException e) {
                    throw new IOException("Error parsing JSON array: " + e.getMessage(), e);
                }
    
                // Write header row with property names
                JSONObject firstObject = jsonArray.getJSONObject(0);
                Iterator<String> keysIterator = firstObject.keys();
                StringBuilder header = new StringBuilder();
                while (keysIterator.hasNext()) {
                    String key = keysIterator.next();
                    header.append(key).append(",");
                }
                header.deleteCharAt(header.length() - 1);
                writer.write(header.toString());
                writer.newLine();
    
                // Write data rows with property values
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    StringBuilder row = new StringBuilder();
                    keysIterator = jsonObject.keys();
                    while (keysIterator.hasNext()) {
                        String key = keysIterator.next();
                        String value = jsonObject.get(key).toString();
                        row.append(value).append(",");
                    }
                    row.deleteCharAt(row.length() - 1);
                    writer.write(row.toString());
                    writer.newLine();
                }
    
            } else {
                throw new IOException("Invalid JSON format");
            }
    
        } catch (IOException e) {
            throw new IOException("Error writing CSV file");
        }
    }
}
