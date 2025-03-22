public class Main {
    public static void main(String[] args) {
        String message = "0001001010001111";
        String cipher = "1010111010110100";
        String ciphertxt = "00000100110100100000101110111000000000101000111110001110011111110110000001010001010000111010000000010011011001110010101110110000";
        SPN spn = new SPN(4, 4, 4);
        String encryptedOutput = spn.encrypt(message);
        System.out.println("Encrypted: " + encryptedOutput);
        String decryptedOutput = spn.decrypt(cipher);
        System.out.println("Decrypted: " + decryptedOutput);
        //String[] cipherBlocks = Helper.splitIntoBlocks(ciphertxt, 16);
        //String ctrDecryptedBinary = spn.ctr(cipherBlocks);
        //System.out.println("CTR Decrypted Binary: " + ctrDecryptedBinary);
        //String readableMessage = Helper.convertBinaryToText(ctrDecryptedBinary);
        //System.out.println("Decrypted Message: " + readableMessage);

    }


public class SPN {
    private static final int[] sBox = {0xE, 4, 0xD, 1, 2, 0xF, 0xB, 8, 3, 0xA, 6, 0xC, 5, 9, 0, 7};
    private static final int[] permutation = {0, 4, 8, 12, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15};
    private final int s;
    private final int n;
    private final int m;
    private int[][] roundKeys;
    private final int rounds;
    private int[] inv;
    private final int[] key = {
        0b0011,
        0b1010,
        0b1001,
        0b0100,
        0b1101,
        0b0110,
        0b0011,
        0b1111
    };

    public SPN(int rounds, int n, int m) {
        this.n = n;
        this.m = m;
        this.s = n * m;
        this.rounds = rounds;
        generateRoundKeys();
    }


    // Two-dimensional array. First index is the round number, second index is the round key
    private void generateRoundKeys() {
        this.roundKeys = new int[rounds + 1][m];
        for (int i = 0; i <= rounds; i++) {
            roundKeys[i] = genRoundKey(i);
        }
    }


    // Whole encryption mechanism
    // First it does the inital encipher step, then calls encipher and finally returns the bitstring of the encoded message
    public String encrypt(String text) {
        int[] message = Helper.splitBinaryString(text);
        message = initialEncipherStep(message);
        message = encipher(1, message);
        message = Helper.fourBitArraytoBinaryArray(message);
        return Helper.intArrayToString(message);
    }


    // Does the decryption
    // It inverses the sbox and does the same as the encipher method just backwards.
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

    public String ctr(String[] ciphers) {
        StringBuilder text = new StringBuilder();
        for (int i = 1; i < ciphers.length; i++) {
            String yi = Helper.binaryStringAddNumber(ciphers[0], i - 1);
            String result = encrypt(yi);
            String res = Helper.xorBinaryStrings(result, ciphers[i]);
            text.append(res);
        }
        return text.toString();
    }



// Courtesy of ChatGPT ;) Only works for n = 4 but could be expanded on
public class Helper {

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
        int[] result = new int[(length + 3) / 4]; // round up to nearest multiple of 4

        // iterate over the binary string in 4-bit chunks, and convert each chunk to a decimal integer
        for (int i = 0; i < length; i += 4) {
            int endIndex = Math.min(i + 4, length); // calculate the end index of the current chunk
            String chunk = binaryString.substring(i, endIndex); // extract the current chunk
            int value = Integer.parseInt(chunk, 2); // convert the chunk to a decimal integer
            result[i / 4] = value; // store the value in the result array
        }
        return result;
    }

    public static int[] fourBitArraytoBinaryArray(int[] message) {
        int[] bits = new int[message.length * 4]; // the resulting bit array

        for (int i = 0; i < message.length; i++) {
            int current = message[i]; // get the current integer
            for (int j = 0; j < 4; j++) {
                bits[i * 4 + j] =
                    (current >> (3 - j)) & 1; // extract the j-th bit and add it to the resulting bit array
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

    public static String[] splitString(String message, int size) {
        int len = message.length();
        int chunkSize = 16;
        String[] chunks = new String[(int) Math.ceil((double) len / chunkSize)];

        for (int i = 0; i < chunks.length; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, len);
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


    public static String binaryStringAddNumber(String binaryStr, int number) {
        // Convert the binary string to an integer
        int num = Integer.parseInt(binaryStr, 2);

        // Add 1 to the integer and take the modulo of 2^16
        int result = (num + number) % (int) Math.pow(2, 16);

        // Convert the resulting integer back to a binary string
        String binaryResult = Integer.toBinaryString(result);

        // Pad the resulting binary string with leading zeros if necessary
        while (binaryResult.length() < binaryStr.length()) {
            binaryResult = "0" + binaryResult;
        }

        return binaryResult;
    }

    public static String xorBinaryStrings(String binaryStr1, String binaryStr2) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < binaryStr1.length(); i++) {
            if (binaryStr1.charAt(i) == binaryStr2.charAt(i)) {
                result.append("0");
            } else {
                result.append("1");
            }
        }
        return result.toString();
    }

    public static String binaryToText(String binary) {
        // Remove padding: find the last occurrence of '1'
        int lastOne = binary.lastIndexOf('1');
        if (lastOne != -1) {
            binary = binary.substring(0, lastOne);
        }

        StringBuilder text = new StringBuilder();
        // Process the binary string in 8-bit chunks
        for (int i = 0; i + 8 <= binary.length(); i += 8) {
            String byteStr = binary.substring(i, i + 8);
            int charCode = Integer.parseInt(byteStr, 2);
            text.append((char) charCode);
        }
        return text.toString();
    }
}
