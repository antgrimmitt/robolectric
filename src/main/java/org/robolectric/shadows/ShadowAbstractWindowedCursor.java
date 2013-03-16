package org.robolectric.shadows;

import android.database.AbstractWindowedCursor;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

import java.sql.Clob;
import java.sql.SQLException;

@Implements(value = AbstractWindowedCursor.class, callThroughByDefault = true)
public class ShadowAbstractWindowedCursor extends ShadowAbstractCursor {
    @Implementation
    public void checkPosition() {
    }

    @Implementation
    public byte[] getBlob(int columnIndex) {
        checkPosition();
        return (byte[]) this.currentRow.get(getColumnNames()[columnIndex]);
    }

    @Implementation
    public String getString(int columnIndex) {
        checkPosition();
        Object value = this.currentRow.get(getColumnNames()[columnIndex]);
        if (value instanceof Clob) {
            try {
                return ((Clob) value).getSubString(1, (int)((Clob) value).length());
            } catch (SQLException x) {
                throw new RuntimeException(x);
            }
        } else {
            return (String)value;
        }
    }

    @Implementation
    public short getShort(int columnIndex) {
        checkPosition();
        Object o =this.currentRow.get(getColumnNames()[columnIndex]);
        if (o==null) return 0;
        return new Short(o.toString());
    }

    @Implementation
    public int getInt(int columnIndex) {
        checkPosition();
        Object o =this.currentRow.get(getColumnNames()[columnIndex]);
        if (o==null) return 0;
        return new Integer(o.toString());
    }

    @Implementation
    public long getLong(int columnIndex) {
        checkPosition();
        Object o =this.currentRow.get(getColumnNames()[columnIndex]);
        if (o==null) return 0;
        return new Long(o.toString());
    }

    @Implementation
    public float getFloat(int columnIndex) {
        checkPosition();
        Object o =this.currentRow.get(getColumnNames()[columnIndex]);
        if (o==null) return 0;
        return new Float(o.toString());

    }

    @Implementation
    public double getDouble(int columnIndex) {
        checkPosition();
        Object o =this.currentRow.get(getColumnNames()[columnIndex]);
        if (o==null) return 0;
        return new Double(o.toString());
    }

    @Implementation
    public boolean isNull(int columnIndex) {
        Object o = this.currentRow.get(getColumnNames()[columnIndex]);
        return o == null;
    }
}
