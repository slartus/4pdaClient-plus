package org.softeg.sqliteannotations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by slartus on 25.02.14.
 */
public class BaseDao<T> {

    private String mTableName;
    private Class<T> tClass;
    /**
     * The application context.
     */
    protected Context mContext;
    /**
     * Keep the static connection to database.
     */
    protected SQLiteDatabase mDB;

    /**
     * The default constructor.
     */
    public BaseDao(Context context, SQLiteDatabase db, String tableName, Class<T> tClass) {
        mContext = context;

        mDB = db;
        mTableName = tableName.replaceAll("[-/]","_");

        this.tClass = tClass;
    }

    public void createTable(SQLiteDatabase db) throws Exception {
        StringBuffer sql = new StringBuffer("drop table if exists '");
        sql.append(getTableName(tClass));
        sql.append("'; ");
        db.execSQL(sql.toString());

        sql = new StringBuffer("CREATE TABLE '");
        sql.append(getTableName(tClass));

        sql.append("' (");
        sql.append(getPrimaryKeyAnnotation());
        for (Field field : getDeclaredFields(tClass)) {
            Column fieldEntityAnnotation = field.getAnnotation(Column.class);
            if (fieldEntityAnnotation == null) continue;
            if (fieldEntityAnnotation.isPrimaryKey()) continue;

            sql.append(",'");
            sql.append(fieldEntityAnnotation.name());
            sql.append("' ");
            sql.append(fieldEntityAnnotation.type());

        }
        sql.append(");");
        db.execSQL(sql.toString());
    }

    public boolean isTableExists() {
        Cursor cursor = mDB.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + getTableName(tClass) + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    /**
     * Get record by specified id.
     *
     * @param id specified id
     * @return the cursor
     */
    public Cursor get(String id) {
        Cursor cursor = null;
        StringBuffer sql = new StringBuffer();
        sql.append(" select * ");
        sql.append(" from " + getTableName(tClass));
        sql.append(" where _id = ?");
        cursor = mDB.rawQuery(sql.toString(), null);
        return cursor;
    }

    /**
     * Get all data in a specified table.
     *
     * @return array list keeps all data of table
     */
    public Collection<? extends T> getAll() throws IllegalAccessException, InstantiationException, NoSuchFieldException {

        if (mDB == null) {
            return null;
        }

        ArrayList<T> items = new ArrayList<T>();
        Cursor cursor = null;

        try {
            synchronized (mDB) {
                cursor = getAllByCursor();
            }
            // convert cursor to list items.
            if (cursor != null && cursor.getCount() > 0) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    T newTObject;

                    newTObject = tClass.newInstance();
                    bindObject(newTObject, cursor);
                    items.add(newTObject);

                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return items;
    }

    /**
     * Get all data of specified table and return in a cursor.
     *
     * @return the cursor keeps all data of table
     */
    public Cursor getAllByCursor() {
        return mDB.query(getTableName(tClass), null, null, null, null, null,
                null);
    }

    /**
     * Check whether a specified record exist or not
     *
     * @param id specified id of record wants to check.
     * @return return true if specified data exist.
     */
    public boolean isExistence(final String id) {
        Cursor cursor = get(id);
        if (cursor.getCount() > 0) {
            // Close cursor after using
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    /**
     * Delete specified record by id.
     *
     * @param id specified id of record wants to delete.
     * @return true if delete successfully
     */
    public void delete(final String id) {
        StringBuffer whereClause = new StringBuffer(getPrimaryKeyColumnName());
        whereClause.append("='");
        whereClause.append(id);
        whereClause.append("'");
        mDB.delete(getTableName(tClass), whereClause.toString(), null);
    }

    private String getPrimaryKeyColumnName() {
        for (Field field : getDeclaredFields(tClass)) {
            Column fieldEntityAnnotation = field.getAnnotation(Column.class);
            if (fieldEntityAnnotation != null) {
                String columnName = getColumnName(field);
                if (columnName != null) {
                    Column annotationColumn = field.getAnnotation(Column.class);
                    if (annotationColumn.isPrimaryKey()) {
                        return columnName;
                    }
                }
            }
        }
        return "_id";
    }

    public static Collection<Field> getDeclaredFields(Class<?> clazz) {
        Map<String, Field> fields = new HashMap<String, Field>();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!fields.containsKey(field.getName())) {
                    fields.put(field.getName(), field);
                }
            }

            clazz = clazz.getSuperclass();
        }


        return fields.values();
    }

    private String getPrimaryKeyAnnotation() throws Exception {

        for (Field field : getDeclaredFields(tClass)) {
            Column fieldEntityAnnotation = field.getAnnotation(Column.class);
            if (fieldEntityAnnotation == null) continue;
            String columnName = getColumnName(field);
            if (columnName == null) continue;
            Column annotationColumn = field.getAnnotation(Column.class);
            if (!annotationColumn.isPrimaryKey()) continue;
            StringBuffer sql = new StringBuffer();
            sql.append(columnName);
            sql.append(" ");
            sql.append(annotationColumn.type());
            sql.append(" PRIMARY KEY ");
            if (annotationColumn.isAutoincrement())
                sql.append("AUTOINCREMENT");

            return sql.toString();
        }

        throw new Exception("Не задан PRIMARY KEY");
    }

    public long insert(T item) {
        if (item != null) {
            try {
                ContentValues value = getFilledContentValues(item);
                long id = mDB.insertOrThrow(getTableName(tClass), null, value);
                return id;
            } catch (IllegalAccessException e){
                Log.e("insert",e.getMessage());
            }
        }
        return -1;
    }

    /**
     * Delete all data of table.
     */
    public final void deleteAll() {
        mDB.delete(getTableName(tClass), null, null);
    }

    public final void update(String id) {
    }

    /**
     * Update data of specified item.
     *
     * @param object object wants to update
     */
    public void update(T object, String id) throws IllegalAccessException {
        if (!isTableExists()) return;
        ContentValues values = getFilledContentValues(object);
        mDB.update(getTableName(tClass), values, getPrimaryKeyColumnName() + "=?", new String[]{id});
    }

    /**
     * Get record by specified id.
     *
     * @param id specified id
     */
    public T getItem(String id) {
        Cursor cursor = get(id);
        // convert cursor to list items.
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            T newTObject = null;
            try {
                newTObject = tClass.newInstance();
                bindObject(newTObject, cursor);
            } catch (InstantiationException e) {
            } catch (IllegalAccessException e) {
            } catch (NoSuchFieldException e) {
            }
            cursor.close();
            cursor = null;
            return newTObject;
        }
        return null;
    }

