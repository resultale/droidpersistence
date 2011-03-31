package org.droidpersistence.dao;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;


public abstract class DroidDao<T> {
	
	private TableDefinition tableDefinition;
	private String insertStatement; 	
	
	private SQLiteDatabase database;
	private SQLiteStatement statement;
	private final Class<T> model;
	

	public DroidDao(Class<T> model, TableDefinition tableDefinition, SQLiteDatabase database){
		this.model = model;
		this.database = database;
						
		try {
			setTableDefinition(tableDefinition);
		} catch (Exception e) {
			e.printStackTrace();
		}	
		createInsertStatement(getTableDefinition().getTableName(), getTableDefinition().getArrayColumns());
		if(getInsertStatement().trim() != ""){
			statement = this.database.compileStatement(getInsertStatement());
		}
		
	}

	public boolean delete(int id) {
		boolean result = false;
		if(id > 0){
			try{
				database.delete(getTableDefinition().getTableName(), BaseColumns._ID + " = ?", 
						new String[] { String.valueOf(id) } );
				result = true;
			}catch(Exception e){
				e.printStackTrace();				
			}			
		}
		return result;
	}


	public T get(long id) {
		T object = null;
		Cursor cursor = database.query(getTableDefinition().getTableName(), getTableDefinition().getArrayColumns(), 
				BaseColumns._ID + " = ?", new String[]{String.valueOf(id)}, null, null, "1");
		if(cursor.moveToFirst()){
			try {
				object = buildDataFromCursor(cursor);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(! cursor.isClosed()){
			cursor.close();
		}		
		return object;
	}


	public List<T> getAll() {		
		List<T> objectList = new ArrayList<T>();
		Cursor cursor = database.query(getTableDefinition().getTableName(), getTableDefinition().getArrayColumns(), 
				null, null, null, null, "1");
		if(cursor.getCount() > 0){
			if(cursor.moveToFirst()){
				try {
					do{
						T object = buildDataFromCursor(cursor);
						if(object != null){
							objectList.add(object);
						}
					}
					while(cursor.moveToNext());
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}		
		if(! cursor.isClosed()){
			cursor.close();
		}		
		return objectList;
	}
	
	public List<T> getAllbyClause(String clause, String[] clauseArgs, String groupBy, String having, String orderBy) {		
		List<T> objectList = new ArrayList<T>();
		Cursor cursor = database.query(getTableDefinition().getTableName(), getTableDefinition().getArrayColumns(), 
				clause, clauseArgs, groupBy, having, orderBy);
		if(cursor.moveToFirst()){
			try {
				do{
					T object = buildDataFromCursor(cursor);
					if(object != null){
						objectList.add(object);
					}
				}
				while(cursor.moveToNext());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(! cursor.isClosed()){
			cursor.close();
		}		
		return objectList;
	}


	public T getByClause(String clause, String[] clauseArgs, String groupBy, String having, String orderBy) {
		T object = null;
		Cursor cursor = database.query(getTableDefinition().getTableName(), getTableDefinition().getArrayColumns(), 
				clause, clauseArgs, groupBy, having, orderBy);
		if(cursor.moveToFirst()){
			try {
				object = buildDataFromCursor(cursor);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(! cursor.isClosed()){
			cursor.close();
		}		
		return object;
	}


	public long save(T object) throws Exception{
		statement.clearBindings();
		
		for(int e = 0; e < getTableDefinition().getArrayColumns().length; e++){
			for(int i = 0; i < object.getClass().getDeclaredMethods().length; i++){
				Method method = object.getClass().getDeclaredMethods()[i];
				if(method.getName().equalsIgnoreCase("get"+getTableDefinition().getArrayColumns()[e])){
					i = object.getClass().getDeclaredMethods().length;
					Type type = method.getReturnType();	
					try{
						if(type == int.class){
							Integer output = (Integer) method.invoke(object);						
							statement.bindLong(e+1, output.longValue());
						}else{
							if(type == Long.class || type == Short.class){
								Long output = (Long) method.invoke(object);
								statement.bindLong(e+1, output);
							}else{
								if(type == Double.class || type == double.class || type == float.class){
									Double output = (Double) method.invoke(object);
									statement.bindDouble(e+1, output);
								}else{
									if(type == String.class){
										String output = (String) method.invoke(object);
										statement.bindString(e+1, output);
									}else{
										if(type == byte[].class){
											byte[] output = (byte[]) method.invoke(object);
											statement.bindBlob(e+1, output);
										}else{
											statement.bindNull(e+1);
										}										
									}
								}
							}
						}
					}catch(Exception ex){
						throw new Exception(" Failed to invoke the method "+method.getName()+", cause:"+ex.getMessage());
					}
				}
			}

		}
		
		
		return statement.executeInsert();
	}


	public void update(T object, long id) throws Exception{
		final ContentValues values = new ContentValues();
		
		for(int e = 0; e < getTableDefinition().getArrayColumns().length; e++){
			for(int i = 0; i < object.getClass().getDeclaredMethods().length; i++){
				Method method = object.getClass().getDeclaredMethods()[i];
				if(method.getName().equalsIgnoreCase("get"+getTableDefinition().getArrayColumns()[e])){
					i = object.getClass().getDeclaredMethods().length;
					String outputMethod = method.invoke(object).toString();
					values.put(tableDefinition.getArrayColumns()[e], outputMethod );
				}
			}			
		}	
		database.update(getTableDefinition().getTableName(), values, BaseColumns._ID + " = ?", 
				new String[]{String.valueOf(id)});
	}

	public String getInsertStatement() {
		return insertStatement;
	}

	public void setInsertStatement(String insertStatement) {
		this.insertStatement = insertStatement;
	}
	
	
	
	public TableDefinition getTableDefinition() {
		return tableDefinition;
	}

	public void setTableDefinition(TableDefinition tableDefinition) {
		this.tableDefinition = tableDefinition;
	}

	private void createInsertStatement(String tableName, String[] columns){
		StringBuffer values = new StringBuffer();
		StringBuffer tableColumns = new StringBuffer();
		
		for(int i = 0; i < columns.length; i++){
			if(i > 0){
				if(i < columns.length){
					values.append(",");
					tableColumns.append(",");
				}
			}
			values.append("?");
			tableColumns.append(columns[i]);
		}
		
		setInsertStatement("insert into " + tableName + "( _id, " + tableColumns + ") " + "values ( null, " + values + ")");
	}
	
	public T buildDataFromCursor(Cursor cursor) throws Exception{
		T object = null;
		
		Field[] fields = getTableDefinition().getFieldDefinition();		  		
		if(cursor != null){
			object = this.model.newInstance();
			
			if(cursor.getColumnName(0).equalsIgnoreCase("_id")){
				Method[] methods = object.getClass().getMethods();					
				for (int e = 0; e < methods.length; e++){
					if(methods[e].getName().trim().equalsIgnoreCase("setId")){	
						methods[e].invoke(object, cursor.getLong(0));
						e = methods.length;						
					}						
				}
			}
			
			Method[] methods = object.getClass().getMethods();
			
			for(int i = 1; i < cursor.getColumnCount(); i++){				
				
				try{						
													
					for (int e = 0; e < methods.length; e++){							
						
						if(methods[e].getName().trim().equalsIgnoreCase("set"+fields[i-1].getName())){							
							Method method = methods[e]; 
							e = methods.length;
							Type type = method.getParameterTypes()[0];				  
							if(type == int.class){
								method.invoke(object, Long.valueOf(cursor.getLong(i)).intValue() );
							}else{
								if(type == Long.class){
									method.invoke(object, cursor.getLong(i));
								}else{
									if(type == Double.class || type == double.class){
										method.invoke(object, cursor.getDouble(i));
									}else{
										if(type == float.class){
											method.invoke(object, cursor.getFloat(i));
										}else{
											if(type == String.class){
												method.invoke(object, cursor.getString(i));
											}else{
												if(type == Short.class){
													method.invoke(object, cursor.getShort(i));
												}else{
													method.invoke(object, cursor.getBlob(i));
												}
											}
										}
									}
								}
							}
						
						}

						}
				}catch(Exception e){
					throw new Exception(" Failed to cast a object, maybe a method not declared, cause:"+e.getMessage()); 
				}												
			}
		}
		if(object.getClass().getDeclaredFields().length == 0){
			throw new Exception("Cannot be cast a no field object!");
		}
		return (T) object;
	}
	

}
