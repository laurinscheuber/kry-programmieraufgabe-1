/**
 * SPN Encryption/Decryption Implementation
 *
 * This program implements a Substitution-Permutation Network (SPN) cipher
 * to decrypt a message that was encrypted using the CTR mode.
 *
 * Parameters:
 * - Rounds (r) = 4
 * - n = 4 (bits per S-box)
 * - m = 4 (number of S-boxes)
 * - s = 32 (key length in bits)
 *
 * The program demonstrates:
 * 1. Encryption and decryption using SPN
 * 2. CTR mode operation
 * 3. Decoding of an encrypted message
 */
public class CombinedSPN {
    // Main method demonstrating the encryption capabilities
    public static void main(String[] args) {
        // Create an SPN instance with the specified parameters (r=4, n=4, m=4)
        SPN spn = new SPN(4, 4, 4);

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

        // Actual encrypted message to decrypt
        String ciphertxt =
            "00000100110100100000101110111000000000101000111110001110011111110110000001010001010000111010000000010011011001110010101110110000";

        // Decrypt the message using CTR mode
        String[] cipherBlocks = Helper.splitString(ciphertxt, 16);
        String ctrDecryptedBinary = spn.ctr(cipherBlocks);

        // Convert binary to readable text
        String readableMessage = Helper.binaryToText(ctrDecryptedBinary);

        // Display results
        System.out.println("=== Assignment Solution ===");
        System.out.println("Encrypted ciphertext:");
        System.out.println(ciphertxt);
        System.out.println("\nDecrypted binary:");
        System.out.println(ctrDecryptedBinary);
        System.out.println("\nDecrypted message:");
        System.out.println(readableMessage);
    }

    /**
     * SPN (Substitution-Permutation Network) implementation
     *
     * This class implements the SPN cipher as specified in the assignment with:
     * - r = 4 (rounds)
     * - n = 4 (bits per S-box)
     * - m = 4 (number of S-boxes)
     * - s = 32 (key size in bits)
     */
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
         *
         * Creates a two-dimensional array where:
         * - First index represents the round number
         * - Second index represents the round key values
         */
        private void generateRoundKeys() {
            this.roundKeys = new int[rounds + 1][m];
            for (int i = 0; i <= rounds; i++) {
                roundKeys[i] = genRoundKey(i);
            }
        }

        /**
         * Performs the complete encryption process on the input text
         *
         * The encryption process:
         * 1. Converts the input binary string to 4-bit chunks
         * 2. Performs the initial whitening step (XOR with first round key)
         * 3. Executes the substitution-permutation rounds recursively
         * 4. Converts the result back to a binary string
         *
         * @param text Binary string to be encrypted (16 bits)
         * @return Encrypted binary string
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
         *
         * The decryption process:
         * 1. Converts the input binary string to 4-bit chunks
         * 2. Creates the inverse S-box for the reverse substitution
         * 3. Performs decryption steps in reverse order compared to encryption
         * 4. Converts the result back to a binary string
         *
         * @param text Encrypted binary string to be decrypted
         * @return Decrypted binary string
         */
        public String decrypt(String text) {
            int[] cipher = Helper.splitBinaryString(text);
            inv = Helper.inverseArray(sBox);
            cipher = initialDecipherStep(cipher);
            cipher = decipher(1, cipher);
            cipher = Helper.fourBitArraytoBinaryArray(cipher);
            return Helper.intArrayToString(cipher);
        }

        // For each round it runs the message through the sBox, permutes the message with the bitPermutation array
        // and finally xors the message with the round key of the specific round
        private int[] encipher(int round, int[] message) {
            if (round < rounds) {
                sBox(message);
                message = bitPermutation(message);
                message = Helper.xorArrays(message, roundKeys[round]);
                return encipher(round + 1, message);
            }
            return finalEncipherStep(message);
        }

        // For each round it runs the cipher through the inverse of the sBox
        // it permutes the cipher with the bitPermutation array
        // then it generates the round key for the decipher mechanism (bit permutation of keys[r -i]
        // finally it calls the final decipher step.
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

        // Method to change every int in the message array to the value configured in the sBox
        private void sBox(int[] message) {
            for (int i = 0; i < message.length; i++) {
                int index = message[i];
                message[i] = sBox[index];
            }
        }

        // Same as sBox but inverse so indeces become the value and the other way around
        private void sBoxInverse(int[] cipher) {
            for (int i = 0; i < cipher.length; i++) {
                int index = cipher[i];
                cipher[i] = inv[index];
            }
        }

        // Bit permutation for each bit in the message array
        // It first converts the int to a binary representation and then changes its index to
        // the one defined in the permutation array
        private int[] bitPermutation(int[] message) {
            int[] bitMsg = Helper.fourBitArraytoBinaryArray(message);
            int[] newMsg = new int[bitMsg.length];
            for (int i = 0; i < bitMsg.length; i++) {
                int newPosition = permutation[i];
                newMsg[newPosition] = bitMsg[i];
            }
            return Helper.binaryArrytoFourBitArray(newMsg);
        }

        // Does xor of round key0 and the input message
        private int[] initialEncipherStep(int[] message) {
            int[] roundKey0 = roundKeys[0];
            return Helper.xorArrays(message, roundKey0);
        }

        // Does the final encipher step which runs the message through the sBox and xors the message with the last round key
        private int[] finalEncipherStep(int[] message) {
            sBox(message);
            return Helper.xorArrays(message, roundKeys[rounds]);
        }

        // Does the same as initialEncipherStep but with the last round key instead of the first one
        private int[] initialDecipherStep(int[] cipher) {
            int[] roundKey0 = roundKeys[rounds];
            return Helper.xorArrays(cipher, roundKey0);
        }

