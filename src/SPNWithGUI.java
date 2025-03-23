import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * SPN Encryption/Decryption Implementation with Enhanced GUI
 *
 * This program implements a Substitution-Permutation Network (SPN) cipher
 * with an improved graphical user interface to encrypt and decrypt custom messages.
 *
 * Parameters:
 * - Rounds (r) = 4
 * - n = 4 (bits per S-box)
 * - m = 4 (number of S-boxes)
 * - s = 32 (key length in bits)
 */
public class SPNWithGUI extends JFrame {
    // Color scheme
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 245);
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);  // Steel Blue
    private static final Color SECONDARY_COLOR = new Color(211, 211, 211);  // Light Gray
    private static final Color ACCENT_COLOR = new Color(255, 127, 80);  // Coral
    private static final Color TEXT_COLOR = new Color(50, 50, 50);  // Dark Gray

    // Font settings
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font TEXT_FONT = new Font("Monospaced", Font.PLAIN, 14);

    // Components
    private JTextArea inputTextArea;
    private JTextArea encryptedTextArea;
    private JTextArea decryptedTextArea;
    private JButton encryptButton;
    private JButton decryptButton;
    private JButton resetButton;
    private JButton copyEncryptedButton;
    private JButton copyDecryptedButton;
    private JCheckBox showVerboseOutput;
    private JLabel statusLabel;

    private SPN spn;

    public SPNWithGUI() {
        // Initialize SPN
        spn = new SPN(4, 4, 4);

        // Set up the JFrame
        setTitle("SPN Encryption/Decryption Tool");
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Create the components
        setupComponents();

        // Display the frame
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setupComponents() {
        // Create the header panel
        JPanel headerPanel = createHeaderPanel();

        // Create the main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Create input panel
        JPanel inputPanel = createTextPanel("Eingabetext", "Geben Sie hier den zu verschlüsselnden Text ein:", true);
        inputTextArea = createTextArea();
        inputPanel.add(new JScrollPane(inputTextArea), BorderLayout.CENTER);

        // Create encrypted text panel
        JPanel encryptedPanel =
            createTextPanel("Verschlüsselter Text (Binär)", "Der verschlüsselte Text im binären Format:", false);
        encryptedTextArea = createTextArea();
        encryptedTextArea.setEditable(false);
        encryptedPanel.add(new JScrollPane(encryptedTextArea), BorderLayout.CENTER);

        // Add copy button for encrypted text
        copyEncryptedButton = new JButton("Kopieren");
        styleButton(copyEncryptedButton, new Color(70, 70, 70));  // Dunklere Farbe für besseren Kontrast
        copyEncryptedButton.addActionListener(e -> copyToClipboard(encryptedTextArea.getText()));
        copyEncryptedButton.setEnabled(false);
        JPanel encryptedButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        encryptedButtonPanel.setBackground(BACKGROUND_COLOR);
        encryptedButtonPanel.add(copyEncryptedButton);
        encryptedPanel.add(encryptedButtonPanel, BorderLayout.SOUTH);

        // Create decrypted text panel
        JPanel decryptedPanel = createTextPanel("Entschlüsselter Text", "Der entschlüsselte Text:", false);
        decryptedTextArea = createTextArea();
        decryptedTextArea.setEditable(false);
        decryptedPanel.add(new JScrollPane(decryptedTextArea), BorderLayout.CENTER);

        // Add copy button for decrypted text
        copyDecryptedButton = new JButton("Kopieren");
        styleButton(copyDecryptedButton, new Color(70, 70, 70));  // Dunklere Farbe für besseren Kontrast
        copyDecryptedButton.addActionListener(e -> copyToClipboard(decryptedTextArea.getText()));
        copyDecryptedButton.setEnabled(false);
        JPanel decryptedButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        decryptedButtonPanel.setBackground(BACKGROUND_COLOR);
        decryptedButtonPanel.add(copyDecryptedButton);
        decryptedPanel.add(decryptedButtonPanel, BorderLayout.SOUTH);

        // Create action buttons panel
        JPanel actionPanel = createActionPanel();

        // Create status panel
        JPanel statusPanel = createStatusPanel();

        // Add all panels to the content panel
        contentPanel.add(inputPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(encryptedPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(decryptedPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(actionPanel);

        // Add all components to the frame
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Substitution-Permutation Netzwerk (SPN) Verschlüsselung");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Verschlüsseln und Entschlüsseln mit SPN im CTR-Modus");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(230, 230, 230));

        JPanel labelPanel = new JPanel(new GridLayout(2, 1));
        labelPanel.setBackground(PRIMARY_COLOR);
        labelPanel.add(titleLabel);
        labelPanel.add(subtitleLabel);

        headerPanel.add(labelPanel, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createTextPanel(String title, String description, boolean withInfo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        // Create titled border with custom styling
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            title
        );
        titledBorder.setTitleFont(TITLE_FONT);
        titledBorder.setTitleColor(PRIMARY_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            titledBorder,
            BorderFactory.createEmptyBorder(5, 10, 10, 10)
        ));

        if (withInfo) {
            JPanel infoPanel = new JPanel(new BorderLayout());
            infoPanel.setBackground(BACKGROUND_COLOR);

            JLabel descLabel = new JLabel(description);
            descLabel.setFont(LABEL_FONT);
            descLabel.setForeground(TEXT_COLOR);
            infoPanel.add(descLabel, BorderLayout.WEST);

            panel.add(infoPanel, BorderLayout.NORTH);
        }

        return panel;
    }

    private JTextArea createTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setFont(TEXT_FONT);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return textArea;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(BACKGROUND_COLOR);

        encryptButton = new JButton("Verschlüsseln");
        decryptButton = new JButton("Entschlüsseln");
        resetButton = new JButton("Zurücksetzen");

        styleButton(encryptButton, PRIMARY_COLOR);
        styleButton(decryptButton, new Color(60, 100, 140));  // Dunklere Farbe für besseren Kontrast
        styleButton(resetButton, ACCENT_COLOR);

        encryptButton.addActionListener(e -> encryptText());
        decryptButton.addActionListener(e -> decryptText());
        resetButton.addActionListener(e -> resetFields());

        // Add verbose output option
        showVerboseOutput = new JCheckBox("Ausführliche Ausgabe");
        showVerboseOutput.setFont(LABEL_FONT);
        showVerboseOutput.setBackground(BACKGROUND_COLOR);
        showVerboseOutput.setForeground(TEXT_COLOR);
        showVerboseOutput.setSelected(true);  // Per default aktiviert

        panel.add(encryptButton);
        panel.add(decryptButton);
        panel.add(resetButton);
        panel.add(showVerboseOutput);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SECONDARY_COLOR);
        panel.setBorder(new EmptyBorder(5, 20, 5, 20));

        statusLabel = new JLabel("Bereit");
        statusLabel.setFont(LABEL_FONT);

        panel.add(statusLabel, BorderLayout.WEST);

        return panel;
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(LABEL_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(140, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void encryptText() {
        String inputText = inputTextArea.getText();
        if (inputText.isEmpty()) {
            updateStatus("Fehler: Bitte geben Sie Text zum Verschlüsseln ein.", true);
            return;
        }

        try {
            // Convert text to binary (ASCII)
            String binaryText = textToBinary(inputText);

            if (showVerboseOutput.isSelected()) {
                encryptedTextArea.setText("Original (als Binär): " + binaryText + "\n\n");
            } else {
                encryptedTextArea.setText("");
            }

            // Add padding according to specifications (add a '1' and then zeros until length is divisible by 16)
            binaryText += "1";
            while (binaryText.length() % 16 != 0) {
                binaryText += "0";
            }

            if (showVerboseOutput.isSelected()) {
                encryptedTextArea.append("Mit Padding: " + binaryText + "\n\n");
            }

            // Split into blocks of 16 bits
            String[] blocks = Helper.splitString(binaryText, 16);

            // Generate IV (first block) - for simplicity, use a fixed IV here
            String iv = "0000010011010010";

            // Encrypt using CTR mode
            StringBuilder encryptedText = new StringBuilder();
            encryptedText.append(iv);  // First block is IV

            if (showVerboseOutput.isSelected()) {
                encryptedTextArea.append("Initialisierungsvektor (IV): " + iv + "\n\n");
                encryptedTextArea.append("Verschlüsselung im CTR-Modus:\n");
            }

            for (int i = 0; i < blocks.length; i++) {
                String counterBlock = Helper.binaryStringAddNumber(iv, i);
                String encryptedCounter = spn.encrypt(counterBlock);
                String cipherBlock = Helper.xorBinaryStrings(encryptedCounter, blocks[i]);

                if (showVerboseOutput.isSelected()) {
                    encryptedTextArea.append("Block " + (i + 1) + ":\n");
                    encryptedTextArea.append("  Counter: " + counterBlock + "\n");
                    encryptedTextArea.append("  Verschlüsselter Counter: " + encryptedCounter + "\n");
                    encryptedTextArea.append("  Klartext: " + blocks[i] + "\n");
                    encryptedTextArea.append("  Geheimtext: " + cipherBlock + "\n\n");
                }

                encryptedText.append(cipherBlock);
            }

            if (showVerboseOutput.isSelected()) {
                encryptedTextArea.append("Kompletter Geheimtext:\n");
            }

            encryptedTextArea.append(encryptedText.toString());
            copyEncryptedButton.setEnabled(true);
            updateStatus("Text erfolgreich verschlüsselt", false);
        } catch (Exception ex) {
            updateStatus("Fehler bei der Verschlüsselung: " + ex.getMessage(), true);
        }
    }

    private void decryptText() {
        String encryptedText = encryptedTextArea.getText().trim();
        if (encryptedText.isEmpty()) {
            updateStatus("Fehler: Bitte verschlüsseln Sie zuerst einen Text.", true);
            return;
        }

        try {
            // Remove any verbose output text if present
            if (encryptedText.contains("Kompletter Geheimtext:")) {
                encryptedText =
                    encryptedText.substring(encryptedText.lastIndexOf("Kompletter Geheimtext:") + 22).trim();
            } else if (encryptedText.contains("Block")) {
                encryptedText = encryptedText.lines()
                    .filter(line -> !line.contains(":") && !line.trim().isEmpty() && !line.trim().startsWith(" "))
                    .reduce((a, b) -> b)
                    .orElse("");
            }

            String[] cipherBlocks = Helper.splitString(encryptedText, 16);

            if (showVerboseOutput.isSelected()) {
                decryptedTextArea.setText("Entschlüsselung im CTR-Modus:\n");
                decryptedTextArea.append("Anzahl der Blöcke: " + cipherBlocks.length + "\n");
                decryptedTextArea.append("IV: " + cipherBlocks[0] + "\n\n");
            } else {
                decryptedTextArea.setText("");
            }

            String decryptedBinary = spn.ctr(cipherBlocks);

            if (showVerboseOutput.isSelected()) {
                decryptedTextArea.append("Entschlüsselter Binärtext: " + decryptedBinary + "\n\n");
            }

            String decryptedText = Helper.binaryToText(decryptedBinary);

            if (showVerboseOutput.isSelected()) {
                decryptedTextArea.append("Entschlüsselter Text: ");
            }

            decryptedTextArea.append(decryptedText);
            copyDecryptedButton.setEnabled(true);
            updateStatus("Text erfolgreich entschlüsselt", false);
        } catch (Exception ex) {
            updateStatus("Fehler bei der Entschlüsselung: " + ex.getMessage(), true);
        }
    }

    private void resetFields() {
        inputTextArea.setText("");
        encryptedTextArea.setText("");
        decryptedTextArea.setText("");
        copyEncryptedButton.setEnabled(false);
        copyDecryptedButton.setEnabled(false);
        updateStatus("Alle Felder zurückgesetzt", false);
    }

    private void copyToClipboard(String text) {
        if (text != null && !text.isEmpty()) {
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(text), null);
            updateStatus("Text in die Zwischenablage kopiert", false);
        }
    }

    private void updateStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? ACCENT_COLOR : TEXT_COLOR);
    }

    private String textToBinary(String text) {
        StringBuilder binary = new StringBuilder();
        for (char character : text.toCharArray()) {
            // Convert each character to its 8-bit binary representation
            String binaryChar = Integer.toBinaryString(character);
            // Pad with leading zeros to ensure 8 bits
            while (binaryChar.length() < 8) {
                binaryChar = "0" + binaryChar;
            }
            binary.append(binaryChar);
        }
        return binary.toString();
    }

    public static void main(String[] args) {
        // Run the validation test first
        runValidationTest();

        // Set a more modern look and feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Fallback to default look and feel
            }
        }

        // Start the GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SPNWithGUI();
            }
        });
    }

    private static void runValidationTest() {
        // Test vector from the assignment with custom key
        String testInput = "0001001010001111";
        String expectedOutput = "1010111010110100";

        // Create a test SPN instance with the test key k = 0001 0001 0010 1000 1000 1100 0000 0000
        int[] testKey = {
            0b0001, // 1
            0b0001, // 1
            0b0010, // 2
            0b1000, // 8
            0b1000, // 8
            0b1100, // 12
            0b0000, // 0
            0b0000  // 0
        };

        SPN testSpn = new SPN(4, 4, 4, testKey);
        String testResult = testSpn.encrypt(testInput);

        System.out.println("Test vector validation:");
        System.out.println("Input:    " + testInput);
        System.out.println("Expected: " + expectedOutput);
        System.out.println("Result:   " + testResult);
        System.out.println("Test " + (testResult.equals(expectedOutput) ? "PASSED" : "FAILED"));
        System.out.println();
    }

    // SPN Class Implementation
    static class SPN {
        // S-Box as specified in the assignment
        // x:    0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F
        // S(x): E  4  D  1  2  F  B  8  3  A  6  C  5  9  0  7
        private static final int[] sBox = {0xE, 4, 0xD, 1, 2, 0xF, 0xB, 8, 3, 0xA, 6, 0xC, 5, 9, 0, 7};

        // Bit permutation as specified in the assignment
        // x:    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
        // β(x): 0  4  8 12  1  5  9 13  2  6 10 14  3  7 11 15
        private static final int[] permutation = {0, 4, 8, 12, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15};

        private final int s;
        private final int n;
        private final int m;
        private int[][] roundKeys;
        private final int rounds;
        private int[] inv;

        // Default key as specified in the assignment: 0011 1010 1001 0100 1101 0110 0011 1111
        private static final int[] DEFAULT_KEY = {
            0b0011, // 3
            0b1010, // 10
            0b1001, // 9
            0b0100, // 4
            0b1101, // 13
            0b0110, // 6
            0b0011, // 3
            0b1111  // 15
        };

        private int[] key;

        /**
         * Constructor using the default key
         */
        public SPN(int rounds, int n, int m) {
            this.n = n;
            this.m = m;
            this.s = n * m;
            this.rounds = rounds;
            this.key = DEFAULT_KEY;
            generateRoundKeys();
        }

        /**
         * Constructor using a custom key (for testing)
         */
        public SPN(int rounds, int n, int m, int[] customKey) {
            this.n = n;
            this.m = m;
            this.s = n * m;
            this.rounds = rounds;
            this.key = customKey;
            generateRoundKeys();
        }

        /**
         * Generates round keys for all encryption/decryption rounds
         */
        private void generateRoundKeys() {
            this.roundKeys = new int[rounds + 1][m];
            for (int i = 0; i <= rounds; i++) {
                roundKeys[i] = genRoundKey(i);
            }
        }

        /**
         * Performs the complete encryption process on the input text
         */
        public String encrypt(String text) {
            int[] message = Helper.splitBinaryString(text);
            message = initialEncipherStep(message);
            message = encipher(1, message);
            message = Helper.fourBitArraytoBinaryArray(message);
            return Helper.intArrayToString(message);
        }

        /**
         * Performs the complete decryption process on the input cipher text
         */
        public String decrypt(String text) {
            int[] cipher = Helper.splitBinaryString(text);
            inv = Helper.inverseArray(sBox);
            cipher = initialDecipherStep(cipher);
            cipher = decipher(1, cipher);
            cipher = Helper.fourBitArraytoBinaryArray(cipher);
            return Helper.intArrayToString(cipher);
        }

        private int[] encipher(int round, int[] message) {
            if (round < rounds) {
                sBox(message);
                message = bitPermutation(message);
                message = Helper.xorArrays(message, roundKeys[round]);
                return encipher(round + 1, message);
            }
            return finalEncipherStep(message);
        }

        private int[] decipher(int round, int[] cipher) {
            if (round < rounds) {
                sBoxInverse(cipher);
                cipher = bitPermutation(cipher);
                int[] roundKey = bitPermutation(roundKeys[rounds - round]);
                cipher = Helper.xorArrays(cipher, roundKey);
                return decipher(round + 1, cipher);
            }
            return finalDecipherStep(cipher);
        }

        private void sBox(int[] message) {
            for (int i = 0; i < message.length; i++) {
                int index = message[i];
                message[i] = sBox[index];
            }
        }

        private void sBoxInverse(int[] cipher) {
            for (int i = 0; i < cipher.length; i++) {
                int index = cipher[i];
                cipher[i] = inv[index];
            }
        }

        private int[] bitPermutation(int[] message) {
            int[] bitMsg = Helper.fourBitArraytoBinaryArray(message);
            int[] newMsg = new int[bitMsg.length];
            for (int i = 0; i < bitMsg.length; i++) {
                int newPosition = permutation[i];
                newMsg[newPosition] = bitMsg[i];
            }
            return Helper.binaryArrytoFourBitArray(newMsg);
        }

        private int[] initialEncipherStep(int[] message) {
            int[] roundKey0 = roundKeys[0];
            return Helper.xorArrays(message, roundKey0);
        }

        private int[] finalEncipherStep(int[] message) {
            sBox(message);
            return Helper.xorArrays(message, roundKeys[rounds]);
        }

        private int[] initialDecipherStep(int[] cipher) {
            int[] roundKey0 = roundKeys[rounds];
            return Helper.xorArrays(cipher, roundKey0);
        }

        private int[] finalDecipherStep(int[] cipher) {
            sBoxInverse(cipher);
            return Helper.xorArrays(cipher, roundKeys[0]);
        }

        private int[] genRoundKey(int round) {
            int[] roundKey = new int[m];
            int roundKeyIndex = 0;
            for (int i = round; i < round + m; i++) {
                roundKey[roundKeyIndex] = key[i];
                roundKeyIndex++;
            }
            return roundKey;
        }

        /**
         * Implements Counter (CTR) mode decryption
         */
        public String ctr(String[] ciphers) {
            StringBuilder text = new StringBuilder();
            for (int i = 1; i < ciphers.length; i++) {
                // Generate counter value by adding (i-1) to the IV (ciphers[0])
                String yi = Helper.binaryStringAddNumber(ciphers[0], i - 1);
                // Encrypt the counter value
                String result = encrypt(yi);
                // XOR the encrypted counter with the cipher block to get plaintext
                String res = Helper.xorBinaryStrings(result, ciphers[i]);
                text.append(res);
            }
            return text.toString();
        }
    }

    // Helper class implementation
    static class Helper {
        public static int[] xorArrays(int[] a, int[] b) {
            if (a.length != b.length) {
                throw new IllegalArgumentException("Arrays must have same length");
            }

            int[] result = new int[a.length];
            for (int i = 0; i < a.length; i++) {
                result[i] = a[i] ^ b[i];
            }

            return result;
        }

        public static int[] splitBinaryString(String binaryString) {
            int length = binaryString.length();
            int[] result = new int[(length + 3) / 4];

            for (int i = 0; i < length; i += 4) {
                int endIndex = Math.min(i + 4, length);
                String chunk = binaryString.substring(i, endIndex);
                while (chunk.length() < 4) {
                    chunk = "0" + chunk;  // Pad with leading zeros if needed
                }
                int value = Integer.parseInt(chunk, 2);
                result[i / 4] = value;
            }
            return result;
        }

        public static int[] fourBitArraytoBinaryArray(int[] message) {
            int[] bits = new int[message.length * 4];

            for (int i = 0; i < message.length; i++) {
                int current = message[i];
                for (int j = 0; j < 4; j++) {
                    bits[i * 4 + j] = (current >> (3 - j)) & 1;
                }
            }
            return bits;
        }

        public static int[] binaryArrytoFourBitArray(int[] message) {
            int[] result = new int[message.length / 4];

            for (int i = 0; i < message.length; i += 4) {
                int combined = 0;
                for (int j = 0; j < 4; j++) {
                    combined <<= 1;
                    combined |= message[i + j];
                }
                result[i / 4] = combined;
            }
            return result;
        }

        public static int[] inverseArray(int[] arr) {
            int[] inv = new int[arr.length];
            for (int i = 0; i < arr.length; i++) {
                int newIndex = arr[i];
                inv[newIndex] = i;
            }
            return inv;
        }

        public static String[] splitString(String message, int blockSize) {
            int len = message.length();
            String[] chunks = new String[(int) Math.ceil((double) len / blockSize)];

            for (int i = 0; i < chunks.length; i++) {
                int start = i * blockSize;
                int end = Math.min(start + blockSize, len);
                chunks[i] = message.substring(start, end);
                // Pad with zeros if needed
                while (chunks[i].length() < blockSize) {
                    chunks[i] = chunks[i] + "0";
                }
            }
            return chunks;
        }

        public static String intArrayToString(int[] arr) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length; i++) {
                sb.append(arr[i]);
            }
            return sb.toString();
        }

        public static String binaryStringAddNumber(String binaryStr, int number) {
            int num = Integer.parseInt(binaryStr, 2);
            int result = (num + number) % 65536; // 2^16 = 65536
            String binaryResult = Integer.toBinaryString(result);
            return padLeft(binaryResult, binaryStr.length());
        }

        private static String padLeft(String input, int length) {
            StringBuilder sb = new StringBuilder(input);
            while (sb.length() < length) {
                sb.insert(0, '0');
            }
            return sb.toString();
        }

        public static String xorBinaryStrings(String binaryStr1, String binaryStr2) {
            // Ensure both strings have the same length by padding the shorter one
            if (binaryStr1.length() < binaryStr2.length()) {
                binaryStr1 = padLeft(binaryStr1, binaryStr2.length());
            } else if (binaryStr2.length() < binaryStr1.length()) {
                binaryStr2 = padLeft(binaryStr2, binaryStr1.length());
            }

            StringBuilder result = new StringBuilder(binaryStr1.length());
            for (int i = 0; i < binaryStr1.length(); i++) {
                result.append(binaryStr1.charAt(i) == binaryStr2.charAt(i) ? '0' : '1');
            }
            return result.toString();
        }

        public static String binaryToText(String binary) {
            // Find the padding marker (last '1') and remove padding
            int lastOne = binary.lastIndexOf('1');
            if (lastOne != -1) {
                binary = binary.substring(0, lastOne);
            }

            StringBuilder text = new StringBuilder();
            // Process the binary string in 8-bit chunks (one byte per character)
            for (int i = 0; i + 8 <= binary.length(); i += 8) {
                String byteStr = binary.substring(i, i + 8);
                int charCode = Integer.parseInt(byteStr, 2);
                text.append((char) charCode);
            }
            return text.toString();
        }
    }
}