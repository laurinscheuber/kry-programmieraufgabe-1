public class Main {
    public static void main(String[] args) {
        String message = "0001001010001111";
        String cipher = "1010111010110100";
        String ciphertxt =
            "00000100110100100000101110111000000000101000111110001110011111110110000001010001010000111010000000010011011001110010101110110000";
        SPN spn = new SPN(4, 4, 4);
        String encryptedOutput = spn.encrypt(message);
        System.out.println("Encrypted: " + encryptedOutput);
        String decryptedOutput = spn.decrypt(cipher);
        System.out.println("Decrypted: " + decryptedOutput);
        String[] cipherBlocks = Helper.splitString(ciphertxt, 16);
        String ctrDecryptedBinary = spn.ctr(cipherBlocks);
        System.out.println("CTR Decrypted Binary: " + ctrDecryptedBinary);
        String readableMessage = Helper.binaryToText(ctrDecryptedBinary);
        System.out.println("Decrypted Message: " + readableMessage);

    }

}