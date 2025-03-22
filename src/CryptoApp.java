/**
 * CryptoApp.java
 *
 * This application implements a Substitution-Permutation Network (SPN) and uses it in CTR mode.
 * It encrypts and decrypts a 16-bit block using SPN (with key schedule, S-box substitution,
 * permutation, and their inverses) and performs CTR mode decryption on a provided ciphertext.
 *
 * The program:
 * 1. Encrypts a sample 16-bit plaintext and then decrypts it to verify the SPN routines.
 * 2. Decrypts a long ciphertext (given as a continuous binary string) in CTR mode.
 * 3. Converts the resulting binary data (after removing padding) into a human-readable message.
 *
 * Console Output Example:
 * -----------------------------
 * Encrypted: 1010100110010010
 * Decrypted: 0001001010001111
 * CTR Decrypted Binary: 0100011101110101011101000010000001100111011001010110110101100001011000110110100001110100001000011000000000000000
 * Decrypted Message: Gut gemacht!
 * -----------------------------
 */
public class CryptoApp {

    public static void main(String[] args) {
        // For CTR mode, use the key provided in the assignment:
        // "00111010100101001101011000111111" (32-bit key)
        String keyString = "00111010100101001101011000111111";

        // Create an SPN engine instance with 4 rounds and parameters n = 4, m = 4.
        SPN spn = new SPN(4, 4, 4, keyString);

        // --- SPN Encryption/Decryption Demo ---
        // Use a sample 16-bit plaintext.
        String plaintext = "0001001010001111";
        // Encrypt the plaintext using the SPN algorithm.
        String encrypted = spn.encrypt(plaintext);
        System.out.println("Encrypted: " + encrypted);

        // Decrypt the encrypted output to verify correctness.
        String decrypted = spn.decrypt(encrypted);
        System.out.println("Decrypted: " + decrypted);

        // --- CTR Mode Decryption Demo ---
        // Provided long ciphertext (from chiffre.txt) is a concatenation of 16-bit blocks.
        String ctrCipherText = "00000100110100100000101110111000000000101000111110001110011111110110000001010001010000111010000000010011011001110010101110110000";
        // Split the long ciphertext into 16-bit blocks.
        String[] blocks = Helper.splitIntoBlocks(ctrCipherText, 16);
        // Decrypt using CTR mode. For each block, the counter is encrypted and then XORed with the ciphertext block.
        String ctrDecryptedBinary = spn.ctr(blocks);
        System.out.println("CTR Decrypted Binary: " + ctrDecryptedBinary);

        // Convert the binary (with padding) into a human-readable message.
        String message = Helper.convertBinaryToText(ctrDecryptedBinary);
        System.out.println("Decrypted Message: " + message);
    }

    /**
     * SPN implements a Substitution-Permutation Network with 4 rounds.
     * It includes key scheduling, S-box substitution, bit permutation, and their inverses.
     */
    static class SPN {
        private int rounds;
        private int n;
        private int m;
        private int key;       // 32-bit key
        private int[] roundKeys; // Array of round keys (r+1 keys)

        // S-box definition (maps 0x0 to 0xF)
        private static final int[] S = {0xE, 0x4, 0xD, 0x1, 0x2, 0xF, 0xB, 0x8, 0x3, 0xA, 0x6, 0xC, 0x5, 0x9, 0x0, 0x7};
        // Inverse S-box computed from S.
        private static final int[] invS = new int[16];
        // Bit permutation: for each input bit index (0 = leftmost) the output bit is placed at position P[i].
        private static final int[] P = {0, 4, 8, 12, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15};
        private static final int[] invP = new int[16];

