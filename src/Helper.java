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
                bits[i * 4 + j] = (current >> (3 - j)) & 1; // extract the j-th bit and add it to the resulting bit array
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
        int result = (num + number) % (int)Math.pow(2, 16);

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