        // Does the same as finalEncipherStep but with the first round key instead of the last one
        private int[] finalDecipherStep(int[] cipher) {
            sBoxInverse(cipher);
            return Helper.xorArrays(cipher, roundKeys[0]);
        }

        // Generates the round key for a specific round
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
         *
         * According to the assignment, the CTR mode works as follows:
         * 1. The first block is used as the initial value (IV)
         * 2. For each subsequent block i, compute y_i = IV + (i-1)
         * 3. Encrypt y_i using the SPN
         * 4. XOR the result with the ciphertext block to get the plaintext
         *
         * @param ciphers Array of cipher blocks where ciphers[0] is the IV
         * @return Decrypted binary string containing the original message
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

    /**
     * Helper class implementation
     *
     * Provides utility methods for:
     * - Binary/text conversions
     * - Bit manipulation operations
     * - Array transformations
     * - String operations specific to the SPN implementation
     */
    static class Helper {
        /**
         * Performs a bitwise XOR operation between two integer arrays
         *
         * @param a First integer array
         * @param b Second integer array
         * @return New array containing the result of a XOR b
         * @throws IllegalArgumentException if arrays have different lengths
         */
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

        /**
         * Splits a binary string into 4-bit chunks and converts each chunk to an integer
         *
         * @param binaryString The binary string to split (e.g., "0001001010001111")
         * @return An array of integers, each representing a 4-bit chunk
         */
        public static int[] splitBinaryString(String binaryString) {
            int length = binaryString.length();
            // Round up to nearest multiple of 4 to handle strings not divisible by 4
            int[] result = new int[(length + 3) / 4];

            // Iterate over the binary string in 4-bit chunks
            for (int i = 0; i < length; i += 4) {
                // Calculate the end index, handling the last chunk that might be shorter
                int endIndex = Math.min(i + 4, length);
                // Extract the current 4-bit chunk
                String chunk = binaryString.substring(i, endIndex);
                // Convert the binary chunk to a decimal integer (0-15)
                int value = Integer.parseInt(chunk, 2);
                // Store the value in the appropriate position in the result array
                result[i / 4] = value;
            }
            return result;
        }

        public static int[] fourBitArraytoBinaryArray(int[] message) {
            int[] bits = new int[message.length * 4];

            for (int i = 0; i < message.length; i++) {
                int current = message[i];
                // Extract each bit by shifting and masking
                for (int j = 0; j < 4; j++) {
                    bits[i * 4 + j] = (current >> (3 - j)) & 1;
                }
            }
            return bits;
        }

        public static int[] binaryArrytoFourBitArray(int[] message) {
            int[] result = new int[message.length / 4]; // the resulting integer array

            for (int i = 0; i < message.length; i += 4) {
                int combined = 0; // initialize the combined value to 0
                for (int j = 0; j < 4; j++) {
                    combined <<= 1; // shift the message of the combined value to the left by 1
                    combined |= message[i + j]; // OR the next bit of the input array into the combined value
                }
                result[i / 4] = combined; // add the combined value to the resulting integer array
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
            // Use the provided blockSize parameter instead of hardcoding
            String[] chunks = new String[(int) Math.ceil((double) len / blockSize)];

            for (int i = 0; i < chunks.length; i++) {
                int start = i * blockSize;
                int end = Math.min(start + blockSize, len);
                chunks[i] = message.substring(start, end);
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

        /**
         * Convert a 16-bit binary string to an incremented binary string
         *
         * @param binaryStr Original binary string
         * @param number Value to add to the binary string
         * @return The incremented binary string
         */
        public static String binaryStringAddNumber(String binaryStr, int number) {
            // Convert the binary string to an integer
            int num = Integer.parseInt(binaryStr, 2);

            // Add the number and take modulo 2^16 to ensure 16-bit result
            int result = (num + number) % 65536; // 2^16 = 65536

            // Convert back to binary and ensure proper length
            String binaryResult = Integer.toBinaryString(result);
            return padLeft(binaryResult, binaryStr.length());
        }

        /**
         * Pad a string with leading zeros to the specified length
         *
         * @param input The string to pad
         * @param length The desired length
         * @return Padded string
         */
        private static String padLeft(String input, int length) {
            StringBuilder sb = new StringBuilder(input);
            while (sb.length() < length) {
                sb.insert(0, '0');
            }
            return sb.toString();
        }

        /**
         * XOR two binary strings of equal length
         *
         * @param binaryStr1 First binary string
         * @param binaryStr2 Second binary string
         * @return Result of XOR operation as a binary string
         */
        public static String xorBinaryStrings(String binaryStr1, String binaryStr2) {
            if (binaryStr1.length() != binaryStr2.length()) {
                throw new IllegalArgumentException("Binary strings must have the same length");
            }

            StringBuilder result = new StringBuilder(binaryStr1.length());
            for (int i = 0; i < binaryStr1.length(); i++) {
                // XOR is 1 when bits are different, 0 when they're the same
                result.append(binaryStr1.charAt(i) == binaryStr2.charAt(i) ? '0' : '1');
            }
            return result.toString();
        }

        /**
         * Converts a binary string to human-readable text
         *
         * The method processes the binary string according to the assignment specifications:
         * 1. Finds the last occurrence of '1' (the padding marker)
         * 2. Removes everything after this marker (the padding zeros)
         * 3. Converts each 8-bit chunk to its corresponding ASCII character
         *
         * @param binary The binary string to convert
         * @return The decoded text message
         */
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
                // Convert each 8-bit binary string to its decimal value
                int charCode = Integer.parseInt(byteStr, 2);
                // Convert the decimal value to its ASCII character
                text.append((char) charCode);
            }
            return text.toString();
        }
    }
}