package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.fc.xls.Reader.shared;

/**
 * Parses OOXML attribute values that may be booleans ("true"/"false") or numbers ("0"/"1").
 */
public final class OoxmlAttrHelper
{
    private OoxmlAttrHelper()
    {
    }

    public static boolean parseBoolean(String value)
    {
        if (value == null || value.length() == 0)
        {
            return false;
        }
        if ("true".equalsIgnoreCase(value) || "1".equals(value))
        {
            return true;
        }
        if ("false".equalsIgnoreCase(value) || "0".equals(value))
        {
            return false;
        }
        try
        {
            return Integer.parseInt(value) != 0;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    public static short parseShort(String value, short defaultValue)
    {
        if (value == null || value.length() == 0)
        {
            return defaultValue;
        }
        try
        {
            return Short.parseShort(value);
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }
}