    private void bindObject(T newTObject, Cursor cursor)
            throws NoSuchFieldException, IllegalAccessException {
        for (Field field :getDeclaredFields(tClass)) {
            if (!field.isAccessible())
                field.setAccessible(true); // for private variables
            Column fieldEntityAnnotation = field.getAnnotation(Column.class);
            if (fieldEntityAnnotation != null) {
                field.set(newTObject, getValueFromCursor(cursor, field));
            }
        }
    }

    // Get content from specific types
    private Object getValueFromCursor(Cursor cursor, Field field)
            throws IllegalAccessException {
        Class<?> fieldType = field.getType();
        Object value = null;
        int columnIndex = cursor.getColumnIndex(getColumnName(field));

        if (fieldType.isAssignableFrom(Long.class)
                || fieldType.isAssignableFrom(long.class)) {
            value = cursor.getLong(columnIndex);
        } else if (fieldType.isAssignableFrom(String.class) ||
                fieldType.isAssignableFrom(CharSequence.class)) {
            value = cursor.getString(columnIndex);
        } else if ((fieldType.isAssignableFrom(Integer.class) || fieldType
                .isAssignableFrom(int.class))) {
            value = cursor.getInt(columnIndex);
        } else if ((fieldType.isAssignableFrom(Byte[].class) || fieldType
                .isAssignableFrom(byte[].class))) {
            value = cursor.getBlob(columnIndex);
        } else if ((fieldType.isAssignableFrom(Double.class) || fieldType
                .isAssignableFrom(double.class))) {
            value = cursor.getDouble(columnIndex);
        } else if ((fieldType.isAssignableFrom(Float.class) || fieldType
                .isAssignableFrom(float.class))) {
            value = cursor.getFloat(columnIndex);
        } else if ((fieldType.isAssignableFrom(Short.class) || fieldType
                .isAssignableFrom(short.class))) {
            value = cursor.getShort(columnIndex);
        } else if (fieldType.isAssignableFrom(Byte.class)
                || fieldType.isAssignableFrom(byte.class)) {
            value = (byte) cursor.getShort(columnIndex);
        } else if (fieldType.isAssignableFrom(Boolean.class)
                || fieldType.isAssignableFrom(boolean.class)) {
            int booleanInteger = cursor.getInt(columnIndex);
            value = booleanInteger == 1;
        }
        return value;
    }

