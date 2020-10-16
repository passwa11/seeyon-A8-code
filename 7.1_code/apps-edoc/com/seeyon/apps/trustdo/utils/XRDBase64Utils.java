package com.seeyon.apps.trustdo.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Base64工具类
 * @author zhaopeng
 *
 */
public class XRDBase64Utils
{
	private static final XRDBase64Encoder encoder = new XRDBase64Encoder();

	/**
	 * encode the input data producing a base 64 encoded byte array.
	 * 
	 * @return a byte array containing the base 64 encoded data.
	 */
	public static byte[] encodeToByte(byte[] data)
	{
		int len = (data.length + 2) / 3 * 4;
		ByteArrayOutputStream bOut = new ByteArrayOutputStream(len);

		try
		{
			encoder.encode(data, 0, data.length, bOut);
		}
		catch (IOException e)
		{
			throw new RuntimeException("exception encoding base64 string: " + e);
		}

		return bOut.toByteArray();
	}
	
	public static String encodeToString(byte[] inData) {
		if (inData == null || inData.length == 0)
			return null;
		return new String(encodeToByte(inData));
	}


	/**
	 * Encode the byte data to base 64 writing it to the given output stream.
	 * 
	 * @return the number of bytes produced.
	 */
	public static int encode(byte[] data, OutputStream out) throws IOException
	{
		return encoder.encode(data, 0, data.length, out);
	}

	/**
	 * Encode the byte data to base 64 writing it to the given output stream.
	 * 
	 * @return the number of bytes produced.
	 */
	public static int encode(byte[] data, int off, int length, OutputStream out)
			throws IOException
	{
		return encoder.encode(data, off, length, out);
	}

	/**
	 * decode the base 64 encoded input data. It is assumed the input data is
	 * valid.
	 * 
	 * @return a byte array representing the decoded data.
	 */
	public static byte[] decode(byte[] data)
	{
		int len = data.length / 4 * 3;
		ByteArrayOutputStream bOut = new ByteArrayOutputStream(len);

		try
		{
			encoder.decode(data, 0, data.length, bOut);
		}
		catch (IOException e)
		{
			throw new RuntimeException("exception decoding base64 string: " + e);
		}

		return bOut.toByteArray();
	}

	/**
	 * decode the base 64 encoded String data - whitespace will be ignored.
	 * 
	 * @return a byte array representing the decoded data.
	 */
	public static byte[] decode(String data)
	{
		int len = data.length() / 4 * 3;
		ByteArrayOutputStream bOut = new ByteArrayOutputStream(len);

		try
		{
			encoder.decode(data, bOut);
		}
		catch (IOException e)
		{
			throw new RuntimeException("exception decoding base64 string: " + e);
		}

		return bOut.toByteArray();
	}

	/**
	 * decode the base 64 encoded String data writing it to the given output
	 * stream, whitespace characters will be ignored.
	 * 
	 * @return the number of bytes produced.
	 */
	public static int decode(String data, OutputStream out) throws IOException
	{
		return encoder.decode(data, out);
	}
}