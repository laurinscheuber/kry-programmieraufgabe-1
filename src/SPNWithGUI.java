import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * SPN Encryption/Decryption Implementation with GUI
 *
 * This program implements a Substitution-Permutation Network (SPN) cipher
 * with a graphical user interface to encrypt and decrypt custom messages.
 *
 * Parameters:
 * - Rounds (r) = 4
 * - n = 4 (bits per S-box)
 * - m = 4 (number of S-boxes)
 * - s = 32 (key length in bits)
 */
public class SPNWithGUI extends JFrame {
    private JTextArea inputTextArea;
    private JTextArea encryptedTextArea;
    private JTextArea decryptedTextArea;
    private JButton encryptButton;
    private JButton decryptButton;
    private JButton resetButton;

    private SPN spn;

    public SPNWithGUI() {
        // Initialize SPN
        spn = new SPN(4, 4, 4);

        // Set up the JFrame
        setTitle("SPN Encryption/Decryption");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create the components
        setupComponents();

        // Display the frame
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setupComponents() {
        // Create input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input Text"));
        inputTextArea = new JTextArea(5, 40);
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);

        // Create encrypted text panel
        JPanel encryptedPanel = new JPanel(new BorderLayout());
        encryptedPanel.setBorder(BorderFactory.createTitledBorder("Encrypted Text (Binary)"));
        encryptedTextArea = new JTextArea(5, 40);
        encryptedTextArea.setLineWrap(true);
        encryptedTextArea.setEditable(false);
        JScrollPane encryptedScrollPane = new JScrollPane(encryptedTextArea);
        encryptedPanel.add(encryptedScrollPane, BorderLayout.CENTER);

        // Create decrypted text panel
        JPanel decryptedPanel = new JPanel(new BorderLayout());
        decryptedPanel.setBorder(BorderFactory.createTitledBorder("Decrypted Text"));
        decryptedTextArea = new JTextArea(5, 40);
        decryptedTextArea.setLineWrap(true);
        decryptedTextArea.setWrapStyleWord(true);
        decryptedTextArea.setEditable(false);
        JScrollPane decryptedScrollPane = new JScrollPane(decryptedTextArea);
        decryptedPanel.add(decryptedScrollPane, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel();
        encryptButton = new JButton("Encrypt");
        decryptButton = new JButton("Decrypt");
        resetButton = new JButton("Reset");
        buttonPanel.add(encryptButton);
        buttonPanel.add(decryptButton);
        buttonPanel.add(resetButton);

        // Add action listeners
        encryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                encryptText();
            }
        });

        decryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                decryptText();
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetFields();
            }
        });

        // Create the main panel to hold all components
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(inputPanel);
        mainPanel.add(encryptedPanel);
        mainPanel.add(decryptedPanel);
        mainPanel.add(buttonPanel);

        // Add the main panel to the frame
        add(mainPanel, BorderLayout.CENTER);
    }

    private void encryptText() {
        String inputText = inputTextArea.getText();
        if (inputText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter text to encrypt.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Convert text to binary (ASCII)
        String binaryText = textToBinary(inputText);

        // Add padding according to specifications (add a '1' and then zeros until length is divisible by 16)
        binaryText += "1";
        while (binaryText.length() % 16 != 0) {
            binaryText += "0";
        }

        // Split into blocks of 16 bits
        String[] blocks = Helper.splitString(binaryText, 16);

        // Generate IV (first block) - for simplicity, use a fixed IV here
        String iv = "0000010011010010";

        // Encrypt using CTR mode
        StringBuilder encryptedText = new StringBuilder();
        encryptedText.append(iv);  // First block is IV

        for (int i = 0; i < blocks.length; i++) {
            String counterBlock = Helper.binaryStringAddNumber(iv, i);
            String encryptedCounter = spn.encrypt(counterBlock);
            String cipherBlock = Helper.xorBinaryStrings(encryptedCounter, blocks[i]);
            encryptedText.append(cipherBlock);
        }

        encryptedTextArea.setText(encryptedText.toString());
    }

    private void decryptText() {
        String encryptedText = encryptedTextArea.getText();
        if (encryptedText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please encrypt some text first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] cipherBlocks = Helper.splitString(encryptedText, 16);
        String decryptedBinary = spn.ctr(cipherBlocks);
        String decryptedText = Helper.binaryToText(decryptedBinary);

        decryptedTextArea.setText(decryptedText);
    }

    private void resetFields() {
        inputTextArea.setText("");
        encryptedTextArea.setText("");
        decryptedTextArea.setText("");
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