    public void putInContentValues(ContentValues contentValues, Field field,
                                   Object object) throws IllegalAccessException {
        if (!field.isAccessible())
            field.setAccessible(true); // for private variables
        Object fieldValue = field.get(object);
        String key = getColumnName(field);
        if (fieldValue instanceof Long) {
            contentValues.put(key, Long.valueOf(fieldValue.toString()));
        } else if (fieldValue instanceof String || fieldValue instanceof CharSequence) {
            contentValues.put(key, fieldValue.toString());
        } else if (fieldValue instanceof Integer) {
            contentValues.put(key, Integer.valueOf(fieldValue.toString()));
        } else if (fieldValue instanceof Float) {
            contentValues.put(key, Float.valueOf(fieldValue.toString()));
        } else if (fieldValue instanceof Byte) {
            contentValues.put(key, Byte.valueOf(fieldValue.toString()));
        } else if (fieldValue instanceof Short) {
            contentValues.put(key, Short.valueOf(fieldValue.toString()));
        } else if (fieldValue instanceof Boolean) {
            contentValues.put(key, Boolean.parseBoolean(fieldValue.toString()));
        } else if (fieldValue instanceof Double) {
            contentValues.put(key, Double.valueOf(fieldValue.toString()));
        } else if (fieldValue instanceof Byte[] || fieldValue instanceof byte[]) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                        outputStream);
                objectOutputStream.writeObject(fieldValue);
                contentValues.put(key, outputStream.toByteArray());
                objectOutputStream.flush();
                objectOutputStream.close();
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
            }
        }
    }

    private static String getColumnName(Field field) {
        Column annotationColumn = field.getAnnotation(Column.class);
        String column = null;
        if (annotationColumn != null) {
            if (annotationColumn.name().equals("")) {
                column = field.getName();
            } else {
                column = annotationColumn.name();
            }
        }
        return column;
    }

    private ContentValues getFilledContentValues(Object object)
            throws IllegalAccessException {
        ContentValues contentValues = new ContentValues();
        for (Field field : getDeclaredFields(object.getClass())) {
            Column fieldEntityAnnotation = field.getAnnotation(Column.class);
            if (fieldEntityAnnotation != null) {
                if (!fieldEntityAnnotation.isAutoincrement()) {
                    putInContentValues(contentValues, field, object);
                }
            }
        }
        return contentValues;
    }

    private String[] getColumns() {
        boolean isHaveAnyKey = false;
        List<String> columnsList = new ArrayList<>();
        for (Field field : getDeclaredFields(tClass)) {
            Column fieldEntityAnnotation = field.getAnnotation(Column.class);
            if (fieldEntityAnnotation != null) {
                String columnName = getColumnName(field);
                if (columnName != null)
                    columnsList.add(columnName);
                if (fieldEntityAnnotation.isPrimaryKey()) {
                    isHaveAnyKey = true;
                }
            }
        }
        if (!isHaveAnyKey) {
            columnsList.add("_id");
        }
        String[] columnsArray = new String[columnsList.size()];
        return columnsList.toArray(columnsArray);
    }

    @SuppressWarnings("unused")
    private Cursor selectCursorFromTable(SQLiteDatabase db, String selection,
                                         String[] selectionArgs, String groupBy, String having,
                                         String orderBy) {
        try {
            String table = getTableName(tClass);
            String[] columns = getColumns();
            Cursor cursor = db.query(table, columns, selection, selectionArgs,
                    groupBy, having, orderBy);
            cursor.moveToFirst();
            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    public String getTableName(Class<?> tClass) {
        return mTableName;
//        Table annotationTable = tClass.getAnnotation(Table.class);
//        String table = tClass.getSimpleName();
//        if (annotationTable != null) {
//            if (!annotationTable.name().equals("")) {
//                table = annotationTable.name();
//            }
//        }
//        return table;
    }
}

