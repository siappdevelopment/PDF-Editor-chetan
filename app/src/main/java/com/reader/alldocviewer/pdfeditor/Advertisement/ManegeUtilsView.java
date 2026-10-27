package com.reader.alldocviewer.pdfeditor.Advertisement;

import android.content.Context;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ManegeUtilsView {

    public static String ssfsfsfsf;

    public static void isUtilsManege(Context context) {




        ManegeParameter.asadpass = "fffgyjklopiuytre";
        ManegeParameter.sdasdada = "U2FsdGVkX194DXtPBb84sYHOAT8EtGeZec50FPDdisM=";



        try {
            ssfsfsfsf = ManegeParameter.cipherSignatureSpace(ManegeParameter.asadpass, ManegeParameter.sdasdada);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }
}
