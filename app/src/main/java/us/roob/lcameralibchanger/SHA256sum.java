package us.roob.lcameralibchanger;
/*MD256sum provides several methods for md256 standards
 *Copyright (C) 2013  Adam Outler
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
 *
 *  https://code.google.com/p/android-casual/source/browse/trunk/CASUALcore/src/CASUAL/crypto/SHA256sum.java
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * provides several methods for md256 standards
 *
 * @author Adam Outler adamoutler@gmail.com
 */
/**
 * attempts to replicates linux's sha256sum.. there appears to be a problem with
 * *Linux* when tested against test vectors from this page:
 * http://www.nsrl.nist.gov/testdata/ I will need to review all data and figure
 * out how to implement this later
 *
 *
 * ad5f9292c7bd44068b5465b48b38bf18c98b4d133e80307957e5f5c372a36f7d logo.xcf
 *
 * @author Adam Outler adamoutler@gmail.com
 */
public class SHA256sum {

    final ByteArrayInputStream toBeSHA256;

    /**
     * spacer used to separate SHA256 and filename in standard sha256sum
     */
    final protected static String LINUXSPACER = "  ";

    /**
     * constructor to make an SHA256 from a string
     *
     * @param s string to sha256
     * @throws IOException {@inheritDoc}
     */
    public SHA256sum(String s) throws IOException {
        ByteArrayInputStream bas = new ByteArrayInputStream(s.getBytes());
        toBeSHA256 = bas;
        toBeSHA256.mark(0);
    }

    /**
     * constructor to make an SHA256 from an InputStream
     *
     * @param is inputstream to sha256
     * @throws IOException {@inheritDoc}
     */
    public SHA256sum(InputStream is) throws IOException {

        byte[] buff = new byte[8120];
        int bytesRead;
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        while ((bytesRead = is.read(buff)) != -1) {
            bao.write(buff, 0, bytesRead);
        }
        ByteArrayInputStream bin = new ByteArrayInputStream(bao.toByteArray());
        toBeSHA256 = bin;
        toBeSHA256.mark(0);
    }

    /**
     * constructor to sha256 a file
     *
     * @param f file to digest
     * @throws FileNotFoundException {@inheritDoc}
     * @throws IOException {@inheritDoc}
     */
    public SHA256sum(File f) throws FileNotFoundException, IOException {

        RandomAccessFile ra;
        ra = new RandomAccessFile(f, "rw");
        byte[] b = new byte[(int) f.length()];
        ra.read(b);
        ByteArrayInputStream bas = new ByteArrayInputStream(b);
        toBeSHA256 = bas;
        toBeSHA256.mark(0);
    }

    /**
     * returns SHA256 sum in standard linux command line format
     *
     * @param filename to use for filename
     * @return linux sha256sum output
     */
    public String getLinuxSum(String filename) {
        if (filename.isEmpty()) {
            filename = "-";
        }
        try {
            String sha = getSha256();
            return sha + LINUXSPACER + filename;
        } catch (IOException ex) {
            return null;
        } catch (NoSuchAlgorithmException ex) {
            return null;
        }

    }

    /**
     * returns SHA256 sum in standard linux command line format
     *
     * @param file to use for filename
     * @return linux sha256sum output
     */
    public static String getLinuxSum(File file) {
        String name = file.getName();
        String sum;

        try {
            sum = new SHA256sum(file).getSha256();
            String linuxSHA256;
            linuxSHA256 = formatLinuxOutputSHA256Sum(sum, name);
            return linuxSHA256;
        } catch (FileNotFoundException ex) {
            return "";
        } catch (NoSuchAlgorithmException ex) {
            return "";
        } catch (IOException ex) {
            return "";
        }
    }

    /**
     * gets the filename from a commandline sha256sum output
     *
     * @param sha256sum linux sha256sum to extract name from
     * @return name of file mentioned in sha256sum
     */
    public static String getName(String sha256sum) {
        if (sha256sum.contains(LINUXSPACER)) {
            String[] split = sha256sum.split(LINUXSPACER);
            return split[1];
        }
        return "";
    }

    /**
     * gets the sha256sum portion of a commandline sha256 output
     *
     * @param sha256sum linux sha256sum to extract sum from
     * @return sum portion of command line sha256 output
     */
    public static String getSum(String sha256sum) {
        if (sha256sum.contains(LINUXSPACER)) {
            String[] split = sha256sum.split(LINUXSPACER);
            return split[0];
        }
        return "";
    }

    /**
     * does the SHA256
     *
     * @return hex string representation of the input
     * @throws IOException {@inheritDoc}
     * @throws NoSuchAlgorithmException {@inheritDoc}
     */
    public String getSha256() throws IOException, NoSuchAlgorithmException {
        toBeSHA256.reset();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] buffer = new byte[8192];
        int read;
        while ((read = toBeSHA256.read(buffer)) > 0) {
            digest.update(buffer, 0, read);
        }
        byte[] md5sum = digest.digest();
        BigInteger bigInt = new BigInteger(1, md5sum);
        String output = bigInt.toString(16);
        while (output.length() != 64) {
            output = "0" + output;
        }
        return output;

    }

    /**
     * converts a byte array to hexadecimal output
     *
     * @param bytes to be turned into hex
     * @return hex string from bytes
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * formats a sha256sum from a sum and a filename
     *
     * @param sum the sha256 sum
     * @param name the file name
     * @return equal to command line output from linux sha256sum command
     */
    public static String formatLinuxOutputSHA256Sum(String sum, String name) {
        String linuxSHA256;
        linuxSHA256 = sum + LINUXSPACER + name;
        return linuxSHA256;
    }

    @Override
    public String toString() {
        String sum = "INVALID000000000000000000000000000000000000000000000000000000000";
        try {
            sum = getSha256();
        } catch (IOException ex) {
            Logger.getLogger(SHA256sum.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SHA256sum.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sum;
    }
}