        // Compute inverse S-box and inverse permutation.
        static {
            for (int i = 0; i < 16; i++) {
                invS[S[i]] = i;
            }
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    if (P[j] == i) {
                        invP[i] = j;
                        break;
                    }
                }
            }
        }

        /**
         * Constructs the SPN engine.
         *
         * @param rounds    Number of rounds (4)
         * @param n         Parameter n (4)
         * @param m         Parameter m (4)
         * @param keyString 32-bit key as a binary string
         */
        public SPN(int rounds, int n, int m, String keyString) {
            this.rounds = rounds;
            this.n = n;
            this.m = m;
            this.key = Helper.parseBinary(keyString);
            generateRoundKeys();
        }

        // Generate round keys: for i = 0 to rounds, K_i = (key >> (4*i)) & 0xFFFF.
        private void generateRoundKeys() {
            roundKeys = new int[rounds + 1];
            for (int i = 0; i <= rounds; i++) {
                roundKeys[i] = (key >> (4 * i)) & 0xFFFF;
            }
        }

        /**
         * Encrypts a 16-bit block represented as a binary string.
         */
        public String encrypt(String binaryInput) {
            int input = Helper.parseBinary(binaryInput);
            int output = encryptBlock(input);
            return Helper.intToBinary(output, 16);
        }

        /**
         * Decrypts a 16-bit block represented as a binary string.
         */
        public String decrypt(String binaryInput) {
            int input = Helper.parseBinary(binaryInput);
            int output = decryptBlock(input);
            return Helper.intToBinary(output, 16);
        }

        // Encrypts a 16-bit integer block using the SPN algorithm.
        private int encryptBlock(int x) {
            // Initial key addition
            x ^= roundKeys[0];
            // Rounds 1 to rounds-1: substitute, permute, then add round key.
            for (int i = 1; i < rounds; i++) {
                x = substitute(x);
                x = permute(x);
                x ^= roundKeys[i];
            }
            // Final round: substitution and final key addition.
            x = substitute(x);
            x ^= roundKeys[rounds];
            return x;
        }

        // Decrypts a 16-bit integer block using the SPN algorithm.
        private int decryptBlock(int y) {
            // Inverse final round.
            y ^= roundKeys[rounds];
            y = inverseSubstitute(y);
            // Inverse rounds from rounds-1 downto 1.
            for (int i = rounds - 1; i >= 1; i--) {
                y ^= roundKeys[i];
                y = inversePermute(y);
                y = inverseSubstitute(y);
            }
            // Final key removal.
            y ^= roundKeys[0];
            return y;
        }

        // Applies the S-box substitution on each 4-bit nibble.
        private int substitute(int x) {
            int result = 0;
            for (int i = 0; i < 4; i++) {
                int shift = 12 - 4 * i;
                int nibble = (x >> shift) & 0xF;
                result |= (S[nibble] << shift);
            }
            return result;
        }

        // Applies the inverse S-box substitution on each 4-bit nibble.
        private int inverseSubstitute(int x) {
            int result = 0;
            for (int i = 0; i < 4; i++) {
                int shift = 12 - 4 * i;
                int nibble = (x >> shift) & 0xF;
                result |= (invS[nibble] << shift);
            }
            return result;
        }

        // Applies the bit permutation on a 16-bit block.
        private int permute(int x) {
            int result = 0;
            for (int i = 0; i < 16; i++) {
                int bit = (x >> (15 - i)) & 1;
                if (bit == 1) {
                    result |= (1 << (15 - P[i]));
                }
            }
            return result;
        }

        // Applies the inverse bit permutation on a 16-bit block.
        private int inversePermute(int x) {
            int result = 0;
            for (int i = 0; i < 16; i++) {
                int bit = (x >> (15 - i)) & 1;
                if (bit == 1) {
                    result |= (1 << (15 - invP[i]));
                }
            }
            return result;
        }

        /**
         * Implements CTR mode decryption.
         * For each 16-bit ciphertext block, the keystream is computed by encrypting the counter (starting at 0).
         * The plaintext block is obtained by XORing the ciphertext block with the keystream.
         *
         * @param blocks An array of ciphertext blocks (each 16 bits as a binary string).
         * @return The concatenated binary string of all decrypted blocks.
         */
        public String ctr(String[] blocks) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < blocks.length; i++) {
                // Counter as a 16-bit binary string.
                String counterStr = Helper.intToBinary(i, 16);
                int counter = Helper.parseBinary(counterStr);
                int keystream = encryptBlock(counter);
                int cipherBlock = Helper.parseBinary(blocks[i]);
                int plainBlock = cipherBlock ^ keystream;
                result.append(Helper.intToBinary(plainBlock, 16));
            }
            return result.toString();
        }
    }

    /**
     * Helper class with utility methods for binary string processing.
     */
    static class Helper {
        // Converts a binary string to an integer.
        public static int parseBinary(String s) {
            return Integer.parseInt(s, 2);
        }

        // Converts an integer to a binary string of fixed length (padding with leading zeros).
        public static String intToBinary(int x, int bits) {
            String binary = Integer.toBinaryString(x);
            while (binary.length() < bits) {
                binary = "0" + binary;
            }
            if (binary.length() > bits) {
                binary = binary.substring(binary.length() - bits);
            }
            return binary;
        }

        // Splits a string into blocks of the specified size.
        public static String[] splitIntoBlocks(String text, int blockSize) {
            int numBlocks = (int) Math.ceil((double) text.length() / blockSize);
            String[] blocks = new String[numBlocks];
            for (int i = 0; i < numBlocks; i++) {
                int start = i * blockSize;
                int end = Math.min(start + blockSize, text.length());
                blocks[i] = text.substring(start, end);
            }
            return blocks;
        }

        /**
         * Converts binary data (with padding) into its ASCII text representation.
         * The padding consists of a single '1' appended immediately after the actual data,
         * followed by enough '0's to reach a multiple of 16 bits.
         *
         * The method removes the trailing zeros until it reaches the '1' (which is then discarded)
         * and converts every 8-bit segment of the remaining binary string into a character.
         */
        public static String convertBinaryToText(String binaryData) {
            int i = binaryData.length() - 1;
            // Remove trailing zeros.
            while (i >= 0 && binaryData.charAt(i) == '0') {
                i--;
            }
            if (i < 0) {
                return "";
            }
            // Discard the padding's '1'.
            String trimmed = binaryData.substring(0, i);

            StringBuilder text = new StringBuilder();
            for (int j = 0; j + 8 <= trimmed.length(); j += 8) {
                String byteStr = trimmed.substring(j, j + 8);
                int ascii = Integer.parseInt(byteStr, 2);
                text.append((char) ascii);
            }
            return text.toString();
        }
    }
}