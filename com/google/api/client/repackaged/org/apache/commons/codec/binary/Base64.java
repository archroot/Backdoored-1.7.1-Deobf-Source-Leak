package com.google.api.client.repackaged.org.apache.commons.codec.binary;

import java.math.BigInteger;

public class Base64 extends BaseNCodec
{
    private static final int BITS_PER_ENCODED_BYTE = 6;
    private static final int BYTES_PER_UNENCODED_BLOCK = 3;
    private static final int BYTES_PER_ENCODED_BLOCK = 4;
    static final byte[] CHUNK_SEPARATOR;
    private static final byte[] STANDARD_ENCODE_TABLE;
    private static final byte[] URL_SAFE_ENCODE_TABLE;
    private static final byte[] DECODE_TABLE;
    private static final int MASK_6BITS = 63;
    private final byte[] encodeTable;
    private final byte[] decodeTable;
    private final byte[] lineSeparator;
    private final int decodeSize;
    private final int encodeSize;
    private int bitWorkArea;
    
    public Base64() {
        this(0);
    }
    
    public Base64(final boolean b) {
        this(76, Base64.CHUNK_SEPARATOR, b);
    }
    
    public Base64(final int n) {
        this(n, Base64.CHUNK_SEPARATOR);
    }
    
    public Base64(final int n, final byte[] array) {
        this(n, array, false);
    }
    
    public Base64(final int n, final byte[] array, final boolean b) {
        super(3, 4, n, (array == null) ? 0 : array.length);
        this.decodeTable = Base64.DECODE_TABLE;
        if (array != null) {
            if (this.containsAlphabetOrPad(array)) {
                throw new IllegalArgumentException("lineSeparator must not contain base64 characters: [" + StringUtils.newStringUtf8(array) + "]");
            }
            if (n > 0) {
                this.encodeSize = 4 + array.length;
                System.arraycopy(array, 0, this.lineSeparator = new byte[array.length], 0, array.length);
            }
            else {
                this.encodeSize = 4;
                this.lineSeparator = null;
            }
        }
        else {
            this.encodeSize = 4;
            this.lineSeparator = null;
        }
        this.decodeSize = this.encodeSize - 1;
        this.encodeTable = (b ? Base64.URL_SAFE_ENCODE_TABLE : Base64.STANDARD_ENCODE_TABLE);
    }
    
    public boolean isUrlSafe() {
        return this.encodeTable == Base64.URL_SAFE_ENCODE_TABLE;
    }
    
    @Override
    void encode(final byte[] array, int n, final int n2) {
        if (this.eof) {
            return;
        }
        if (n2 < 0) {
            this.eof = true;
            if (0 == this.modulus && this.lineLength == 0) {
                return;
            }
            this.ensureBufferSize(this.encodeSize);
            final int pos = this.pos;
            switch (this.modulus) {
                case 1:
                    this.buffer[this.pos++] = this.encodeTable[this.bitWorkArea >> 2 & 0x3F];
                    this.buffer[this.pos++] = this.encodeTable[this.bitWorkArea << 4 & 0x3F];
                    if (this.encodeTable == Base64.STANDARD_ENCODE_TABLE) {
                        this.buffer[this.pos++] = 61;
                        this.buffer[this.pos++] = 61;
                        break;
                    }
                    break;
                case 2:
                    this.buffer[this.pos++] = this.encodeTable[this.bitWorkArea >> 10 & 0x3F];
                    this.buffer[this.pos++] = this.encodeTable[this.bitWorkArea >> 4 & 0x3F];
                    this.buffer[this.pos++] = this.encodeTable[this.bitWorkArea << 2 & 0x3F];
                    if (this.encodeTable == Base64.STANDARD_ENCODE_TABLE) {
                        this.buffer[this.pos++] = 61;
                        break;
                    }
                    break;
            }
            this.currentLinePos += this.pos - pos;
            if (this.lineLength > 0 && this.currentLinePos > 0) {
                System.arraycopy(this.lineSeparator, 0, this.buffer, this.pos, this.lineSeparator.length);
                this.pos += this.lineSeparator.length;
            }
        }
        else {
            for (int i = 0; i < n2; ++i) {
                this.ensureBufferSize(this.encodeSize);
                this.modulus = (this.modulus + 1) % 3;
                int n3 = array[n++];
                if (n3 < 0) {
                    n3 += 256;
                }
                this.bitWorkArea = (this.bitWorkArea << 8) + n3;
                if (0 == this.modulus) {
                    this.buffer[this.pos++] = this.encodeTable[this.bitWorkArea >> 18 & 0x3F];
                    this.buffer[this.pos++] = this.encodeTable[this.bitWorkArea >> 12 & 0x3F];
                    this.buffer[this.pos++] = this.encodeTable[this.bitWorkArea >> 6 & 0x3F];
                    this.buffer[this.pos++] = this.encodeTable[this.bitWorkArea & 0x3F];
                    this.currentLinePos += 4;
                    if (this.lineLength > 0 && this.lineLength <= this.currentLinePos) {
                        System.arraycopy(this.lineSeparator, 0, this.buffer, this.pos, this.lineSeparator.length);
                        this.pos += this.lineSeparator.length;
                        this.currentLinePos = 0;
                    }
                }
            }
        }
    }
    
