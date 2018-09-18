package com.github.junrar.unpack.decode;

public class Decode
{
	private int maxNum;

	private final int[] decodeLen = new int[16];

	private final int[] decodePos = new int[16];

	protected int[] decodeNum = new int[2];

	/**
	 * returns the decode Length array
	 * @return decodeLength
	 */
	public int[] getDecodeLen()
	{
		return decodeLen;
	}

	/**
	 * returns the decode num array
	 * @return decodeNum
	 */
	public int[] getDecodeNum()
	{
		return decodeNum;
	}

	/**
	 * returns the decodePos array
	 * @return decodePos
	 */
	public int[] getDecodePos()
	{
		return decodePos;
	}

	/**
	 * returns the max num
	 * @return maxNum
	 */
	public int getMaxNum()
	{
		return maxNum;
	}

	/**
	 * sets the max num
	 * @param maxNum to be set to maxNum
	 */
	public void setMaxNum(int maxNum)
	{
		this.maxNum = maxNum;
	}

}
