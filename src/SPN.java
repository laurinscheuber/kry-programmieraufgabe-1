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
}