    @Override
    void decode(final byte[] array, int n, final int n2) {
        if (this.eof) {
            return;
        }
        if (n2 < 0) {
            this.eof = true;
        }
        for (int i = 0; i < n2; ++i) {
            this.ensureBufferSize(this.decodeSize);
            final byte b = array[n++];
            if (b == 61) {
                this.eof = true;
                break;
            }
            if (b >= 0 && b < Base64.DECODE_TABLE.length) {
                final byte b2 = Base64.DECODE_TABLE[b];
                if (b2 >= 0) {
                    this.modulus = (this.modulus + 1) % 4;
                    this.bitWorkArea = (this.bitWorkArea << 6) + b2;
                    if (this.modulus == 0) {
                        this.buffer[this.pos++] = (byte)(this.bitWorkArea >> 16 & 0xFF);
                        this.buffer[this.pos++] = (byte)(this.bitWorkArea >> 8 & 0xFF);
                        this.buffer[this.pos++] = (byte)(this.bitWorkArea & 0xFF);
                    }
                }
            }
        }
        if (this.eof && this.modulus != 0) {
            this.ensureBufferSize(this.decodeSize);
            switch (this.modulus) {
                case 2:
                    this.bitWorkArea >>= 4;
                    this.buffer[this.pos++] = (byte)(this.bitWorkArea & 0xFF);
                    break;
                case 3:
                    this.bitWorkArea >>= 2;
                    this.buffer[this.pos++] = (byte)(this.bitWorkArea >> 8 & 0xFF);
                    this.buffer[this.pos++] = (byte)(this.bitWorkArea & 0xFF);
                    break;
            }
        }
    }
    
    @Deprecated
    public static boolean isArrayByteBase64(final byte[] array) {
        return isBase64(array);
    }
    
    public static boolean isBase64(final byte b) {
        return b == 61 || (b >= 0 && b < Base64.DECODE_TABLE.length && Base64.DECODE_TABLE[b] != -1);
    }
    
    public static boolean isBase64(final String s) {
        return isBase64(StringUtils.getBytesUtf8(s));
    }
    
    public static boolean isBase64(final byte[] array) {
        for (int i = 0; i < array.length; ++i) {
            if (!isBase64(array[i]) && !BaseNCodec.isWhiteSpace(array[i])) {
                return false;
            }
        }
        return true;
    }
    
    public static byte[] encodeBase64(final byte[] array) {
        return encodeBase64(array, false);
    }
    
    public static String encodeBase64String(final byte[] array) {
        return StringUtils.newStringUtf8(encodeBase64(array, false));
    }
    
    public static byte[] encodeBase64URLSafe(final byte[] array) {
        return encodeBase64(array, false, true);
    }
    
    public static String encodeBase64URLSafeString(final byte[] array) {
        return StringUtils.newStringUtf8(encodeBase64(array, false, true));
    }
    
    public static byte[] encodeBase64Chunked(final byte[] array) {
        return encodeBase64(array, true);
    }
    
    public static byte[] encodeBase64(final byte[] array, final boolean b) {
        return encodeBase64(array, b, false);
    }
    
    public static byte[] encodeBase64(final byte[] array, final boolean b, final boolean b2) {
        return encodeBase64(array, b, b2, 0);
    }
    
    public static byte[] encodeBase64(final byte[] array, final boolean b, final boolean b2, final int n) {
        if (array == null || array.length == 0) {
            return array;
        }
        final Base64 base64 = b ? new Base64(b2) : new Base64(0, Base64.CHUNK_SEPARATOR, b2);
        final long encodedLength = base64.getEncodedLength(array);
        if (encodedLength > n) {
            throw new IllegalArgumentException("Input array too big, the output array would be bigger (" + encodedLength + ") than the specified maximum size of " + n);
        }
        return base64.encode(array);
    }
    
    public static byte[] decodeBase64(final String s) {
        return new Base64().decode(s);
    }
    
    public static byte[] decodeBase64(final byte[] array) {
        return new Base64().decode(array);
    }
    
    public static BigInteger decodeInteger(final byte[] array) {
        return new BigInteger(1, decodeBase64(array));
    }
    
    public static byte[] encodeInteger(final BigInteger bigInteger) {
        if (bigInteger == null) {
            throw new NullPointerException("encodeInteger called with null parameter");
        }
        return encodeBase64(toIntegerBytes(bigInteger), false);
    }
    
    static byte[] toIntegerBytes(final BigInteger bigInteger) {
        final int n = bigInteger.bitLength() + 7 >> 3 << 3;
        final byte[] byteArray = bigInteger.toByteArray();
        if (bigInteger.bitLength() % 8 != 0 && bigInteger.bitLength() / 8 + 1 == n / 8) {
            return byteArray;
        }
        int n2 = 0;
        int length = byteArray.length;
        if (bigInteger.bitLength() % 8 == 0) {
            n2 = 1;
            --length;
        }
        final int n3 = n / 8 - length;
        final byte[] array = new byte[n / 8];
        System.arraycopy(byteArray, n2, array, n3, length);
        return array;
    }
    
    @Override
    protected boolean isInAlphabet(final byte b) {
        return b >= 0 && b < this.decodeTable.length && this.decodeTable[b] != -1;
    }
    
    static {
        CHUNK_SEPARATOR = new byte[] { 13, 10 };
        STANDARD_ENCODE_TABLE = new byte[] { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47 };
        URL_SAFE_ENCODE_TABLE = new byte[] { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 45, 95 };
        DECODE_TABLE = new byte[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, 62, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51 };
    }
}
