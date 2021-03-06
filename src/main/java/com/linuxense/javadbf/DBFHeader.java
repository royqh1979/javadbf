/*

(C) Copyright 2015 Alberto Fernández <infjaf@gmail.com>
(C) Copyright 2014 Jan Schlößin
(C) Copyright 2004 Anil Kumar K <anil@linuxense.com>

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3.0 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library.  If not, see <http://www.gnu.org/licenses/>.

*/


package com.linuxense.javadbf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.nio.charset.Charset;

/**
 * Class for reading the metadata assuming that the given InputStream carries
 * DBF data.
 */
class DBFHeader {

	public static final byte SIG_DBASE_III = (byte) 0x03;
	/* DBF structure start here */
	
	private byte signature;              /* 0 */
	private byte year;                   /* 1 */
	private byte month;                  /* 2 */
	private byte day;                    /* 3 */
	int numberOfRecords;         /* 4-7 */
	short headerLength;          /* 8-9 */
	short recordLength;          /* 10-11 */
	private short reserv1;               /* 12-13 */
	private byte incompleteTransaction;  /* 14 */
	private byte encryptionFlag;         /* 15 */
	private int freeRecordThread;        /* 16-19 */
	private int reserv2;                 /* 20-23 */
	private int reserv3;                 /* 24-27 */
	private byte mdxFlag;                /* 28 */
	private byte languageDriver;         /* 29 */
	private short reserv4;               /* 30-31 */
	DBFField []fieldArray;       /* each 32 bytes */	
	private byte terminator1;            /* n+1 */

	//byte[] databaseContainer; /* 263 bytes */
	/* DBF structure ends here */

	protected DBFHeader() {
		this.signature = SIG_DBASE_III;
		this.terminator1 = 0x0D;
	}

	void read( DataInput dataInput, Charset charset) throws IOException {

		this.signature = dataInput.readByte(); /* 0 */
		this.year = dataInput.readByte();      /* 1 */
		this.month = dataInput.readByte();     /* 2 */
		this.day = dataInput.readByte();       /* 3 */
		this.numberOfRecords = Utils.readLittleEndianInt( dataInput); /* 4-7 */

		this.headerLength = Utils.readLittleEndianShort( dataInput); /* 8-9 */
		this.recordLength = Utils.readLittleEndianShort( dataInput); /* 10-11 */

		this.reserv1 = Utils.readLittleEndianShort( dataInput);      /* 12-13 */
		this.incompleteTransaction = dataInput.readByte();           /* 14 */
		this.encryptionFlag = dataInput.readByte();                  /* 15 */
		this.freeRecordThread = Utils.readLittleEndianInt( dataInput); /* 16-19 */
		this.reserv2 = dataInput.readInt();                            /* 20-23 */
		this.reserv3 = dataInput.readInt();                            /* 24-27 */
		this.mdxFlag = dataInput.readByte();                           /* 28 */
		this.languageDriver = dataInput.readByte();                    /* 29 */
		this.reserv4 = Utils.readLittleEndianShort( dataInput);        /* 30-31 */

		List<DBFField> v_fields = new ArrayList<>();
		
		DBFField field = null; /* 32 each */
		while ((field = DBFField.createField(dataInput,charset))!= null) {
			v_fields.add(field);
		}		
		this.fieldArray = v_fields.toArray(new DBFField[v_fields.size()]);		
	}

	void write(DataOutput dataOutput, Charset charset) throws IOException {
		dataOutput.writeByte(this.signature); /* 0 */

		GregorianCalendar calendar = new GregorianCalendar();
		this.year = (byte) (calendar.get(Calendar.YEAR) - 1900);
		this.month = (byte) (calendar.get(Calendar.MONTH) + 1);
		this.day = (byte) (calendar.get(Calendar.DAY_OF_MONTH));

		dataOutput.writeByte(this.year); /* 1 */
		dataOutput.writeByte(this.month); /* 2 */
		dataOutput.writeByte(this.day); /* 3 */

		this.numberOfRecords = Utils.littleEndian(this.numberOfRecords);
		dataOutput.writeInt(this.numberOfRecords); /* 4-7 */

		this.headerLength = findHeaderLength();
		dataOutput.writeShort(Utils.littleEndian(this.headerLength)); /* 8-9 */

		this.recordLength = sumUpLenghtOfFields();
		dataOutput.writeShort(Utils.littleEndian(this.recordLength)); /* 10-11 */

		dataOutput.writeShort(Utils.littleEndian(this.reserv1)); /* 12-13 */
		dataOutput.writeByte(this.incompleteTransaction); /* 14 */
		dataOutput.writeByte(this.encryptionFlag); /* 15 */
		dataOutput.writeInt(Utils.littleEndian(this.freeRecordThread));/* 16-19 */
		dataOutput.writeInt(Utils.littleEndian(this.reserv2)); /* 20-23 */
		dataOutput.writeInt(Utils.littleEndian(this.reserv3)); /* 24-27 */

		dataOutput.writeByte(this.mdxFlag); /* 28 */
		dataOutput.writeByte(this.languageDriver); /* 29 */
		dataOutput.writeShort(Utils.littleEndian(this.reserv4)); /* 30-31 */
		for (DBFField field : this.fieldArray) {
			field.write(dataOutput,charset);
		}
		dataOutput.writeByte(this.terminator1); /* n+1 */
	}

	private short findHeaderLength() {

		return (short)(
		1+
		3+
		4+
		2+
		2+
		2+
		1+
		1+
		4+
		4+
		4+
		1+
		1+
		2+
		(32*this.fieldArray.length)+
		1
		);
	}

	private short sumUpLenghtOfFields() {
		int sum = 0;
		for (DBFField field : this.fieldArray) {
			sum += field.getFieldLength();
		}
		return (short) (sum + 1);
	}
	
	public int getYear() {
		return 1900 + this.year;
	}
	public int getMonth() {
		return this.month;
	}
	public int getDay() {
		return this.day;
	}
	
}
