/*
 * This class is taken from Geotool and used with very little
 * modifications.  since Spice is LGLP and hereby the code is made
 * available, the LGPL license criteria should be fulfilled
 * Andreas Prlic ap3@sanger.ac.uk
 *
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.biojava.spice.panel ;

// Logging
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

// Table model
import javax.swing.table.TableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.EventListenerList;
import java.awt.EventQueue;

// Collections
import java.util.Map;
import java.util.LinkedHashMap;

// Formatting
import java.util.Date;
import java.text.DateFormat;

// by AP
// Resources
//import org.geotools.resources.XArray;
//import org.geotools.resources.gui.Resources;
//import org.geotools.resources.gui.ResourceKeys;


/**
 * A logging {@link Handler} storing {@link LogRecords} as a {@link TableModel}.
 * This model is used by {@link LoggingPanel} for displaying logging messages in
 * a {@link javax.swing.JTable}.
 *
 * @version $Id: LoggingTableModel.java,v 1.7 2003/06/25 12:58:06 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class LoggingTableModel extends Handler implements TableModel {
    public static final int LOGGER                   =    34;
    public static final int CLASS                    =    33;
    public static final int METHOD                   =    35;
    public static final int TIME_OF_DAY              =    36;
    public static final int LEVEL                    =    31;
    public static final int MESSAGE                  =    32;
    /**
     * Resource keys for default column names. <STRONG>NOTE: Order is significant.</STRONG>
     * If the order is changed, then the constants in {@link LoggingPanel} must be updated.
     */
    private static final int[] COLUMN_NAMES = new int[] {
        LOGGER,
        CLASS,
        METHOD,
        TIME_OF_DAY,
        LEVEL,
        MESSAGE
    };

    /**
     * Resource keys for column names. This is usuall the same array than <code>COLUMN_NAMES</code>.
     * However, method {@link #setColumnVisible} may add or remove column in this list.
     */
    private int[] columnNames = COLUMN_NAMES;

    /**
     * The last {@link LogRecord}s stored. This array will grows as needed up to
     * {@link #capacity}. Once the maximal capacity is reached, early records
     * are discarted.
     */
    private LogRecord[] records = new LogRecord[16];

    /**
     * The maximum amount of records that can be stored in this logging panel.
     * If more than {@link #capacity} messages are logged, early messages will
     * be discarted.
     */
    private int capacity = 500;

    /**
     * The total number of logging messages published by this panel. This number may be
     * greater than the amount of {@link LogRecord} actually memorized, since early records
     * may have been discarted. The slot in <code>records</code> where to write the next
     * message can be computed by <code>recordCount % capacity</code>.
     */
    private int recordCount;

    /**
     * String representations of latest required records. Keys are {@link LogRecord} objects
     * and values are <code>String[]</code>. This is a cache for faster rendering.
     */
    private final Map cache = new LinkedHashMap() {
        private static final long serialVersionUID = 4689139871234987390L;
        protected boolean removeEldestEntry(final Map.Entry eldest) {
            return size() >= Math.min(capacity, 80);
        }
    };

    /**
     * The list of registered listeners.
     */
    private final EventListenerList listenerList = new EventListenerList();

    /**
     * The format to use for formatting time.
     */
    private final DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);

    /**
     * Construct the handler.
     */
    public LoggingTableModel() {
        //setLevel(Level.CONFIG);
        setFormatter(new SimpleFormatter());
    }

    /**
     * Returns the capacity. This is the maximum number of {@link LogRecord}s this handler
     * can memorize. If more messages are logged, then the oldiest messages will be discarted.
     * @return int the capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Set the capacity. This is the maximum number of {@link LogRecord}s this handler can
     * memorize. If more messages are logged, then the oldiest messages will be discarted.
     *  @param capacity
     *  */
    public synchronized void setCapacity(final int capacity) {
        if (recordCount != 0) {
            throw new IllegalStateException("Not yet implemented.");
        }
        this.capacity = capacity;
    }

    /**
     * Returns <code>true</code> if the given column is visible.
     *
     * @param index One of {@link LoggingPanel} constants, which maps to entries in
     *        {@link COLUMN_NAMES}. For example <code>0</code> for the logger,
     *        <code>1</code> for the class, etc.
     */
    final boolean isColumnVisible(int index) {
        final int key = COLUMN_NAMES[index];
        for (int i=0; i<columnNames.length; i++) {
            if (columnNames[i] == key) {
                return true;
            }
        }
        return false;
    }


     private static Object resizeArray(final Object array, final int length) {
        final int current = array == null ? 0 : java.lang.reflect.Array.getLength(array);
        if (current != length) {
            final Object newArray=java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), length);
            System.arraycopy(array, 0, newArray, 0, Math.min(current, length));
            return newArray;
        } else {
            return array;
        }
    }

    /**
     * Show or hide the given column.
     *
     * @param index One of {@link LoggingPanel} constants, which maps to entries in
     *        {@link COLUMN_NAMES}. For example <code>0</code> for the logger,
     *        <code>1</code> for the class, etc.
     * @param visible The visible state for the specified column.
     */
    final void setColumnVisible(final int index, final boolean visible) {
        final int key = COLUMN_NAMES[index];
        int[] names = new int[COLUMN_NAMES.length];
        int count = 0;
        for (int i=0; i<COLUMN_NAMES.length; i++) {
            final int toTest = COLUMN_NAMES[i];
            if (toTest == key) {
                if (visible) {
                    names[count++] = toTest;
                }
                continue;
            }
            for (int j=0; j<columnNames.length; j++) {
                if (columnNames[j] == toTest) {
                    names[count++] = toTest;
                    break;
                }
            }
        }
        columnNames = names = (int[])resizeArray(names, count);
        cache.clear();
        fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
        assert isColumnVisible(index) == visible : visible;
    }
    
    /**
     * Publish a {@link LogRecord}. If the maximal capacity has been reached,
     * the oldiest record will be discarted.
     */
    public synchronized void publish(final LogRecord record) {
	//System.out.println("LoggingTable publish");
	if (!isLoggable(record)) {
	    return;
	}
        final int nextSlot = recordCount % capacity;
        if (nextSlot >= records.length) {
            records = (LogRecord[]) resizeArray(records, Math.min(records.length*2, capacity));
        }
        records[nextSlot] = record;
        final TableModelEvent event;
        if (++recordCount <= capacity) {
            event = new TableModelEvent(this, nextSlot, nextSlot,
                                        TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
        } else {
            event = new TableModelEvent(this, 0, capacity-1,
                                        TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
        }
        //
        // Notify all listeners that a record has been added.
        //
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                fireTableChanged(event);
            }
        });
    }

    /**
     * Returns the log record for the specified row.
     *
     * @param row The row in the table. This is the visible row,
     *            not the record number from the first record.
     *  @return the LogRecord object          
     */
    public synchronized LogRecord getLogRecord(int row) {
	//System.out.println("LoggingTableModel getLogRecord");
        assert row < getRowCount();
        if (recordCount > capacity) {
            row += (recordCount % capacity);
            row %= capacity;
        }
        return records[row];
    }

    /** clear all log records */
    
    public synchronized void clearRecords() {
	records = new LogRecord[16];
	recordCount = 0 ;
	
    }

    /**
     * Returns the number of columns in the model.
     */
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Returns the number of rows in the model.
     */
    public synchronized int getRowCount() {
        return Math.min(recordCount, capacity);
    }

    /**
     * Returns the most specific superclass for all the cell values in the column.
     */
    public Class getColumnClass(final int columnIndex) {
        return String.class;
    }

    /**
     * Returns the name of the column at <code>columnIndex</code>.
     */
    public String getColumnName(final int columnIndex) {
	switch (columnNames[columnIndex]) {
	case LOGGER:      return "Logger";  
	case CLASS:       return "Class";   
	case METHOD:      return "Method";  
	case TIME_OF_DAY: return "Time" ;   
	case LEVEL:       return "Level" ;  
	case MESSAGE:     return "Message"; 
	default:          throw new AssertionError(columnIndex);
	}

    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and <code>rowIndex</code>.
     */ 
    public synchronized Object getValueAt(final int rowIndex, final int columnIndex) {
	//System.out.println("LoggingTableModel getValue at");
        final LogRecord record = getLogRecord(rowIndex);
        String[] row = (String[]) cache.get(record);
        if (row == null) {
            row = new String[getColumnCount()];
            for (int i=0; i<row.length; i++) {
                final String value;
                switch (columnNames[i]) {
                    case LOGGER:      value=record.getLoggerName();                          break;
                    case CLASS:       value=getShortClassName(record.getSourceClassName());  break;
                    case METHOD:      value=record.getSourceMethodName();                    break;
                    case TIME_OF_DAY: value=dateFormat.format(new Date(record.getMillis())); break;
                    case LEVEL:       value=record.getLevel().getLocalizedName();            break;
                    case MESSAGE:     value=getFormatter().formatMessage(record);            break;
                    default:                       throw new AssertionError(i);
                }
                row[i] = value;
            }
            cache.put(record, row);
            assert cache.size() <= capacity;
        }
        return row[columnIndex];
    }

    /**
     * Returns the class name in a shorter form (without package).
     */
    private static String getShortClassName(String name) {
        if (name != null) {
            final int dot = name.lastIndexOf('.');
            if (dot >= 0) {
                name = name.substring(dot+1);
            }
            name = name.replace('$','.');
        }
        return name;
    }
    
    /**
     * Do nothing since cells are not editable.
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    /**
     * Returns <code>false</code> since cells are not editable.
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    /**
     * Adds a listener that is notified each time a change to the data model occurs.
     */
    public void addTableModelListener(final TableModelListener listener) {
        listenerList.add(TableModelListener.class, listener);
    }

    /**
     * Removes a listener from the list that is notified each time a change occurs.
     */
    public void removeTableModelListener(final TableModelListener listener) {
        listenerList.remove(TableModelListener.class, listener);
    }

    /**
     * Forwards the given notification event to all {@link TableModelListeners}.
     */
    private void fireTableChanged(final TableModelEvent event) {
        final Object[] listeners = listenerList.getListenerList();
        for (int i=listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TableModelListener.class) {
                ((TableModelListener)listeners[i+1]).tableChanged(event);
            }
        }
    }

    /**
     * Flush any buffered output.
     */
    public void flush() {
    }

    /**
     * Close the <code>Handler</code> and free all associated resources.
     */
    public void close() {
    }
}
